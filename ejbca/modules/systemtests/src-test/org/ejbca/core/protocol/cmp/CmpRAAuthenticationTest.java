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

package org.ejbca.core.protocol.cmp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Date;

import org.apache.log4j.Logger;
import org.bouncycastle.asn1.DEROutputStream;
import org.cesecore.authentication.tokens.AuthenticationToken;
import org.cesecore.authentication.tokens.UsernamePrincipal;
import org.cesecore.certificates.ca.CaSessionRemote;
import org.cesecore.certificates.ca.X509CAInfo;
import org.cesecore.certificates.certificate.CertificateStoreSessionRemote;
import org.cesecore.certificates.certificateprofile.CertificateProfileConstants;
import org.cesecore.certificates.crl.RevokedCertInfo;
import org.cesecore.certificates.util.AlgorithmConstants;
import org.cesecore.certificates.util.DnComponents;
import org.cesecore.keys.util.KeyTools;
import org.cesecore.mock.authentication.tokens.TestAlwaysAllowLocalAuthenticationToken;
import org.cesecore.util.CertTools;
import org.cesecore.util.CryptoProviderTools;
import org.cesecore.util.EjbRemoteHelper;
import org.ejbca.config.CmpConfiguration;
import org.ejbca.core.ejb.ca.caadmin.CAAdminSessionRemote;
import org.ejbca.core.ejb.config.ConfigurationSessionRemote;
import org.ejbca.core.ejb.ra.EndEntityManagementSessionRemote;
import org.ejbca.core.ejb.ra.raadmin.EndEntityProfileSessionRemote;
import org.ejbca.core.model.ra.raadmin.EndEntityProfile;
import org.ejbca.core.model.ra.raadmin.EndEntityProfileExistsException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.novosec.pkix.asn1.cmp.PKIMessage;

/**
 * This will test that different PBE shared secrets can be used to authenticate the RA to different CAs.
 * 
 * @version $Id$
 */
public class CmpRAAuthenticationTest extends CmpTestCase {

    private static final Logger LOG = Logger.getLogger(CmpRAAuthenticationTest.class);
    private static final AuthenticationToken ADMIN = new TestAlwaysAllowLocalAuthenticationToken(new UsernamePrincipal("CmpRAAuthenticationTest"));

    private static final String CA_NAME_1 = "CmpRAAuthenticationTestCA1";
    private static final String CA_NAME_2 = "CmpRAAuthenticationTestCA2";
    private static final String PBE_SECRET_1 = "sharedSecret1";
    private static final String PBE_SECRET_2 = "sharedSecret2";
    private static final String PBE_SECRET_3 = "sharedSecret3";
    private static final String EEP_1 = "CmpRAAuthenticationTestEEP";

    private static final String USERNAME = "cmpRAAuthenticationTestUser";
    
    private static X509Certificate caCertificate1;
    private static X509Certificate caCertificate2;
    
    private EndEntityManagementSessionRemote userAdminSession = EjbRemoteHelper.INSTANCE.getRemoteSession(EndEntityManagementSessionRemote.class);

    @BeforeClass
    public static void beforeClass() {
        CryptoProviderTools.installBCProviderIfNotAvailable();
    }

    /** Create CAs and change configuration for the following tests. */
    @Before
    public void setUp() throws Exception {
        super.setUp();
        // Create and configure CAs with different CMP RA secrets
        caCertificate1 = setupCA(CA_NAME_1, PBE_SECRET_1);
        caCertificate2 = setupCA(CA_NAME_2, PBE_SECRET_2);
        // Configure CMP for this test. RA mode with individual shared PBE secrets for each CA.
        updatePropertyOnServer(CmpConfiguration.CONFIG_OPERATIONMODE, "ra");
        updatePropertyOnServer(CmpConfiguration.CONFIG_ALLOWRAVERIFYPOPO, "true");
        updatePropertyOnServer(CmpConfiguration.CONFIG_RESPONSEPROTECTION, "pbe");
        updatePropertyOnServer(CmpConfiguration.CONFIG_RA_AUTHENTICATIONSECRET, null);
        updatePropertyOnServer(CmpConfiguration.CONFIG_RA_ENDENTITYPROFILE, "EMPTY");
        updatePropertyOnServer(CmpConfiguration.CONFIG_RA_CERTIFICATEPROFILE, "ENDUSER");
        updatePropertyOnServer(CmpConfiguration.CONFIG_RACANAME, "KeyId");
        updatePropertyOnServer(CmpConfiguration.CONFIG_AUTHENTICATIONMODULE, CmpConfiguration.AUTHMODULE_REG_TOKEN_PWD + ";" + CmpConfiguration.AUTHMODULE_HMAC);
        updatePropertyOnServer(CmpConfiguration.CONFIG_AUTHENTICATIONPARAMETERS, "-;-");
    }

