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

package org.ejbca.core.ejb.keyrecovery;

import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.FinderException;

import org.ejbca.core.ejb.BaseSessionBean;
import org.ejbca.core.ejb.authorization.IAuthorizationSessionLocal;
import org.ejbca.core.ejb.authorization.IAuthorizationSessionLocalHome;
import org.ejbca.core.ejb.ca.caadmin.ICAAdminSessionLocal;
import org.ejbca.core.ejb.ca.caadmin.ICAAdminSessionLocalHome;
import org.ejbca.core.ejb.ca.sign.ISignSessionLocal;
import org.ejbca.core.ejb.ca.sign.ISignSessionLocalHome;
import org.ejbca.core.ejb.ca.store.ICertificateStoreSessionLocal;
import org.ejbca.core.ejb.ca.store.ICertificateStoreSessionLocalHome;
import org.ejbca.core.ejb.log.ILogSessionLocal;
import org.ejbca.core.ejb.log.ILogSessionLocalHome;
import org.ejbca.core.ejb.ra.IUserAdminSessionLocal;
import org.ejbca.core.ejb.ra.IUserAdminSessionLocalHome;
import org.ejbca.core.ejb.approval.IApprovalSessionLocal;
import org.ejbca.core.ejb.approval.IApprovalSessionLocalHome;
import org.ejbca.core.model.approval.ApprovalException;
import org.ejbca.core.model.approval.ApprovalExecutorUtil;
import org.ejbca.core.model.approval.ApprovalOveradableClassName;
import org.ejbca.core.model.approval.WaitingForApprovalException;
import org.ejbca.core.model.approval.approvalrequests.KeyRecoveryApprovalRequest;
import org.ejbca.core.model.authorization.AuthorizationDeniedException;
import org.ejbca.core.model.authorization.AvailableAccessRules;
import org.ejbca.core.model.ca.caadmin.CAInfo;
import org.ejbca.core.model.ca.caadmin.extendedcaservices.KeyRecoveryCAServiceRequest;
import org.ejbca.core.model.ca.caadmin.extendedcaservices.KeyRecoveryCAServiceResponse;
import org.ejbca.core.model.keyrecovery.KeyRecoveryData;
import org.ejbca.core.model.log.Admin;
import org.ejbca.core.model.log.LogEntry;
import org.ejbca.core.model.ra.UserDataConstants;
import org.ejbca.util.CertTools;


