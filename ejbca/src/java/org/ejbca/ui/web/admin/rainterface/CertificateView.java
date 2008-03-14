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
 
package org.ejbca.ui.web.admin.rainterface;


import java.io.IOException;
import java.math.BigInteger;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.bouncycastle.util.encoders.Hex;
import org.ejbca.core.model.ca.certificateprofiles.CertificateProfile;
import org.ejbca.ui.web.admin.configuration.EjbcaWebBean;
import org.ejbca.util.CertTools;
import org.ejbca.util.cert.QCStatementExtension;
import org.ejbca.util.cert.SubjectDirAttrExtension;
import org.ejbca.util.dn.DNFieldExtractor;



/**
 * A class transforming X509 certificate data inte more readable form used
 * by JSP pages.
 *
 * @author  Philip Vendil
 * @version $Id: CertificateView.java,v 1.7 2008-03-14 16:31:46 anatom Exp $
 */
public class CertificateView implements java.io.Serializable {

   public static final int DIGITALSIGNATURE = CertificateProfile.DIGITALSIGNATURE;
   public static final int NONREPUDIATION   = CertificateProfile.NONREPUDIATION;
   public static final int KEYENCIPHERMENT  = CertificateProfile.KEYENCIPHERMENT;
   public static final int DATAENCIPHERMENT = CertificateProfile.DATAENCIPHERMENT;
   public static final int KEYAGREEMENT     = CertificateProfile.KEYAGREEMENT;
   public static final int KEYCERTSIGN      = CertificateProfile.KEYCERTSIGN;
   public static final int CRLSIGN          = CertificateProfile.CRLSIGN;
   public static final int ENCIPHERONLY     = CertificateProfile.ENCIPHERONLY;
   public static final int DECIPHERONLY     = CertificateProfile.DECIPHERONLY;
   
   public static final String[] KEYUSAGETEXTS = {"DIGITALSIGNATURE","NONREPUDIATION", "KEYENCIPHERMENT", "DATAENCIPHERMENT", "KEYAGREEMENT", "KEYCERTSIGN", "CRLSIGN", "ENCIPHERONLY", "DECIPHERONLY" };
   
   /** Array for texts that must match the indexes in CertificateProfile.EXTENDEDKEYUSAGEOIDSTRINGS.
    * if an extended key usage should not be displayed in the GUI, put null as value. 
    * This is done for deprecated ipsec key usages below. "IPSECENDSYSTEM", "IPSECTUNNEL", "IPSECUSER"  
    */
   public static final String[] EXTENDEDKEYUSAGETEXTS = {"ANYEXTENDEDKEYUSAGE","SERVERAUTH", "CLIENTAUTH", 
                                    "CODESIGNING", "EMAILPROTECTION", null, 
                                    null, null, "TIMESTAMPING", "SMARTCARDLOGON",
                                    "OCSPSIGNER", "EFS_CRYPTO", "EFS_RECOVERY", "IPSECIKE"};


   private static final int SUBALTNAME_OTHERNAME     = 0;
   private static final int SUBALTNAME_RFC822NAME    = 1;
   private static final int SUBALTNAME_DNSNAME       = 2;
   private static final int SUBALTNAME_X400ADDRESS   = 3;
   private static final int SUBALTNAME_DIRECTORYNAME = 4;
   private static final int SUBALTNAME_EDIPARTYNAME  = 5;   
   private static final int SUBALTNAME_URI           = 6;
   private static final int SUBALTNAME_IPADDRESS     = 7;   
   private static final int SUBALTNAME_REGISTREDID   = 8;

    /** Creates a new instance of CertificateView */
    public CertificateView(X509Certificate certificate, RevokedInfoView revokedinfo, String username) {
      this.certificate=certificate;
      this.revokedinfo= revokedinfo;
      this.username=username;

      subjectdnfieldextractor = new DNFieldExtractor(CertTools.getSubjectDN(certificate), DNFieldExtractor.TYPE_SUBJECTDN);
      issuerdnfieldextractor  = new DNFieldExtractor(CertTools.getIssuerDN(certificate), DNFieldExtractor.TYPE_SUBJECTDN);

      // Build HashMap of Extended KeyUsage OIDs (String) to Text representation (String)
      if(extendedkeyusageoidtotextmap == null){
        extendedkeyusageoidtotextmap = new HashMap();
        for(int i=0; i < EXTENDEDKEYUSAGETEXTS.length; i++){
           extendedkeyusageoidtotextmap.put(CertificateProfile.EXTENDEDKEYUSAGEOIDSTRINGS[i], EXTENDEDKEYUSAGETEXTS[i]);   
        }
      }
      
    }