    private X509Certificate setupCA(String caName, String pbeSecret) throws Exception {
        LOG.trace(">setupCA");
        assertTrue("Failed to create " + caName, createTestCA(caName, 1024));
        X509CAInfo x509CaInfo = (X509CAInfo) EjbRemoteHelper.INSTANCE.getRemoteSession(CaSessionRemote.class).getCAInfo(ADMIN, getTestCAId(caName));
        x509CaInfo.setCmpRaAuthSecret(pbeSecret);
        x509CaInfo.setUseCertReqHistory(false); // Disable storage of certificate history, to save some clean up
        EjbRemoteHelper.INSTANCE.getRemoteSession(CAAdminSessionRemote.class).editCA(ADMIN, x509CaInfo);
        X509Certificate ret = (X509Certificate) x509CaInfo.getCertificateChain().iterator().next();
        assertNotNull("CA certificate was null.", ret);
        LOG.trace("<setupCA");
        return ret;
    }

    /** Test that a CA specific secret. */
    @Test
    public void test01IssueConfirmRevoke1() throws Exception {
        LOG.trace(">test01IssueConfirmRevoke1");
        testIssueConfirmRevoke(caCertificate1, PBE_SECRET_1, CA_NAME_1);
        LOG.trace("<test01IssueConfirmRevoke1");
    }

    /** Test another CA specific secret. */
    @Test
    public void test02IssueConfirmRevoke2() throws Exception {
        LOG.trace(">test02IssueConfirmRevoke2");
        testIssueConfirmRevoke(caCertificate2, PBE_SECRET_2, CA_NAME_2);
        LOG.trace("<test02IssueConfirmRevoke2");
    }

    /** Test that a globally configured secret overrides any CA specific secret. */
    @Test
    public void test03IssueConfirmRevokeWithCommonSecret() throws Exception {
        LOG.trace(">test03IssueConfirmRevokeWithCommonSecret");
        updatePropertyOnServer(CmpConfiguration.CONFIG_RA_AUTHENTICATIONSECRET, PBE_SECRET_3);
        testIssueConfirmRevoke(caCertificate2, PBE_SECRET_3, CA_NAME_2);
        LOG.trace("<test03IssueConfirmRevokeWithCommonSecret");
    }

    /** Test that the proper secret is used if CA is configured to ProfileDefault (= use default from EEP). */
    @Test
    public void test04IssueConfirmRevokeEEP() throws Exception {
        LOG.trace(">test04IssueConfirmRevokeEEP");
        updatePropertyOnServer(CmpConfiguration.CONFIG_RA_AUTHENTICATIONSECRET, null);
        updatePropertyOnServer(CmpConfiguration.CONFIG_RACANAME, "ProfileDefault");
        updatePropertyOnServer(CmpConfiguration.CONFIG_RA_ENDENTITYPROFILE, EEP_1);
        // Create EEP
        if (EjbRemoteHelper.INSTANCE.getRemoteSession(EndEntityProfileSessionRemote.class).getEndEntityProfile(EEP_1) == null) {
            // Configure an EndEntity profile that allows CN, O, C in DN and rfc822Name, MS UPN in altNames.
            EndEntityProfile eep = new EndEntityProfile(true);
            eep.setValue(EndEntityProfile.DEFAULTCERTPROFILE, 0, "" + CertificateProfileConstants.CERTPROFILE_FIXED_ENDUSER);
            eep.setValue(EndEntityProfile.AVAILCERTPROFILES, 0, "" + CertificateProfileConstants.CERTPROFILE_FIXED_ENDUSER);
            eep.setValue(EndEntityProfile.DEFAULTCA, 0, "" + getTestCAId(CA_NAME_1));
            eep.setValue(EndEntityProfile.AVAILCAS, 0, "" + getTestCAId(CA_NAME_1));
            eep.setModifyable(DnComponents.RFC822NAME, 0, true);
            eep.setUse(DnComponents.RFC822NAME, 0, false); // Don't use field from "email" data
            try {
                EjbRemoteHelper.INSTANCE.getRemoteSession(EndEntityProfileSessionRemote.class).addEndEntityProfile(ADMIN, EEP_1, eep);
            } catch (EndEntityProfileExistsException e) {
                LOG.error("Could not create end entity profile " + EEP_1, e);
            }
        }
        testIssueConfirmRevoke(caCertificate1, PBE_SECRET_1, EEP_1);
        LOG.trace("<test04IssueConfirmRevokeEEP");
    }