/**
 * Stores key recovery data. Uses JNDI name for datasource as defined in env 'Datasource' in
 * ejb-jar.xml.
 *
 * @version $Id: LocalKeyRecoverySessionBean.java,v 1.6 2006-09-29 08:53:11 anatom Exp $
 *
 * @ejb.bean
 *   display-name="Stores key recovery data"
 *   name="KeyRecoverySession"
 *   jndi-name="KeyRecoverySession"
 *   local-jndi-name="KeyRecoverySessionLocal"
 *   view-type="both"
 *   type="Stateless"
 *   transaction-type="Container"
 *
 * @ejb.transaction type="Required"
 *
 * @weblogic.enable-call-by-reference True
 *
 * @ejb.env-entry description="JDBC datasource to be used"
 * name="DataSource"
 * type="java.lang.String"
 * value="${datasource.jndi-name-prefix}${datasource.jndi-name}"
 *
 * @ejb.ejb-external-ref
 *   description="The key recovery data entity bean"
 *   view-type="local"
 *   ejb-name="KeyRecoveryDataLocal"
 *   type="Entity"
 *   home="org.ejbca.core.ejb.keyrecovery.KeyRecoveryDataLocalHome"
 *   business="org.ejbca.core.ejb.keyrecovery.KeyRecoveryDataLocal"
 *   link="KeyRecoveryData"
 *
 * @ejb.ejb-external-ref
 *   description="The Sign Session Bean"
 *   view-type="local"
 *   ejb-name="RSASignSessionLocal"
 *   type="Session"
 *   home="org.ejbca.core.ejb.ca.sign.ISignSessionLocalHome"
 *   business="org.ejbca.core.ejb.ca.sign.ISignSessionLocal"
 *   link="RSASignSession"
 *
 * @ejb.ejb-external-ref
 *   description="The Certificate Store session bean"
 *   view-type="local"
 *   ejb-name="CertificateStoreSessionLocal"
 *   type="Session"
 *   home="org.ejbca.core.ejb.ca.store.ICertificateStoreSessionLocalHome"
 *   business="org.ejbca.core.ejb.ca.store.ICertificateStoreSessionLocal"
 *   link="CertificateStoreSession"
 *   
 * @ejb.ejb-external-ref description="The CAAdmin Session Bean"
 *   view-type="local"
 *   ejb-name="CAAdminSessionLocal"
 *   type="Session"
 *   home="org.ejbca.core.ejb.ca.caadmin.ICAAdminSessionLocalHome"
 *   business="org.ejbca.core.ejb.ca.caadmin.ICAAdminSessionLocal"
 *   link="CAAdminSession"
 *   
 * @ejb.ejb-external-ref
 *   description="The User Admin session bean"
 *   view-type="local"
 *   ejb-name="UserAdminSessionLocal"
 *   type="Session"
 *   home="org.ejbca.core.ejb.ra.IUserAdminSessionLocalHome"
 *   business="org.ejbca.core.ejb.ra.IUserAdminSessionLocalLocal"
 *   link="UserAdminSession"
 *
 * @ejb.ejb-external-ref description="The Approval Session Bean"
 *   view-type="local"
 *   ejb-name="ApprovalSessionLocal"
 *   type="Session"
 *   home="org.ejbca.core.ejb.approval.IApprovalSessionLocalHome"
 *   business="org.ejbca.core.ejb.approval.IApprovalSessionLocal"
 *   link="ApprovalSession"
 *   
 * @ejb.ejb-external-ref
 *   description="The Authorization session bean"
 *   view-type="local"
 *   ejb-name="AuthorizationSessionLocal"
 *   type="Session"
 *   home="org.ejbca.core.ejb.authorization.IAuthorizationSessionLocalHome"
 *   business="org.ejbca.core.ejb.authorization.IAuthorizationSessionLocal"
 *   link="AuthorizationSession"
 *
 * @ejb.ejb-external-ref
 *   description="The log session bean"
 *   view-type="local"
 *   ejb-name="LogSessionLocal"
 *   type="Session"
 *   home="org.ejbca.core.ejb.log.ILogSessionLocalHome"
 *   business="org.ejbca.core.ejb.log.ILogSessionLocal"
 *   link="LogSession"
 *
 * @ejb.home
 *   extends="javax.ejb.EJBHome"
 *   local-extends="javax.ejb.EJBLocalHome"
 *   local-class="org.ejbca.core.ejb.keyrecovery.IKeyRecoverySessionLocalHome"
 *   remote-class="org.ejbca.core.ejb.keyrecovery.IKeyRecoverySessionHome"
 *
 * @ejb.interface
 *   extends="javax.ejb.EJBObject"
 *   local-extends="javax.ejb.EJBLocalObject"
 *   local-class="org.ejbca.core.ejb.keyrecovery.IKeyRecoverySessionLocal"
 *   remote-class="org.ejbca.core.ejb.keyrecovery.IKeyRecoverySessionRemote"
 *
 * @jonas.bean
 *   ejb-name="KeyRecoverySession"
 *
 */
public class LocalKeyRecoverySessionBean extends BaseSessionBean {

    /** The local home interface of hard token issuer entity bean. */
    private KeyRecoveryDataLocalHome keyrecoverydatahome = null;

    /** The local interface of sign session bean */
    private ISignSessionLocal signsession = null;

    /** The local interface of certificate store session bean */
    private ICertificateStoreSessionLocal certificatestoresession = null;
    
    /** The local interface of the caadmin session bean*/
    private ICAAdminSessionLocal caadminsession = null;
    
    /** The local interface of the approval session bean*/
    private IApprovalSessionLocal approvalsession = null;
    
