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
package org.ejbca.extra.db;


/**
 * Ext RA Requset SubMessage when requesting using a PKCS10 and expecting a PKCS10 Response containing a certifcate. 
 * @author philip
 * $Id: ExtRAPKCS10Request.java,v 1.1 2006-07-31 13:13:06 herrvendil Exp $
 */

public class ExtRAPKCS10Request extends ExtRARequest {

	public static final float LATEST_VERSION = (float) 1.0;
	
	static final int CLASS_TYPE = 2;
	
	// Field constants
	private static final String PKCS10              = "PKCS10";
	
	
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Default constructor that should be used.
	 * @param pkcs10 the PKCS10 request
	 */
	public ExtRAPKCS10Request(long requestId, String username, String subjectDN, String subjectAltName, 
            String email, String subjectDirectoryAttributes,
            String endEntityProfileName, String certificateProfileName,
            String cAName, String pkcs10){
        super(requestId, username, subjectDN, subjectAltName, email, subjectDirectoryAttributes, endEntityProfileName, certificateProfileName,cAName);
		data.put(CLASSTYPE, new Integer(CLASS_TYPE));
		data.put(VERSION, new Float(LATEST_VERSION));
		data.put(PKCS10, pkcs10);
	}

	/**
	 * Constructor used when loaded from a persisted state
	 */	
	public ExtRAPKCS10Request(){}
	

	public float getLatestVersion() {
		return LATEST_VERSION;
	}
	
	/**
	 * Returns the PKCS10 used in this request.
	 * @return
	 */
	public String getPKCS10(){
		return (String) data.get(PKCS10);
	}
	
	public void upgrade() {
		if(LATEST_VERSION != getVersion()){						
			data.put(VERSION, new Float(LATEST_VERSION));
		}
		
	}

}