    /**
     * Sends a certificate request message and verifies result. Sends a confirm message and verifies result. Sends a revocation message and verifies
     * result.
     */
    private void testIssueConfirmRevoke(X509Certificate caCertificate, String pbeSecret, String keyId) throws Exception {
        LOG.trace(">testIssueConfirmRevoke");
        // Generate and send certificate request
        byte[] nonce = CmpMessageHelper.createSenderNonce();
        byte[] transid = CmpMessageHelper.createSenderNonce(); 
        Date notBefore = new Date();
        Date notAfter = new Date(new Date().getTime() + 24 * 3600 * 1000);
        KeyPair keys = KeyTools.genKeys("512", AlgorithmConstants.KEYALGORITHM_RSA);
        String subjectDN = "CN=" + USERNAME;
        try {
            PKIMessage one = genCertReq(CertTools.getSubjectDN(caCertificate), subjectDN, keys, caCertificate, nonce, transid, true, null, notBefore,
                    notAfter, null);
            PKIMessage req = protectPKIMessage(one, false, pbeSecret, keyId, 567);
            assertNotNull("Request was not created properly.", req);
            int reqId = req.getBody().getIr().getCertReqMsg(0).getCertReq().getCertReqId().getValue().intValue();
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            new DEROutputStream(bao).writeObject(req);
            byte[] ba = bao.toByteArray();
            byte[] resp = sendCmpHttp(ba, 200);
            checkCmpResponseGeneral(resp, CertTools.getSubjectDN(caCertificate), subjectDN, caCertificate, nonce, transid, false, pbeSecret);
            X509Certificate cert = checkCmpCertRepMessage(subjectDN, caCertificate, resp, reqId);

            // Send a confirm message to the CA
            String hash = "foo123";
            PKIMessage confirm = genCertConfirm(subjectDN, caCertificate, nonce, transid, hash, reqId);
            assertNotNull("Could not create confirmation message.", confirm);
            PKIMessage req1 = protectPKIMessage(confirm, false, pbeSecret, keyId, 567);
            bao = new ByteArrayOutputStream();
            new DEROutputStream(bao).writeObject(req1);
            ba = bao.toByteArray();
            resp = sendCmpHttp(ba, 200);
            checkCmpResponseGeneral(resp, CertTools.getSubjectDN(caCertificate), subjectDN, caCertificate, nonce, transid, false, pbeSecret);
            checkCmpPKIConfirmMessage(subjectDN, caCertificate, resp);

            // Now revoke the bastard using the CMPv1 reason code!
            PKIMessage rev = genRevReq(CertTools.getSubjectDN(caCertificate), subjectDN, cert.getSerialNumber(), caCertificate, nonce, transid, false);
            PKIMessage revReq = protectPKIMessage(rev, false, pbeSecret, keyId, 567);
            assertNotNull("Could not create revocation message.", revReq);
            bao = new ByteArrayOutputStream();
            new DEROutputStream(bao).writeObject(revReq);
            ba = bao.toByteArray();
            resp = sendCmpHttp(ba, 200);
            checkCmpResponseGeneral(resp, CertTools.getSubjectDN(caCertificate), subjectDN, caCertificate, nonce, transid, false, pbeSecret);
            checkCmpRevokeConfirmMessage(CertTools.getSubjectDN(caCertificate), subjectDN, cert.getSerialNumber(), caCertificate, resp, true);
            int reason = EjbRemoteHelper.INSTANCE.getRemoteSession(CertificateStoreSessionRemote.class).getStatus(CertTools.getSubjectDN(caCertificate), cert.getSerialNumber()).revocationReason;
            assertEquals("Certificate was not revoked with the right reason.", RevokedCertInfo.REVOCATION_REASON_KEYCOMPROMISE, reason);
            LOG.trace("<testIssueConfirmRevoke");
        } finally {
            userAdminSession.deleteUser(ADMIN, USERNAME);
        }
    }

    /** Remove CAs and restore configuration that was used by the tests. */
    @After
    public void tearDown() throws Exception {
        super.tearDown();
        removeTestCA(CA_NAME_1);
        removeTestCA(CA_NAME_2);
        EjbRemoteHelper.INSTANCE.getRemoteSession(ConfigurationSessionRemote.class).restoreConfiguration();
        EjbRemoteHelper.INSTANCE.getRemoteSession(EndEntityProfileSessionRemote.class).removeEndEntityProfile(ADMIN, EEP_1);
    }
    
    public String getRoleName() {
        return this.getClass().getSimpleName(); 
    }
}