    /** The local interface of the useradmin session bean*/
    private IUserAdminSessionLocal useradminsession = null;
    
    

    /** The local interface of  log session bean */
    private ILogSessionLocal logsession = null;

    /** The local interface of  authorization session bean */
	private IAuthorizationSessionLocal authorizationsession;
	
	
	/**
	 * Method checking the following authorizations:
	 * 
	 * If /superadmin -> true
	 * 
	 * Other must have both
	 * AvailableAccessRules.
	 *  /ra_functionality/keyrecovery
	 *  and /endentityprofilesrules/<endentityprofile>/ keyrecovery
	 *  
	 * 
	 * @param admin
	 * @param profileid end entity profile
	 * @return true if the admin is authorized to keyrecover
	 * @throws AuthorizationDeniedException if administrator isn't authorized.
	 */
    private boolean authorizedToKeyRecover(Admin admin, int profileid) throws AuthorizationDeniedException{
        boolean returnval = false;
        try{
        	authorizationsession.isAuthorizedNoLog(admin, "/super_administrator");
        	returnval = true;
        }catch(AuthorizationDeniedException e){}
        
        if(admin.getAdminType() == Admin.TYPE_PUBLIC_WEB_USER){
        	returnval = true; // Special Case, public web use should be able to key recover
        }
        	
        if(!returnval){
        	returnval = authorizationsession.isAuthorizedNoLog(admin, AvailableAccessRules.ENDENTITYPROFILEPREFIX + profileid + AvailableAccessRules.KEYRECOVERY_RIGHTS) &&
        	authorizationsession.isAuthorizedNoLog(admin, AvailableAccessRules.REGULAR_KEYRECOVERY);                         
        }
        	
        return returnval;
    }

    /**
     * Help method that checks the CA data config if specified action 
     * requires approvals and how many
     * @param action one of CAInfo.REQ_APPROVAL_ constants
     * @param caid of the ca to check
     * @return 0 of no approvals is required othervise the number of approvals
     */
    private int getNumOfApprovalRequired(Admin admin,int action, int caid) {
    	CAInfo cainfo = caadminsession.getCAInfo(admin, caid);
    	return ApprovalExecutorUtil.getNumOfApprovalRequired(action, cainfo);    	
	}
    
    private IUserAdminSessionLocal getUserAdminSession(){
    	if(useradminsession == null){
    	  try {
    	    IUserAdminSessionLocalHome  useradminhome = (IUserAdminSessionLocalHome)	 getLocator().getLocalHome(IUserAdminSessionLocalHome.COMP_NAME);    	  
			useradminsession = useradminhome.create();
		  } catch (CreateException e) {
			throw new EJBException(e);		
    	  }
    	}	
    	return useradminsession;
    }
    
    /**
     * Help method to check if approval of key recovery is required
     * @param admin 
     * @param certificate 
     * @param username 
     * @param userdata 
     * @param checkNewest 
     * @throws ApprovalException 
     * @throws WaitingForApprovalException 
     */
    private void checkIfApprovalRequired(Admin admin, X509Certificate certificate, String username, int endEntityProfileId, boolean checkNewest) throws ApprovalException, WaitingForApprovalException{    	
        final int caid = CertTools.getIssuerDN(certificate).hashCode();
    	
        // Check if approvals is required.
        int numOfApprovalsRequired = getNumOfApprovalRequired(admin, CAInfo.REQ_APPROVAL_KEYRECOVER, caid );
        if (numOfApprovalsRequired > 0){    

			KeyRecoveryApprovalRequest ar = new KeyRecoveryApprovalRequest(certificate,username,checkNewest, admin,null,numOfApprovalsRequired,caid,endEntityProfileId);
			if (ApprovalExecutorUtil.requireApproval(ar, NONAPPROVABLECLASSNAMES_KEYRECOVERY)){       		    		
				approvalsession.addApprovalRequest(admin, ar);
				throw new WaitingForApprovalException("Key Recovery Action have been added for approval by authorized adminstrators");
			}

        } 
    }
    
