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
 
package org.ejbca.core.model.ca.certificateprofiles;

import java.util.ArrayList;

/**
 * HardTokenAuthCertificateProfile is a class defining the fixed characteristics 
 * of a hard token authentication certificate.
 *
 * @version $Id$
 */
public class HardTokenAuthCertificateProfile extends CertificateProfile{

    // Public Constants

    public static final String CERTIFICATEPROFILENAME =  "HARDTOKEN_AUTH";

    // Public Methods
    /** Creates a certificate with the characteristics of an end user. 
    * General options are set in the superclass's default contructor that is called automatically.
    * You can override the general options by defining them again with different parameters here.
    */
    public HardTokenAuthCertificateProfile() {

      setType(TYPE_ENDENTITY);

      setUseKeyUsage(true);
      setKeyUsage(new boolean[9]);      
	  setKeyUsage(DIGITALSIGNATURE,true);           
      setKeyUsageCritical(true);

      setUseExtendedKeyUsage(true);
      ArrayList eku = new ArrayList();            
      eku.add(new Integer(CLIENTAUTH));      
      eku.add(new Integer(SMARTCARDLOGON));
      setExtendedKeyUsage(eku);
      setExtendedKeyUsageCritical(false);
      
    }

    // Public Methods.
    public void upgrade(){
    	if(Float.compare(LATEST_VERSION, getVersion()) != 0) {
    		// New version of the class, upgrade
    		
    		super.upgrade();         
    	}
    }


    // Private fields.
}