    // Public methods
    /** Method that returns the version number of the X509 certificate. */
    public String getVersion() {
      return Integer.toString(certificate.getVersion());
    }

    public String getType() {
      return "X509";
    }

    public String getSerialNumber() {
      return certificate.getSerialNumber().toString(16).toUpperCase();
    }

    public BigInteger getSerialNumberBigInt() {
      return certificate.getSerialNumber();
    }

    public String getIssuerDN() {
      return CertTools.getIssuerDN(certificate);
    }

    public String getIssuerDNField(int field, int number) {
      return issuerdnfieldextractor.getField(field, number);
    }

    public String getSubjectDN() {
      return CertTools.getSubjectDN(certificate);
    }

    public String getSubjectDNField(int field, int number) {
      return subjectdnfieldextractor.getField(field, number);
    }

    public Date getValidFrom() {
      return certificate.getNotBefore();
    }

    public Date getValidTo() {
      return certificate.getNotAfter();
    }

    public boolean checkValidity(){
      boolean valid = true;
      try{
        certificate.checkValidity();
      }
      catch( CertificateExpiredException e){
        valid=false;
      }
      catch(CertificateNotYetValidException e){
         valid=false;
      }

      return valid;
    }

    public boolean checkValidity(Date date)  {
      boolean valid = true;
      try{
        certificate.checkValidity(date);
      }
      catch( CertificateExpiredException e){
        valid=false;
      }
      catch(CertificateNotYetValidException e){
         valid=false;
      }

      return valid;
    }

    public String getPublicKeyAlgorithm(){
      return certificate.getPublicKey().getAlgorithm();
    }

    public String getPublicKeyLength(){
      String keylength = null;
      if( certificate.getPublicKey() instanceof RSAPublicKey){
        keylength = "" + ((RSAPublicKey)certificate.getPublicKey()).getModulus().bitLength();
      }
      return keylength;
    }

    public String getSignatureAlgoritm() {
      return certificate.getSigAlgName();
    }

    /** Method that returns if key is allowed for given usage. Usage must be one of this class key usage constants. */
    public boolean getKeyUsage(int usage) {
      boolean returnval = false;
      if(certificate.getKeyUsage() != null)
        returnval= certificate.getKeyUsage()[usage];

      return returnval;
    }

    public boolean[] getAllKeyUsage(){
      return certificate.getKeyUsage();
    }
    
    public String[] getExtendedKeyUsageAsTexts(){
      java.util.List extendedkeyusage = null;  
      try{  
        extendedkeyusage = certificate.getExtendedKeyUsage();  
      }catch(java.security.cert.CertificateParsingException e){}  
      if(extendedkeyusage == null)    
        extendedkeyusage = new java.util.ArrayList();
      
      String[] returnval = new String[extendedkeyusage.size()];  
      for(int i=0; i < extendedkeyusage.size(); i++){
        returnval[i] = (String) extendedkeyusageoidtotextmap.get(extendedkeyusage.get(i));    
      }
        
      return returnval; 
    }

    public String getBasicConstraints(EjbcaWebBean ejbcawebbean) {
    	String retval = ejbcawebbean.getText("ENDENTITY");
    	if(certificate.getBasicConstraints() != -1){                	
       	    if(certificate.getBasicConstraints() == Integer.MAX_VALUE){
                retval = ejbcawebbean.getText("CANOLIMIT");
       	    }else{
               retval = ejbcawebbean.getText("CAPATHLENGTH") + " : " + certificate.getBasicConstraints();                    	 
            }
       }
    	
      return retval;
    }

    public String getSignature() {
      return (new java.math.BigInteger(certificate.getSignature())).toString(16);
    }

    public String getSHA1Fingerprint(){
      String returnval = "";
      try {
         byte[] res = CertTools.generateSHA1Fingerprint(certificate.getEncoded());
         String ret = new String(Hex.encode(res));
         returnval = ret.toUpperCase();
      } catch (CertificateEncodingException cee) {
      }
      return  returnval;
    }