    /**
     * Default create for SessionBean without any creation Arguments.
     *
     * @throws CreateException if bean instance can't be created
     */
    public void ejbCreate() throws CreateException {
        debug(">ejbCreate()");

        try {
            keyrecoverydatahome = (KeyRecoveryDataLocalHome) getLocator().getLocalHome(KeyRecoveryDataLocalHome.COMP_NAME);

            ILogSessionLocalHome logHome = (ILogSessionLocalHome) getLocator().getLocalHome(ILogSessionLocalHome.COMP_NAME);
            logsession = logHome.create();

            ICertificateStoreSessionLocalHome storeHome = (ICertificateStoreSessionLocalHome) getLocator().getLocalHome(ICertificateStoreSessionLocalHome.COMP_NAME);
            certificatestoresession = storeHome.create();

            ISignSessionLocalHome signsessionhome = (ISignSessionLocalHome) getLocator().getLocalHome(ISignSessionLocalHome.COMP_NAME);
            signsession = signsessionhome.create();
            
            IAuthorizationSessionLocalHome authorizationsessionhome = (IAuthorizationSessionLocalHome) getLocator().getLocalHome(IAuthorizationSessionLocalHome.COMP_NAME);
            authorizationsession = authorizationsessionhome.create();

            ICAAdminSessionLocalHome caadminsessionhome = (ICAAdminSessionLocalHome) getLocator().getLocalHome(ICAAdminSessionLocalHome.COMP_NAME);
            caadminsession = caadminsessionhome.create();
            
            IApprovalSessionLocalHome approvalsessionhome = (IApprovalSessionLocalHome) getLocator().getLocalHome(IApprovalSessionLocalHome.COMP_NAME);
            approvalsession = approvalsessionhome.create();
            

            
            debug("<ejbCreate()");
        } catch (Exception e) {
            throw new EJBException(e);
        }
    }

    /**
     * Adds a certificates keyrecovery data to the database.
     *
     * @param admin the administrator calling the function
     * @param certificate the certificate used with the keypair.
     * @param username of the administrator
     * @param keypair the actual keypair to save.
     *
     * @return false if the certificates keyrecovery data already exists.
     *
     * @throws EJBException if a communication or other error occurs.
     *
     * @ejb.interface-method view-type="both"
     */
    public boolean addKeyRecoveryData(Admin admin, X509Certificate certificate, String username,
                                      KeyPair keypair) {
        debug(">addKeyRecoveryData(user: " + username + ")");

        boolean returnval = false;

        try {
            int caid = CertTools.getIssuerDN(certificate).hashCode();

            KeyRecoveryCAServiceResponse response = (KeyRecoveryCAServiceResponse) signsession.extendedService(admin, caid,
                    new KeyRecoveryCAServiceRequest(KeyRecoveryCAServiceRequest.COMMAND_ENCRYPTKEYS, keypair));

            keyrecoverydatahome.create(certificate.getSerialNumber(),
                    CertTools.getIssuerDN(certificate), username, response.getKeyData());
            logsession.log(admin, certificate, LogEntry.MODULE_KEYRECOVERY, new java.util.Date(), username,
                    certificate, LogEntry.EVENT_INFO_KEYRECOVERY,
                    "Keyrecovery data for certificate with serial number : " +
                    certificate.getSerialNumber().toString(16) + ", " +
                    CertTools.getIssuerDN(certificate) + " added.");
            returnval = true;
        } catch (Exception e) {
            logsession.log(admin, certificate, LogEntry.MODULE_KEYRECOVERY, new java.util.Date(),
                    username, certificate, LogEntry.EVENT_ERROR_KEYRECOVERY,
                    "Error when trying to add keyrecovery data for certificate with serial number : " +
                    certificate.getSerialNumber().toString(16) + ", " +
                    CertTools.getIssuerDN(certificate) + ".");
        }

        debug("<addKeyRecoveryData()");

        return returnval;
    } // addKeyRecoveryData

