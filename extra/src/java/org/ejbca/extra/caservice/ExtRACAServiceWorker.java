/*************************************************************************
 *                                                                       *
 *  EJBCA: The OpenSource Certificate Authority                          *
 *                                                                       *
 *  This software is free software; you can redistribute it and/or       *
 *  modify it under the terms of the GNU Lesser General Public           *
 *  License as published by the Free Software Foundation; either         *
 *  version 2.1 of the License, or any later version.                    *
 *                                                                       *
 *  See terms of license at gnu.org.                                     *
 *                                                                       *
 *************************************************************************/
package org.ejbca.extra.caservice;

import java.rmi.RemoteException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import javax.ejb.CreateException;
import javax.naming.NamingException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ejbca.config.ConfigurationHolder;
import org.ejbca.core.model.authorization.AuthorizationDeniedException;
import org.ejbca.core.model.ca.crl.RevokedCertInfo;
import org.ejbca.core.model.log.Admin;
import org.ejbca.core.model.services.BaseWorker;
import org.ejbca.core.model.services.ServiceExecutionFailedException;
import org.ejbca.core.model.util.EjbLocalHelper;
import org.ejbca.extra.caservice.processor.MessageProcessor;
import org.ejbca.extra.db.ISubMessage;
import org.ejbca.extra.db.Message;
import org.ejbca.extra.db.MessageHome;
import org.ejbca.extra.db.SubMessages;
import org.ejbca.extra.util.RAKeyStore;
import org.ejbca.util.CertTools;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/** An EJBCA Service worker that polls the External RA database for extRA messages and processes them.
 * The design includes that no two workers with the same serviceName can run on the same CA host at the same time.
 * 
 * @version $Id: ExtRACAProcess.java,v 1.26 2008-01-25 12:40:24 anatom Exp $
 */
public class ExtRACAServiceWorker extends BaseWorker {

	private static Logger log = Logger.getLogger(ExtRACAServiceWorker.class);

	private static final String defaultHibernateResource = "hibernate1.cfg.xml";
	private static final String defaultKeyStorePath = "keystore/extrakeystore.p12";
	private static final String defaultKeyStorePwd = "foo123";
	private static final Boolean defaultEncryptionRequired = Boolean.FALSE;
	private static final Boolean defaultSignatureRequired = Boolean.FALSE;
	private static final String defaultRAIssuer = "AdminCA1";
	
	private boolean encryptionRequired = false;
	private boolean signatureRequired = false;
	private String keystorePwd = null;
	private String caname = null;
	
	private SessionFactory sessionFactory = null;
	private MessageHome msgHome = null;
	
	private RAKeyStore serviceKeyStore = null;

	private Admin internalUser = new Admin(Admin.TYPE_INTERNALUSER);
	
	/** Used to help in looking up EJB interfaces */
	private final EjbLocalHelper ejb = new EjbLocalHelper();
	
	/** Semaphore to keep several processes from running simultaneously on the same host */
	private static HashMap running = new HashMap();

	/**
	 * Checks if there are any new messages on the External RA and processes them.
	 * 
	 * @see org.ejbca.core.model.services.IWorker#work()
	 */
	public void work() throws ServiceExecutionFailedException {
		log.debug(">work: "+serviceName);
		if (startWorking()) {
			try {
				// A semaphore used to not run parallel service jobs on the same host so not to start unlimited number of threads just
				// because there is a lot of work to do.
				init();
				processWaitingMessages();
			} finally {
				cleanup();
				stopWorking();
			}			
		} else {
			log.info("Service "+ExtRACAServiceWorker.class.getName()+" with name "+serviceName+" is already running in this VM! Not starting work.");
		}
		log.debug("<work: "+serviceName);
	}

	/** Synchronized method that makes checks if another service thread with this particular service name is already running. 
	 * If another service thread is running, false is returned. If another service is not running true is returned and an object is inserted in the running HashMap
	 * to indicate that this service thread is running. 
	 * @return false is another service thread with the same serviceName is running, false otherwise.
	 */
	private synchronized boolean startWorking() {
		boolean ret = false;
		Object o = running.get(serviceName);
		if (o == null) {
			running.put(serviceName, new Object());
			ret = true;
		} 
		return ret;
	}
	/** Removes the object, that was inserted in startWorking() from the running HashMap.
	 * @see #startWorking 
	 */
	private synchronized void stopWorking() {
		running.remove(serviceName);
	}
	
