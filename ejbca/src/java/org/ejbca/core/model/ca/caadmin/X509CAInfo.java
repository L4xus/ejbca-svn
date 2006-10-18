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
 
package org.ejbca.core.model.ca.caadmin;

import java.util.Collection;
import java.util.Date;

import org.ejbca.core.model.ca.catoken.CATokenInfo;
import org.ejbca.util.CertTools;
import org.ejbca.util.StringTools;


/**
 * Holds nonsensitive information about a X509CA.
 *
 * @version $Id: X509CAInfo.java,v 1.6 2006-10-18 08:58:38 anatom Exp $
 */
public class X509CAInfo extends CAInfo{
   
  String policyid;
  boolean useauthoritykeyidentifier;
  boolean authoritykeyidentifiercritical;
  boolean usecrlnumber;
  boolean crlnumbercritical;
  String defaultcrldistpoint;
  String defaultocsplocator;
  String subjectaltname;
  boolean useUTF8PolicyText;
    
    /**
     * Constructor that should be used when creating CA and retreiving CA info.
     */
    public X509CAInfo(String subjectdn, String name, int status, String subjectaltname, int certificateprofileid, 
                    int validity, Date expiretime, int catype, int signedby, Collection certificatechain, 
                    CATokenInfo catokeninfo, String description, int revokationreason, Date revokationdate, String policyid, int crlperiod, int crlIssueInterval, int crlOverlapTime, Collection crlpublishers,
                    boolean useauthoritykeyidentifier, boolean authoritykeyidentifiercritical,
                    boolean usecrlnumber, boolean crlnumbercritical, String defaultcrldistpoint, String defaultocspservicelocator, boolean finishuser,
                    Collection extendedcaserviceinfos, boolean useUTF8PolicyText, Collection approvalSettings, int numOfReqApprovals) {
        this.subjectdn = StringTools.strip(CertTools.stringToBCDNString(subjectdn));
        this.caid = this.subjectdn.hashCode();
        this.name = name;
        this.status = status;
        this.validity = validity;
        this.expiretime = expiretime;
        this.catype = catype;
        this.signedby = signedby;
        this.certificatechain = certificatechain;        
        this.catokeninfo = catokeninfo; 
        this.description = description;
        this.revokationreason = revokationreason;
        this.revokationdate = revokationdate;
        this.policyid = policyid;
        this.crlperiod = crlperiod;
        this.crlIssueInterval = crlIssueInterval;
        this.crlOverlapTime = crlOverlapTime;
        this.crlpublishers = crlpublishers;
        this.useauthoritykeyidentifier = useauthoritykeyidentifier;
        this.authoritykeyidentifiercritical = authoritykeyidentifiercritical;
        this.usecrlnumber = usecrlnumber;
        this.crlnumbercritical = crlnumbercritical;
        this.defaultcrldistpoint = defaultcrldistpoint;
        this.defaultocsplocator = defaultocspservicelocator;
        this.finishuser = finishuser;                     
        this.subjectaltname = subjectaltname;
        this.certificateprofileid = certificateprofileid;
        this.extendedcaserviceinfos = extendedcaserviceinfos; 
        this.useUTF8PolicyText = useUTF8PolicyText;
        this.approvalSettings = approvalSettings;
        this.numOfReqApprovals = numOfReqApprovals;
    }

    /**
     * Constructor that should be used when updating CA data.
     */
    public X509CAInfo(int caid, int validity, CATokenInfo catokeninfo, String description,
                      int crlperiod, int crlIssueInterval, int crlOverlapTime, Collection crlpublishers,
                      boolean useauthoritykeyidentifier, boolean authoritykeyidentifiercritical,
                      boolean usecrlnumber, boolean crlnumbercritical, String defaultcrldistpoint, String defaultocspservicelocator, 
                      boolean finishuser, Collection extendedcaserviceinfos, 
                      boolean useUTF8PolicyText, Collection approvalSettings, int numOfReqApprovals) {        
        this.caid = caid;
        this.validity=validity;
        this.catokeninfo = catokeninfo; 
        this.description = description;        
        this.crlperiod = crlperiod;
        this.crlIssueInterval = crlIssueInterval;
        this.crlOverlapTime = crlOverlapTime;
        this.crlpublishers = crlpublishers;
        this.useauthoritykeyidentifier = useauthoritykeyidentifier;
        this.authoritykeyidentifiercritical = authoritykeyidentifiercritical;
        this.usecrlnumber = usecrlnumber;
        this.crlnumbercritical = crlnumbercritical;
        this.defaultcrldistpoint = defaultcrldistpoint;
        this.defaultocsplocator = defaultocspservicelocator;
        this.finishuser = finishuser;
		this.extendedcaserviceinfos = extendedcaserviceinfos; 
        this.useUTF8PolicyText = useUTF8PolicyText;
        this.approvalSettings = approvalSettings;
        this.numOfReqApprovals = numOfReqApprovals;
    }  
  
  
  public X509CAInfo(){}
    
  public String getPolicyId(){ return policyid;}
 
  public boolean getUseCRLNumber(){ return usecrlnumber;}
  public void setUseCRLNumber(boolean usecrlnumber){ this.usecrlnumber=usecrlnumber;}
  
  public boolean getCRLNumberCritical(){ return crlnumbercritical;}
  public void setCRLNumberCritical(boolean crlnumbercritical){ this.crlnumbercritical=crlnumbercritical;}
  
  public boolean getUseAuthorityKeyIdentifier(){ return useauthoritykeyidentifier;}
  public void setUseAuthorityKeyIdentifier(boolean useauthoritykeyidentifier)
                {this.useauthoritykeyidentifier=useauthoritykeyidentifier;}
  
  public boolean getAuthorityKeyIdentifierCritical(){ return authoritykeyidentifiercritical;}
  public void setAuthorityKeyIdentifierCritical(boolean authoritykeyidentifiercritical)
                {this.authoritykeyidentifiercritical=authoritykeyidentifiercritical;}
  
  
  public String getDefaultCRLDistPoint(){ return defaultcrldistpoint; }
  
  public String getDefaultOCSPServiceLocator(){ return defaultocsplocator; }
  
  public String getSubjectAltName(){ return subjectaltname; }
  
  public boolean getUseUTF8PolicyText() { return useUTF8PolicyText; } 
  
}