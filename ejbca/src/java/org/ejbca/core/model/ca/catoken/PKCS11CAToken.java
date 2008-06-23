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

import java.security.KeyStore;
import java.security.Security;
import java.security.KeyStore.PasswordProtection;
import java.util.HashMap;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.ejbca.core.model.InternalResources;
import org.ejbca.util.keystore.KeyTools;

/**
 * @author lars
 * @version $Id$
 */
public class PKCS11CAToken extends BaseCAToken {

    /** Log4j instance */
    private static final Logger log = Logger.getLogger(PKCS11CAToken.class);
    /** Internal localization of logs and errors */
    private static final InternalResources intres = InternalResources.getInstance();

    /** Keys, specific to PKCS#11, that can be defined in CA token properties */
    static final public String SLOT_LABEL_KEY = "slot";
    static final public String SHLIB_LABEL_KEY = "sharedLibrary";
    static final public String ATTRIB_LABEL_KEY = "attributesFile";
    
    
    /**
     * @param providerClass
     * @throws InstantiationException
     */
    public PKCS11CAToken() throws InstantiationException {
        super();
        try {
        	PKCS11CAToken.class.getClassLoader().loadClass(KeyTools.SUNPKCS11CLASS);
        } catch (Throwable t) {
            throw new InstantiationException("Pkcs11 provider class "+KeyTools.SUNPKCS11CLASS+" not found.");
        }
    }

    /* (non-Javadoc)
     * @see org.ejbca.core.model.ca.catoken.BaseCAToken#activate(java.lang.String)
     */
    @Override
    public void activate(String authCode) throws CATokenOfflineException,
                                         CATokenAuthenticationFailedException {
        try {
            final PasswordProtection pwp =new PasswordProtection( (authCode!=null && authCode.length()>0)? authCode.toCharArray():null );
            final KeyStore.Builder builder = KeyStore.Builder.newInstance("PKCS11",
                                                                          Security.getProvider(getProvider()),
                                                                          pwp);
            final KeyStore keyStore = builder.getKeyStore();
            log.debug("Loading key from slot '"+sSlotLabel+"' using pin.");
            keyStore.load(null, null);
            setKeys(keyStore, null);
            pwp.destroy();
        } catch (Throwable t) {
            log.error("Failed to initialize PKCS11 provider slot '"+sSlotLabel+"'.", t);
            throw new CATokenAuthenticationFailedException("Failed to initialize PKCS11 provider slot '"+sSlotLabel+"'.");
        }
		String msg = intres.getLocalizedMessage("catoken.activated", "PKCS11");
        log.info(msg);
    }

    /* (non-Javadoc)
     * @see org.ejbca.core.model.ca.catoken.ICAToken#init(java.util.Properties, java.lang.String)
     */
    public void init(Properties properties, HashMap data, String signaturealgorithm) throws Exception {
    	// Don't autoactivate this right away, we must dynamically create the auth-provider with a slot
        init("slot", properties, signaturealgorithm, false);
        final boolean isIndex;
        if (sSlotLabel == null) {
            sSlotLabel = properties.getProperty("slotListIndex");         
            sSlotLabel = sSlotLabel!=null ? sSlotLabel.trim() : "-1";
            isIndex = sSlotLabel!=null;
        } else
            isIndex = false;
        String sharedLibrary = properties.getProperty(PKCS11CAToken.SHLIB_LABEL_KEY);
        String arributesFile = properties.getProperty(PKCS11CAToken.ATTRIB_LABEL_KEY);
        setJCAProvider( KeyTools.getP11Provider(sSlotLabel, sharedLibrary, isIndex, arributesFile) );
        autoActivate();
    }
}