	private void init() {

		// Read configuration properties
		// First we get it from the built in configuration in the properties file using ConfigurationHolder
		// Second we try to override this value with a value from the properties of this specific worker, configured in the GUI
		// Oh, and if no configuration exist it uses the hard coded values from the top of this file.
		
		String hibernateconfresource = ConfigurationHolder.getString("externalra-caservice.hibernateresource", defaultHibernateResource);
		hibernateconfresource = this.properties.getProperty("externalra-caservice.hibernateresource", hibernateconfresource);
		log.debug("externalra-caservice.hibernateresource: "+hibernateconfresource);

		String keystorePath = ConfigurationHolder.getString("externalra-caservice.keystore.path", defaultKeyStorePath);
		keystorePath = this.properties.getProperty("externalra-caservice.keystore.path", keystorePath);
		log.debug("externalra-caservice.keystore.path: "+keystorePath);

		keystorePwd = ConfigurationHolder.getString("externalra-caservice.keystore.pwd", defaultKeyStorePwd);
		keystorePwd = this.properties.getProperty("externalra-caservice.keystore.pwd", keystorePwd);
		log.debug("externalra-caservice.keystore.pwd: "+keystorePwd);

		encryptionRequired = ConfigurationHolder.instance().getBoolean("externalra-caservice.encryption.required", defaultEncryptionRequired).booleanValue();
		encryptionRequired = Boolean.valueOf(this.properties.getProperty("externalra-caservice.encryption.required", Boolean.toString(encryptionRequired))).booleanValue();
		log.debug("externalra-caservice.encryption.required: "+encryptionRequired);

		signatureRequired = ConfigurationHolder.instance().getBoolean("externalra-caservice.signature.required", defaultSignatureRequired).booleanValue();
		signatureRequired = Boolean.valueOf(this.properties.getProperty("externalra-caservice.signature.required", Boolean.toString(signatureRequired))).booleanValue();
		log.debug("externalra-caservice.signature.required: "+signatureRequired);

		caname = ConfigurationHolder.getString("externalra-caservice.raissuer", defaultRAIssuer);
		caname = this.properties.getProperty("externalra-caservice.raissuer", caname);
		log.debug("externalra-caservice.raissuer: "+caname);
		
		// Initialize hibernate
        sessionFactory = new Configuration().configure(hibernateconfresource).buildSessionFactory();
        msgHome = new MessageHome(sessionFactory, MessageHome.MESSAGETYPE_EXTRA, false);

		try {
			serviceKeyStore = new RAKeyStore(keystorePath, keystorePwd);
		} catch (Exception e) {
			if(encryptionRequired || signatureRequired){
			  log.error("Error reading ExtRACAService keystore" ,e);
			}else{
			  log.debug("ExtRACAService KeyStore couldn't be configured, but isn't required");	
			}
		}
	}

	private void cleanup() {
		log.trace(">cleanup()");
		if (sessionFactory != null) {
			sessionFactory.close();
		}
		log.trace("<cleanup()");
	}
	
	/**
	 * Loops and gets waiting messages from the extRA database as long as there are any, and processes them. 
	 * If there are no more messages in status waiting the method ends.
	 */
	public void processWaitingMessages() {

		Collection cACertChain = null;
		try {
			cACertChain = MessageProcessor.getCACertChain(internalUser, caname, true, ejb);
		} catch (ConfigurationException e) {
			if(encryptionRequired || signatureRequired){
				log.error("RAIssuer is misconfigured: ", e);
				return;
			}else{
				log.debug("RAIssuer is misconfigured, but isn't required");	
			}
		}				

		Message msg = null;
		String lastMessageId = null;
		do{	
			msg = msgHome.getNextWaitingMessage();
			// A small section that makes sure we don't loop too quickly over the same message.
			// Check if we are trying to process the same messageId as the last time. If this is the case exit from the loop and let the next 
			// worker try to process it.
			// If it is not the same messageId process the message immediately.
			if (msg != null) {
				String id = msg.getMessageid();
				if (StringUtils.equals(id, lastMessageId)) {
					log.info("The same message was in the queue twice, putting back and exiting from the current loop");
					// Re-set status to waiting so we will process it the next time the service is run
					msg.setStatus(Message.STATUS_WAITING);
					msgHome.update(msg);							
					msg = null;
				} else {
					String errormessage = null;
					SubMessages submgs = null;
					try {
						log.info("Started processing message with messageId: " + msg.getMessageid()+", and uniqueId: "+msg.getUniqueId()); 

						if (serviceKeyStore != null) {
							submgs = msg.getSubMessages(
									(PrivateKey) serviceKeyStore.getKeyStore().getKey(serviceKeyStore.getAlias(), keystorePwd.toCharArray()),
									cACertChain,null);
						} else {
							submgs =  msg.getSubMessages(null,null,null);
						}
						if (submgs.isSigned()) {
							log.debug("Message from : " + msg.getMessageid() + " was signed");
						}
						if (signatureRequired && !submgs.isSigned()) {
							errormessage = "Error: Message from : " + msg.getMessageid() + " wasn't signed which is a requirement";
							log.error(errormessage);

						}
						if (submgs.isEncrypted()) {
							log.debug("Message from : " + msg.getMessageid() + " was encrypted");
						}
						if (encryptionRequired && !submgs.isEncrypted()) {
							errormessage = "Error: Message from : " + msg.getMessageid() + " wasn't encrypted which is a requirement";
							log.error(errormessage);
						}
					} catch (Exception e) {
						errormessage = "Error processing waiting message with Messageid : " + msg.getMessageid() + " : "+ e.getMessage();
						log.error("Error processing waiting message with Messageid : " + msg.getMessageid(), e);
					}

					if (submgs != null) {
						SubMessages respSubMsg;
						try {
							respSubMsg = generateResponseSubMessage(submgs.getSignerCert());
							Iterator iter = submgs.getSubMessages().iterator();
							boolean somethingprocessed = false;
							while(iter.hasNext()){
								ISubMessage respMsg = MessageProcessor.processSubMessage(getAdmin(submgs), (ISubMessage) iter.next(), errormessage);
								if (respMsg != null) {
									// if the response message is null here, we will ignore this message, 
									// it means that we should not do anything with it this round 
									respSubMsg.addSubMessage(respMsg);
									somethingprocessed = true;
								}
							}
							if (somethingprocessed) {
								msg.setStatus(Message.STATUS_PROCESSED);
								msg.setSubMessages(respSubMsg);
							} else {
								log.info("Nothing processed for msg with messageId: "+msg.getMessageid()+", leaving it in the queue");
								msg.setStatus(Message.STATUS_WAITING);
								// Update create time, so that we will process the next message instead of this again the next round in the loop
								msg.setCreatetime((new Date()).getTime());
							}
							msgHome.update(msg);							
						} catch (Exception e) {
							log.error("Error generating response message with Messageid : " + msg.getMessageid(), e);
						}

					}					
				}
				lastMessageId = id;	    	 
			}
		} while (msg != null);

	} // processWaitingMessage
	
	
	protected MessageHome getMessageHome() {
		return msgHome;
	}