    public String getMD5Fingerprint(){
      String returnval = "";
      try {
         byte[] res = CertTools.generateMD5Fingerprint(certificate.getEncoded());
         String ret = new String(Hex.encode(res));
         returnval = ret.toUpperCase();
      } catch (CertificateEncodingException cee) {
      }
      return  returnval;
    }
     
     

    public boolean isRevoked(){
      return revokedinfo != null  && revokedinfo.isRevoked();     
    }

    public String[] getRevokationReasons(){
      String[] returnval = null;
      if(revokedinfo != null)
        returnval = revokedinfo.getRevokationReasons();
      return returnval;
    }

    public Date getRevokationDate(){
      Date returnval = null;
      if(revokedinfo != null)
        returnval = revokedinfo.getRevocationDate();
      return returnval;
    }

    public String getUsername(){
      return this.username;
    }

    public X509Certificate getCertificate(){
      return certificate;
    }
    
    public String getSubjectDirAttr() {
    	if(subjectdirattrstring == null) {
    		try {
    			subjectdirattrstring = SubjectDirAttrExtension.getSubjectDirectoryAttributes(certificate);
    		} catch (Exception e) {
    			subjectdirattrstring = e.getMessage();		
    		}
    	}
    	return subjectdirattrstring;
    }
    
    public String getSubjectAltName() {
      if(subjectaltnamestring == null){      	
        try {
          if(certificate.getSubjectAlternativeNames() != null){
			subjectaltnamestring = "";
			
			String separator = "";
          	String guid = null;
          	try{              	  
          		guid = CertTools.getGuidAltName(certificate); 
          	}catch(IOException e){
          		subjectaltnamestring = e.getMessage();
          	}  
          	if(guid != null){
          		subjectaltnamestring += separator + "GUID=" + guid;
          		separator = ", ";
          	}
          	String upn = null;
          	try{              	  
          		upn = CertTools.getUPNAltName(certificate);
          	}catch(IOException e){
          		subjectaltnamestring = e.getMessage();	
          	}  
          	if(upn != null){               
          		subjectaltnamestring += separator + "UPN=" + upn;
          		separator = ", ";
          	}
			
			Iterator iter = certificate.getSubjectAlternativeNames().iterator();
			while(iter.hasNext()){				
              List next = (List) iter.next(); 
              int OID = ((Integer) next.get(0)).intValue();
              
              switch(OID){
              	case SUBALTNAME_OTHERNAME:
              	  // Already taken care of                                                                 
              	  break;
              	case SUBALTNAME_RFC822NAME: 
				  subjectaltnamestring += separator + "RFC822NAME=" + (String) next.get(1);
				  separator = ", ";
              	  break;
              	case SUBALTNAME_DNSNAME:
				  subjectaltnamestring += separator + "DNSNAME=" + (String) next.get(1);
				  separator = ", ";
              	  break;
              	case SUBALTNAME_X400ADDRESS:
              	  //TODO Implement X400ADDRESS
              	  break;
				case SUBALTNAME_EDIPARTYNAME:
				  //TODO Implement EDIPARTYNAME
				  break;              	                	  
                case SUBALTNAME_DIRECTORYNAME:
                  //TODO Implement EDIPARTYNAME
                  break;                                      
				case SUBALTNAME_URI:
		          if(!subjectaltnamestring.equals(""))
					 subjectaltnamestring += ", ";
				  subjectaltnamestring += separator + "URI=" + (String) next.get(1);
				  separator = ", ";
				  break;
				case SUBALTNAME_IPADDRESS:
				  subjectaltnamestring += separator + "IPADDRESS=" + (String) next.get(1);
				  separator = ", ";
				  break;
				case SUBALTNAME_REGISTREDID:
                  //TODO implement REGISTREDID
				  break;
              }

			}			
          }	
		} catch (CertificateParsingException e) {
			subjectaltnamestring = e.getMessage();		
		}                  
      }        

      return subjectaltnamestring; 	
    }

    public boolean hasQcStatement() {
    	boolean ret = false; 
    	try {
			ret = QCStatementExtension.hasQcStatement(certificate);
		} catch (IOException e) {
			ret = false;
		}
		return ret;
    }
    // Private fields
    private X509Certificate  certificate;
    private DNFieldExtractor subjectdnfieldextractor, issuerdnfieldextractor;
    private RevokedInfoView  revokedinfo;
    private String           username;
    private String           subjectaltnamestring;
    private String           subjectdirattrstring;
    private static HashMap   extendedkeyusageoidtotextmap;
}