    /**
     * Updates keyrecovery data
     *
     * @param admin DOCUMENT ME!
     * @param certificate DOCUMENT ME!
     * @param markedasrecoverable DOCUMENT ME!
     * @param keypair DOCUMENT ME!
     *
     * @return false if certificates keyrecovery data doesn't exists
     *
     * @throws EJBException if a communication or other error occurs.
     *
     * @ejb.interface-method view-type="both"
     */
    public boolean changeKeyRecoveryData(Admin admin, X509Certificate certificate,
                                         boolean markedasrecoverable, KeyPair keypair) {
        debug(">changeKeyRecoveryData(certsn: " + certificate.getSerialNumber().toString() + ", " +
                CertTools.getIssuerDN(certificate) + ")");

        boolean returnval = false;
        final String hexSerial = certificate.getSerialNumber().toString(16);
        final String dn = CertTools.getIssuerDN(certificate);
        try {
            KeyRecoveryDataLocal krd = keyrecoverydatahome.findByPrimaryKey(new KeyRecoveryDataPK(hexSerial, dn));
            krd.setMarkedAsRecoverable(markedasrecoverable);

            int caid = dn.hashCode();

            KeyRecoveryCAServiceResponse response = (KeyRecoveryCAServiceResponse) signsession.extendedService(admin, caid,
                    new KeyRecoveryCAServiceRequest(KeyRecoveryCAServiceRequest.COMMAND_ENCRYPTKEYS, keypair));


            krd.setKeyDataFromByteArray(response.getKeyData());
            logsession.log(admin, certificate, LogEntry.MODULE_KEYRECOVERY, new java.util.Date(),
                    krd.getUsername(), certificate, LogEntry.EVENT_INFO_KEYRECOVERY,
                    "Keyrecovery data for certificate with serial number : " +
                    hexSerial + ", " +
                    dn + " changed.");
            returnval = true;
        } catch (Exception e) {
            logsession.log(admin, certificate, LogEntry.MODULE_KEYRECOVERY, new java.util.Date(), null,
                    certificate, LogEntry.EVENT_ERROR_KEYRECOVERY,
                    "Error when trying to update keyrecovery data for certificate with serial number : " +
                    hexSerial + ", " +
                    dn + ".");
        }

        debug("<changeKeyRecoveryData()");

        return returnval;
    } // changeKeyRecoveryData

    /**
     * Removes a certificates keyrecovery data from the database.
     *
     * @param admin the administrator calling the function
     * @param certificate the certificate used with the keys about to be removed.
     *
     * @throws EJBException if a communication or other error occurs.
     *
     * @ejb.interface-method view-type="both"
     */
    public void removeKeyRecoveryData(Admin admin, X509Certificate certificate) {
        debug(">removeKeyRecoveryData(certificate: " + certificate.getSerialNumber().toString() +
                ")");
        final String hexSerial = certificate.getSerialNumber().toString(16);
        final String dn = CertTools.getIssuerDN(certificate);
        try {
            String username = null;
            KeyRecoveryDataLocal krd = keyrecoverydatahome.findByPrimaryKey(new KeyRecoveryDataPK(hexSerial, dn));
            username = krd.getUsername();
            krd.remove();
            logsession.log(admin, certificate, LogEntry.MODULE_KEYRECOVERY, new java.util.Date(), username,
                    certificate, LogEntry.EVENT_INFO_KEYRECOVERY,
                    "Keyrecovery data for certificate with serial number : " +
                    hexSerial + ", " +
                    dn + " removed.");
        } catch (Exception e) {
            logsession.log(admin, certificate, LogEntry.MODULE_KEYRECOVERY, new java.util.Date(), null,
                    certificate, LogEntry.EVENT_ERROR_KEYRECOVERY,
                    "Error when removing keyrecovery data for certificate with serial number : " +
                    hexSerial + ", " +
                    dn + ".");
        }

        debug("<removeKeyRecoveryData()");
    } // removeKeyRecoveryData

