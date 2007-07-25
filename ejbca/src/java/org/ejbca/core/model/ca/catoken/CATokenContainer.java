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
 
package org.ejbca.core.model.ca.catoken;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;

import org.ejbca.core.model.UpgradeableDataHashMap;



/** Handles maintenance of the device producing signatures and handling the private key.
 * 
 * @version $Id: CATokenContainer.java,v 1.1 2007-07-25 08:56:46 anatom Exp $
 */
public abstract class CATokenContainer extends UpgradeableDataHashMap implements java.io.Serializable{

    public static final String CATOKENTYPE = "catokentype";
    
    protected static final String SIGNATUREALGORITHM = "signaturealgorithm";
    protected static final String ENCRYPTIONALGORITHM = "encryptionalgorithm";

    /** constants needed for soft CA keystores */
    protected static final String SIGNKEYSPEC       = "SIGNKEYSPEC";
    protected static final String ENCKEYSPEC        = "ENCKEYSPEC";
    protected static final String SIGNKEYALGORITHM  = "SIGNKEYALGORITHM";
    protected static final String ENCKEYALGORITHM   = "ENCKEYALGORITHM";
    protected static final String KEYSTORE          = "KEYSTORE";

    /** Old provided for upgrade purposes from 3.3. -> 3.4 */
    protected static final String KEYALGORITHM  = "KEYALGORITHM";
    /** Old provided for upgrade purposes from 3.3. -> 3.4 */
    protected static final String KEYSIZE       = "KEYSIZE";

   /**
    *  Returns information about this CAToken.
    */
    public abstract CATokenInfo getCATokenInfo();  
    
   /**
    * Updates the CAToken data saved in database.
    */
    public abstract void updateCATokenInfo(CATokenInfo catokeninfo);

    /**
     * Method used to activate HardCATokens when connected after being offline.
     * 
     * @param authenticationcode used to unlock catoken, i.e PIN for smartcard HSMs
     * @throws CATokenOfflineException if CAToken is not available or connected.
     * @throws CATokenAuthenticationFailedException with error message if authentication to HardCATokens fail.
     */
    public abstract void activate(String authenticationcode) throws CATokenAuthenticationFailedException, CATokenOfflineException;    

    /**
     * Method used to deactivate HardCATokens. 
     * Used to set a CAToken too offline status and to reset the HSMs authorization code.
     * 
     * @return true if deactivation was successful.
     */
    public abstract boolean deactivate();    
   
    
   /** Returns the private key (if possible) of token.
    *
    * @param purpose should be SecConst.CAKEYPURPOSE_CERTSIGN, SecConst.CAKEYPURPOSE_CRLSIGN or SecConst.CAKEYPURPOSE_KEYENCRYPT 
    * @throws CATokenOfflineException if CAToken is not available or connected.
    * @return PrivateKey object
    */
    public abstract PrivateKey getPrivateKey(int purpose)  throws CATokenOfflineException;

   /** Returns the public key (if possible) of token.
    *
    * @param purpose should be SecConst.CAKEYPURPOSE_CERTSIGN, SecConst.CAKEYPURPOSE_CRLSIGN or SecConst.CAKEYPURPOSE_KEYENCRYPT    
    * @throws CATokenOfflineException if CAToken is not available or connected.
    * @return PublicKey object
    */
    public abstract PublicKey getPublicKey(int purpose) throws CATokenOfflineException;

    
    
    /** Returns the signature Provider that should be used to sign things with
     *  the PrivateKey object returned by this signingdevice implementation.
     * @return String the name of the Provider
     */
    public abstract String getProvider();

	/**
	 * Method that generates the keys that will be used by the CAToken.
	 * Only available for Soft CA Tokens so far.
	 */
	public abstract void generateKeys() throws Exception;  

	/**
	 * Method that import CA token keys from a P12 file. Was originally used when upgrading from 
	 * old EJBCA versions. Only supports SHA1 and SHA256 with RSA or ECDSA.
	 */
	public abstract void importKeys(PrivateKey privatekey, PublicKey publickey, PrivateKey privateEncryptionKey,
			PublicKey publicEncryptionKey, Certificate[] caSignatureCertChain) throws Exception;

}