	protected void storeMessageInRA(Message msg){
		log.trace(">storeMessageInRA() MessageId : " + msg.getMessageid());
		getMessageHome().update(msg);
		log.trace("<storeMessageInRA() MessageId : " + msg.getMessageid());		
	}
	

	// 
	// Private helper methods
	//
	
	/**
	 * Method used to retrieve which administrator to use.
	 * If message is signed then use the signer as admin otherwise use InternalUser
	 * @throws NamingException 
	 * @throws CreateException 
	 * @throws ClassCastException 
	 * @throws SignatureException 
	 * @throws AuthorizationDeniedException 
	 * @throws RemoteException 
	 */
	private Admin getAdmin(SubMessages submessages) throws ClassCastException, CreateException, NamingException, SignatureException,  AuthorizationDeniedException{
		if(submessages.isSigned()){
			
			// Check if Signer Cert is revoked
			X509Certificate signerCert = submessages.getSignerCert();
			
			Admin admin = new Admin(signerCert);
			
			// Check that user have the administrator flag set.
			getUserAdminSession().checkIfCertificateBelongToUser(admin, signerCert.getSerialNumber(), signerCert.getIssuerDN().toString());
			
			RevokedCertInfo revokeResult =  ejb.getCertStoreSession().isRevoked(internalUser,CertTools.stringToBCDNString(signerCert.getIssuerDN().toString()), signerCert.getSerialNumber());
			if(revokeResult == null || revokeResult.getReason() != RevokedCertInfo.NOT_REVOKED){
				throw new SignatureException("Error Signer certificate doesn't exist or is revoked.");
			}
			
			
			return admin;
		}
		return internalUser;
	}	
	
	/**
	 * Method that generates a response submessage depending on
	 * required security configuration
	 * @param reqCert the requestors certificate used for encryption.
	 * @return a new instance of a SubMessage
	 * @throws UnrecoverableKeyException 
	 * @throws NoSuchAlgorithmException 
	 * @throws KeyStoreException 
	 */	
	private SubMessages generateResponseSubMessage(X509Certificate reqCert) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
		
		if(encryptionRequired && signatureRequired){
			return new SubMessages((X509Certificate) serviceKeyStore.getKeyStore().getCertificate(serviceKeyStore.getAlias()),
					               (PrivateKey) serviceKeyStore.getKeyStore().getKey(serviceKeyStore.getAlias(), keystorePwd.toCharArray()),
					               reqCert);					                
		}
		if(signatureRequired){
			return new SubMessages((X509Certificate) serviceKeyStore.getKeyStore().getCertificate(serviceKeyStore.getAlias()),
					               (PrivateKey) serviceKeyStore.getKeyStore().getKey(serviceKeyStore.getAlias(), keystorePwd.toCharArray()),
					               null);					                
		}
		if(encryptionRequired){
			return new SubMessages(null,
					               null,
					               reqCert);					                
		}
		
		return new SubMessages(null,null,null);
	}

}