    /**
     * Removes a all keyrecovery data saved for a user from the database.
     *
     * @param admin DOCUMENT ME!
     * @param username DOCUMENT ME!
     *
     * @throws EJBException if a communication or other error occurs.
     *
     * @ejb.interface-method view-type="both"
     */
    public void removeAllKeyRecoveryData(Admin admin, String username) {
        debug(">removeAllKeyRecoveryData(user: " + username + ")");

        try {
            Collection result = keyrecoverydatahome.findByUsername(username);
            Iterator iter = result.iterator();

            while (iter.hasNext()) {
                ((KeyRecoveryDataLocal) iter.next()).remove();
            }

            logsession.log(admin, admin.getCaId(), LogEntry.MODULE_KEYRECOVERY, new java.util.Date(), username,
                    null, LogEntry.EVENT_INFO_KEYRECOVERY,
                    "All keyrecovery data for user: " + username + " removed.");
        } catch (Exception e) {
            logsession.log(admin, admin.getCaId(), LogEntry.MODULE_KEYRECOVERY, new java.util.Date(), null,
                    null, LogEntry.EVENT_ERROR_KEYRECOVERY,
                    "Error when removing all keyrecovery data for user: " + username + ".");
        }

        debug("<removeAllKeyRecoveryData()");
    } // removeAllKeyRecoveryData

    /**
     * Returns the keyrecovery data for a user. Observe only one certificates key can be recovered
     * for every user at the time.
     *
     * @param admin 
     * @param username
     * @param endentityprofileid, the end entity profile id the user belongs to.
     *
     * @return the marked keyrecovery data  or null if no recoverydata can be found.
     * @throws AuthorizationDeniedException 
     *
     * @throws EJBException if a communication or other error occurs.
     *
     * @ejb.interface-method view-type="both"
     */
    public KeyRecoveryData keyRecovery(Admin admin, String username, int endEntityProfileId) throws AuthorizationDeniedException {
        debug(">keyRecovery(user: " + username + ")");

        KeyRecoveryData returnval = null;
        KeyRecoveryDataLocal krd = null;
        X509Certificate certificate = null;
        
        if(authorizedToKeyRecover(admin, endEntityProfileId)){
        	
        	try {
        		Collection result = keyrecoverydatahome.findByUserMark(username);
        		Iterator i = result.iterator();
        		
        		try {
        			while (i.hasNext()) {
        				krd = (KeyRecoveryDataLocal) i.next();
        				
        				if (returnval == null) {
        					int caid = krd.getIssuerDN().hashCode();
        					
        					KeyRecoveryCAServiceResponse response = (KeyRecoveryCAServiceResponse) signsession.extendedService(admin, caid,
        							new KeyRecoveryCAServiceRequest(KeyRecoveryCAServiceRequest.COMMAND_DECRYPTKEYS, krd.getKeyDataAsByteArray()));
        					KeyPair keys = response.getKeyPair();
        					certificate = (X509Certificate) certificatestoresession
        					.findCertificateByIssuerAndSerno(admin,
        							krd.getIssuerDN(), krd.getCertificateSN());
        					returnval = new KeyRecoveryData(krd.getCertificateSN(), krd.getIssuerDN(),
        							krd.getUsername(), krd.getMarkedAsRecoverable(), keys, certificate);
        					
        					
        				}
        				
        				// krd.setMarkedAsRecoverable(false);
        			}
        			
        			logsession.log(admin, admin.getCaId(), LogEntry.MODULE_KEYRECOVERY, new java.util.Date(),
        					username, certificate, LogEntry.EVENT_INFO_KEYRECOVERY,
        					"Keydata for user: " + username + " have been sent for key recovery.");
        		} catch (Exception e) {
        			log.error("-keyRecovery: ", e);
        			logsession.log(admin, admin.getCaId(), LogEntry.MODULE_KEYRECOVERY, new java.util.Date(),
        					username, null, LogEntry.EVENT_ERROR_KEYRECOVERY,
        			"Error when trying to revover key data.");
        		}
        	} catch (FinderException e) {
        	}
        }

        debug("<keyRecovery()");

        return returnval;
    } // keyRecovery

    
	private static final ApprovalOveradableClassName[] NONAPPROVABLECLASSNAMES_KEYRECOVERY = {
		new ApprovalOveradableClassName("org.ejbca.core.model.approval.approvalrequests.KeyRecoveryApprovalRequest",null),		
	};
	
    /**
     * Marks a users newest certificate for key recovery. Newest means certificate with latest not
     * before date.
     *
     * @param admin the administrator calling the function
     * @param username or the user.
     * @param the end entity profile of the user, used for access control
     *
     * @return true if operation went successful or false if no certificates could be found for
     *         user, or user already marked.
     * @throws AuthorizationDeniedException 
     * @throws WaitingForApprovalException 
     * @throws ApprovalException 
     *
     * @throws EJBException if a communication or other error occurs.
     *
     * @ejb.interface-method view-type="both"
     */
    public boolean markNewestAsRecoverable(Admin admin, String username, int endEntityProfileId) throws AuthorizationDeniedException, ApprovalException, WaitingForApprovalException {
        debug(">markNewestAsRecoverable(user: " + username + ")");

        boolean returnval = false;
        long newesttime = 0;
        KeyRecoveryDataLocal krd = null;
        KeyRecoveryDataLocal newest = null;
        X509Certificate certificate = null;
        X509Certificate newestcertificate = null;

        if (!isUserMarked(admin, username)) {
            try {
                Collection result = keyrecoverydatahome.findByUsername(username);
                Iterator iter = result.iterator();

                while (iter.hasNext()) {
                    krd = (KeyRecoveryDataLocal) iter.next();
                    certificate = (X509Certificate) certificatestoresession
                            .findCertificateByIssuerAndSerno(admin,
                                    krd.getIssuerDN(), krd.getCertificateSN());

                    if (certificate != null) {
                        if (certificate.getNotBefore().getTime() > newesttime) {
                            newesttime = certificate.getNotBefore().getTime();
                            newest = krd;
                            newestcertificate = certificate;
                        }
                    }
                }

                if (newest != null) {
                	

                	
                    // Check that the administrator is authorized to keyrecover
                    authorizedToKeyRecover(admin, endEntityProfileId);        	        	
                    // Check if approvals is required.            
                    checkIfApprovalRequired(admin,newestcertificate,username,endEntityProfileId,true); 
                    newest.setMarkedAsRecoverable(true);
                    getUserAdminSession().setUserStatus(admin, username, UserDataConstants.STATUS_KEYRECOVERY);
                    returnval = true;
                }

                logsession.log(admin, admin.getCaId(), LogEntry.MODULE_KEYRECOVERY, new java.util.Date(),
                        username, newestcertificate, LogEntry.EVENT_INFO_KEYRECOVERY,
                        "User's newest certificate marked for recovery.");
            } catch (FinderException e) {
                logsession.log(admin, admin.getCaId(), LogEntry.MODULE_KEYRECOVERY, new java.util.Date(),
                        username, null, LogEntry.EVENT_ERROR_KEYRECOVERY,
                        "Error when trying to mark users newest certificate for recovery.");
            }
        }

        debug("<markNewestAsRecoverable()");

        return returnval;
    } // markNewestAsRecoverable

    /**
     * Marks a users certificate for key recovery.
     *
     * @param admin the administrator calling the function
     * @param certificate the certificate used with the keys about to be removed.
     *
     * @return true if operation went successful or false if  certificate couldn't be found.
     * @throws AuthorizationDeniedException 
     * @throws WaitingForApprovalException 
     * @throws ApprovalException 
     *
     * @throws EJBException if a communication or other error occurs.
     *
     * @ejb.interface-method view-type="both"
     */
    public boolean markAsRecoverable(Admin admin, X509Certificate certificate, int endEntityProfileId) throws AuthorizationDeniedException, WaitingForApprovalException, ApprovalException {
        debug(">markAsRecoverable(certificatesn: " + certificate.getSerialNumber() + ")");
        
        boolean returnval = false;
        final String hexSerial = certificate.getSerialNumber().toString(16);
        final String dn = CertTools.getIssuerDN(certificate);        
        try {
            String username = null;
            KeyRecoveryDataLocal krd = keyrecoverydatahome.findByPrimaryKey(new KeyRecoveryDataPK(hexSerial, dn));
            username = krd.getUsername();
        	
            // Check that the administrator is authorized to keyrecover
            authorizedToKeyRecover(admin, endEntityProfileId);        	        	
            // Check if approvals is required.            
            checkIfApprovalRequired(admin,certificate,username,endEntityProfileId,false); 
            krd.setMarkedAsRecoverable(true);
            getUserAdminSession().setUserStatus(admin, username, UserDataConstants.STATUS_KEYRECOVERY);
            logsession.log(admin, certificate, LogEntry.MODULE_KEYRECOVERY, new java.util.Date(), username,
                    certificate, LogEntry.EVENT_INFO_KEYRECOVERY,
                    "User's certificate marked for recovery.");
            returnval = true;
        } catch (FinderException e) {
        	log.debug(e);
            logsession.log(admin, certificate, LogEntry.MODULE_KEYRECOVERY, new java.util.Date(), null,
                    certificate, LogEntry.EVENT_ERROR_KEYRECOVERY,
                    "Error when trying to mark certificate for recovery.");
        } 

        debug("<markAsRecoverable()");

        return returnval;
    } // markAsRecoverable

    /**
     * Resets keyrecovery mark for a user,
     *
     * @param admin DOCUMENT ME!
     * @param username DOCUMENT ME!
     *
     * @throws EJBException if a communication or other error occurs.
     *
     * @ejb.interface-method view-type="both"
     */
    public void unmarkUser(Admin admin, String username) {
        debug(">unmarkUser(user: " + username + ")");

        KeyRecoveryDataLocal krd = null;

        try {
            Collection result = keyrecoverydatahome.findByUserMark(username);            
            Iterator i = result.iterator();

            while (i.hasNext()) {
                krd = (KeyRecoveryDataLocal) i.next();
                krd.setMarkedAsRecoverable(false);
            }
        } catch (Exception e) {
            throw new EJBException(e);
        }

        debug("<unmarkUser()");
    } // unmarkUser

    /**
     * Returns true if a user is marked for key recovery.
     *
     * @param admin DOCUMENT ME!
     * @param username DOCUMENT ME!
     *
     * @return true if user is already marked for key recovery.
     *
     * @throws EJBException if a communication or other error occurs.
     *
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Supports"
     */
    public boolean isUserMarked(Admin admin, String username) {
        debug(">isUserMarked(user: " + username + ")");

        boolean returnval = false;
        KeyRecoveryDataLocal krd = null;
        try {
            Collection result = keyrecoverydatahome.findByUserMark(username);
            Iterator i = result.iterator();

            while (i.hasNext()) {
                krd = (KeyRecoveryDataLocal) i.next();

                if (krd.getMarkedAsRecoverable()) {
                    returnval = true;
                    break;
                }
            }
        } catch (Exception e) {
            throw new EJBException(e);
        }
        debug("<isUserMarked(" + returnval + ")");
        return returnval;
    } // isUserMarked

    /**
     * Returns true if specified certificates keys exists in database.
     *
     * @param admin the administrator calling the function
     * @param certificate the certificate used with the keys about to be removed.
     *
     * @return true if user is already marked for key recovery.
     *
     * @throws EJBException if a communication or other error occurs.
     *
     * @ejb.interface-method view-type="both"
     * @ejb.transaction type="Supports"
     */
    public boolean existsKeys(Admin admin, X509Certificate certificate) {
        debug(">existsKeys()");

        boolean returnval = false;
        final String hexSerial = certificate.getSerialNumber().toString(16);
        final String dn = CertTools.getIssuerDN(certificate);
        try {
            KeyRecoveryDataLocal krd = keyrecoverydatahome.findByPrimaryKey(new KeyRecoveryDataPK(hexSerial, dn));
            debug("Found key for user: "+krd.getUsername());
            returnval = true;
        } catch (FinderException e) {
        }
        debug("<existsKeys(" + returnval + ")");
        return returnval;
    } // existsKeys

}// LocalKeyRecoverySessionBean


