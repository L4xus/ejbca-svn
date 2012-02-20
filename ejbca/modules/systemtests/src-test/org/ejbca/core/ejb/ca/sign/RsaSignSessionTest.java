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
package org.ejbca.core.ejb.ca.sign;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DEROutputStream;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.X509Extension;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.asn1.x509.qualified.ETSIQCObjectIdentifiers;
import org.bouncycastle.asn1.x509.qualified.RFC3739QCObjectIdentifiers;
import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.bouncycastle.jce.X509KeyUsage;
import org.cesecore.authentication.tokens.AuthenticationToken;
import org.cesecore.authentication.tokens.UsernamePrincipal;
import org.cesecore.certificates.ca.CAConstants;
import org.cesecore.certificates.ca.CADoesntExistsException;
import org.cesecore.certificates.ca.CAInfo;
import org.cesecore.certificates.ca.CaSessionRemote;
import org.cesecore.certificates.ca.SignRequestException;
import org.cesecore.certificates.ca.SignRequestSignatureException;
import org.cesecore.certificates.certificate.IllegalKeyException;
import org.cesecore.certificates.certificate.request.PKCS10RequestMessage;
import org.cesecore.certificates.certificate.request.ResponseMessage;
import org.cesecore.certificates.certificate.request.X509ResponseMessage;
import org.cesecore.certificates.certificateprofile.CertificateProfile;
import org.cesecore.certificates.certificateprofile.CertificateProfileConstants;
import org.cesecore.certificates.certificateprofile.CertificateProfileSessionRemote;
import org.cesecore.certificates.endentity.EndEntityInformation;
import org.cesecore.certificates.endentity.EndEntityType;
import org.cesecore.certificates.endentity.EndEntityTypes;
import org.cesecore.certificates.endentity.ExtendedInformation;
import org.cesecore.certificates.util.AlgorithmConstants;
import org.cesecore.certificates.util.AlgorithmTools;
import org.cesecore.certificates.util.DnComponents;
import org.cesecore.certificates.util.cert.QCStatementExtension;
import org.cesecore.keys.util.KeyTools;
import org.cesecore.mock.authentication.tokens.TestAlwaysAllowLocalAuthenticationToken;
import org.cesecore.util.Base64;
import org.cesecore.util.CertTools;
import org.cesecore.util.CryptoProviderTools;
import org.cesecore.util.EjbRemoteHelper;
import org.ejbca.core.ejb.ca.CaTestCase;
import org.ejbca.core.ejb.ca.caadmin.CAAdminSessionRemote;
import org.ejbca.core.ejb.ca.store.CertReqHistorySessionRemote;
import org.ejbca.core.ejb.ra.UserAdminSessionRemote;
import org.ejbca.core.ejb.ra.raadmin.EndEntityProfileSessionRemote;
import org.ejbca.core.model.SecConst;
import org.ejbca.core.model.ca.AuthStatusException;
import org.ejbca.core.model.ca.store.CertReqHistory;
import org.ejbca.core.model.ra.ExtendedInformationFields;
import org.ejbca.core.model.ra.UserDataConstants;
import org.ejbca.core.model.ra.raadmin.EndEntityProfile;
import org.ejbca.cvc.CardVerifiableCertificate;
import org.ejbca.util.cert.SeisCardNumberExtension;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test class for tests based on an RSA
 * 
 * @version $Id$
 *
 */
public class RsaSignSessionTest extends SignSessionCommon {

    private static final Logger log = Logger.getLogger(RsaSignSessionTest.class);

    private static byte[] keytoolp10 = Base64.decode(("MIIBbDCB1gIBADAtMQ0wCwYDVQQDEwRUZXN0MQ8wDQYDVQQKEwZBbmFUb20xCzAJBgNVBAYTAlNF"
            + "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDY+ATE4ZB0oKfmXStu8J+do0GhTag6rOGtoydI"
            + "eNX9DdytlsmXDyONKl8746478/3HXdx9rA0RevUizKSataMpDsb3TjprRjzBTvYPZSIfzko6s8g6"
            + "AZLO07xCFOoDmyRzb9k/KEZsMls0ujx79CQ9p5K4rg2ksjmDeW7DaPMphQIDAQABoAAwDQYJKoZI"
            + "hvcNAQEFBQADgYEAyJVobqn6wGRoEsdHxjoqPXw8fLrQyBGEwXccnVpI4kv9iIZ45Xres0LrOwtS"
            + "kFLbpn0guEzhxPBbL6mhhmDDE4hbbHJp1Kh6gZ4Bmbb5FrwpvUyrSjTIwwRC7GAT00A1kOjl9jCC" + "XCfJkJH2QleCy7eKANq+DDTXzpEOvL/UqN0=").getBytes());
    private static byte[] iep10 = Base64.decode(("MIICnTCCAgYCAQAwGzEZMBcGA1UEAxMQNkFFSzM0N2Z3OHZXRTQyNDCBnzANBgkq"
            + "hkiG9w0BAQEFAAOBjQAwgYkCgYEAukW70HN9bt5x2AiSZm7y8GXQuyp1jN2OIvqU" + "sr0dzLIOFt1H8GPJkL80wx3tLDj3xJfWJdww3TqExsxMSP+qScoYKIOeNBb/2OMW"
            + "p/k3DThCOewPebmt+M08AClq5WofXTG+YxyJgXWbMTNfXKIUyR0Ju4Spmg6Y4eJm" + "GXTG7ZUCAwEAAaCCAUAwGgYKKwYBBAGCNw0CAzEMFgo1LjAuMjE5NS4yMCAGCisG"
            + "AQQBgjcCAQ4xEjAQMA4GA1UdDwEB/wQEAwIE8DCB/wYKKwYBBAGCNw0CAjGB8DCB" + "7QIBAR5cAE0AaQBjAHIAbwBzAG8AZgB0ACAARQBuAGgAYQBuAGMAZQBkACAAQwBy"
            + "AHkAcAB0AG8AZwByAGEAcABoAGkAYwAgAFAAcgBvAHYAaQBkAGUAcgAgAHYAMQAu" + "ADADgYkAjuYPzZPpbLgCWYnXoNeX2gS6nuI4osrWHlQQKcS67VJclhELlnT3hBb9"
            + "Blr7I0BsJ/lguZvZFTZnC1bMeNULRg17bhExTg+nUovzPcJhMvG7G3DR17PrJ7V+" + "egHAsQV4dQC2hOGGhOnv88JhP9Pwpso3t2tqJROa5ZNRRSJSkw8AAAAAAAAAADAN"
            + "BgkqhkiG9w0BAQQFAAOBgQCL5k4bJt265j63qB/9GoQb1XFOPSar1BDFi+veCPA2" + "GJ/vRXt77Vcr4inx9M51iy87FNcGGsmyesBoDg73p06UxpIDhkL/WpPwZAfQhWGe"
            + "o/gWydmP/hl3uEfE0E4WG02UXtNwn3ziIiJM2pBCGQQIN2rFggyD+aTxwAwOU7Z2" + "fw==").getBytes());
    private static byte[] keytooldsa = Base64.decode(("MIICNjCCAfQCAQAwMTERMA8GA1UEAxMIRFNBIFRlc3QxDzANBgNVBAoTBkFuYXRvbTELMAkGA1UE"
            + "BhMCU0UwggG4MIIBLAYHKoZIzjgEATCCAR8CgYEA/X9TgR11EilS30qcLuzk5/YRt1I870QAwx4/"
            + "gLZRJmlFXUAiUftZPY1Y+r/F9bow9subVWzXgTuAHTRv8mZgt2uZUKWkn5/oBHsQIsJPu6nX/rfG"
            + "G/g7V+fGqKYVDwT7g/bTxR7DAjVUE1oWkTL2dfOuK2HXKu/yIgMZndFIAccCFQCXYFCPFSMLzLKS"
            + "uYKi64QL8Fgc9QKBgQD34aCF1ps93su8q1w2uFe5eZSvu/o66oL5V0wLPQeCZ1FZV4661FlP5nEH"
            + "EIGAtEkWcSPoTCgWE7fPCTKMyKbhPBZ6i1R8jSjgo64eK7OmdZFuo38L+iE1YvH7YnoBJDvMpPG+"
            + "qFGQiaiD3+Fa5Z8GkotmXoB7VSVkAUw7/s9JKgOBhQACgYEAiVCUaC95mHaU3C9odWcuJ8j3fT6z"
            + "bSR02CVFC0F6QO5s2Tx3JYWrm5aAjWkXWJfeYOR6qBSwX0R1US3rDI0Kepsrdco2q7wGSo+235KL"
            + "Yfl7tQ9RLOKUGX/1c5+XuvN1ZbGy0yUw3Le16UViahWmmx6FM1sW6M48U7C/CZOyoxagADALBgcq"
            + "hkjOOAQDBQADLwAwLAIUQ+S2iFA1y7dfDWUCg7j1Nc8RW0oCFFhnDlU69xFRMeXXn1C/Oi+8pwrQ").getBytes());

    private static final String RSA_USERNAME = "RsaUser";
    private static final String RSA_REVERSE_USERNAME = "RsaReverseUser";
    private static final String DEFAULT_EE_PROFILE = "FOOEEPROFILE";
    private static final String DEFAULT_CERTIFICATE_PROFILE = "FOOCERTPROFILE";

    private static final AuthenticationToken internalAdmin = new TestAlwaysAllowLocalAuthenticationToken(new UsernamePrincipal("RsaSignSessionTest"));

    private CAAdminSessionRemote caAdminSession = EjbRemoteHelper.INSTANCE.getRemoteSession(CAAdminSessionRemote.class);
    private CaSessionRemote caSession = EjbRemoteHelper.INSTANCE.getRemoteSession(CaSessionRemote.class);
    private CertificateProfileSessionRemote certificateProfileSession = EjbRemoteHelper.INSTANCE
            .getRemoteSession(CertificateProfileSessionRemote.class);
    private CertReqHistorySessionRemote certReqHistorySession = EjbRemoteHelper.INSTANCE.getRemoteSession(CertReqHistorySessionRemote.class);
    private EndEntityProfileSessionRemote endEntityProfileSession = EjbRemoteHelper.INSTANCE.getRemoteSession(EndEntityProfileSessionRemote.class);
    private SignSessionRemote signSession = EjbRemoteHelper.INSTANCE.getRemoteSession(SignSessionRemote.class);
    private UserAdminSessionRemote userAdminSession = EjbRemoteHelper.INSTANCE.getRemoteSession(UserAdminSessionRemote.class);

    private static KeyPair rsakeys;

    @BeforeClass
    public static void beforeClass() throws Exception {
        // Install BouncyCastle provider
        CryptoProviderTools.installBCProviderIfNotAvailable();
        CaTestCase.createTestCA();
        CaSessionRemote caSession = EjbRemoteHelper.INSTANCE.getRemoteSession(CaSessionRemote.class);
        int rsacaid = caSession.getCAInfo(internalAdmin, getTestCAName()).getCAId();
        createEndEntity(RSA_USERNAME, DEFAULT_EE_PROFILE, DEFAULT_CERTIFICATE_PROFILE, rsacaid);

        rsakeys = KeyTools.genKeys("1024", AlgorithmConstants.KEYALGORITHM_RSA);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        cleanUpEndEntity(RSA_USERNAME);
        CaTestCase.removeTestCA();
        CertificateProfileSessionRemote certificateProfileSession = EjbRemoteHelper.INSTANCE.getRemoteSession(CertificateProfileSessionRemote.class);
        EndEntityProfileSessionRemote endEntityProfileSession = EjbRemoteHelper.INSTANCE.getRemoteSession(EndEntityProfileSessionRemote.class);
        // Delete test end entity profile
        try {
            endEntityProfileSession.removeEndEntityProfile(internalAdmin, "TESTREQUESTCOUNTER");
        } catch (Exception e) {
            /* ignore */
        }
        try {
            endEntityProfileSession.removeEndEntityProfile(internalAdmin, "TESTISSUANCEREVREASON");
        } catch (Exception e) { /* ignore */
        }
        try {
            endEntityProfileSession.removeEndEntityProfile(internalAdmin, "TESTDNOVERRIDE");
        } catch (Exception e) { /* ignore */
        }
        try {
            endEntityProfileSession.removeEndEntityProfile(internalAdmin, EEPROFILE_PRIVKEYUSAGEPERIOD);
        } catch (Exception ignored) { /* ignore */
        }
        try {
            certificateProfileSession.removeCertificateProfile(internalAdmin, CERTPROFILE_PRIVKEYUSAGEPERIOD);
        } catch (Exception e) { /* ignore */
        }
        try {
            certificateProfileSession.removeCertificateProfile(internalAdmin, "TESTDNOVERRIDE ");
        } catch (Exception e) { /* ignore */
        }
    }

    @After
    public void tearDown() throws Exception {


    }

    @Test
    public void testSignSession() throws Exception {
        createReverseEndEntity();
        try {
            // user that we know exists...
            X509Certificate cert = (X509Certificate) signSession.createCertificate(internalAdmin, RSA_USERNAME, "foo123", rsakeys.getPublic());
            assertNotNull("Failed to create certificate.", cert);
            log.debug("Cert=" + cert.toString());
            // Normal DN order
            assertEquals(cert.getSubjectX500Principal().getName(), "C=SE,CN=" + RSA_USERNAME);
            X509Certificate rsacacert = (X509Certificate) caSession.getCAInfo(internalAdmin, getTestCAName()).getCertificateChain().toArray()[0];
            cert.verify(rsacacert.getPublicKey());
            cert = (X509Certificate) signSession.createCertificate(internalAdmin, RSA_REVERSE_USERNAME, "foo123", rsakeys.getPublic());
            assertNotNull("Failed to create certificate.", cert);
            log.debug("Cert=" + cert.toString());
            // Reverse DN order
            assertEquals(cert.getSubjectX500Principal().getName(), "CN=" + RSA_REVERSE_USERNAME + ",O=AnaTom,C=SE");
            CAInfo inforsareverse = caSession.getCAInfo(internalAdmin, "TESTRSAREVERSE");
            X509Certificate rsarevcacert = (X509Certificate) inforsareverse.getCertificateChain().toArray()[0];
            try {
                cert.verify(rsarevcacert.getPublicKey());
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                fail("Verify failed: " + e.getMessage());
            }
        } finally {
            try {
                userAdminSession.deleteUser(internalAdmin, RSA_REVERSE_USERNAME);
            } catch (Exception e) {
                //NOPMD
            }
        }
    }

    /**
     * test DSA keys instead of RSA
     * 
     * @throws Exception
     *             if en error occurs...
     */
    @Test
    public void testDSAKey() throws Exception {
        userAdminSession.setUserStatus(internalAdmin, RSA_USERNAME, UserDataConstants.STATUS_NEW);
        log.debug("Reset status of 'foo' to NEW");
        try {
            PKCS10RequestMessage p10 = new PKCS10RequestMessage(keytooldsa);
            p10.setUsername(RSA_USERNAME);
            p10.setPassword("foo123");
            ResponseMessage resp = signSession.createCertificate(internalAdmin, p10, X509ResponseMessage.class, null);
            Certificate cert = CertTools.getCertfromByteArray(resp.getResponseMessage());
            log.info("cert with DN '" + CertTools.getSubjectDN(cert) + "' should not be issued?");
        } catch (Exception e) {
            // RSASignSession should throw an IllegalKeyException here.
            assertTrue("Expected IllegalKeyException: " + e.toString(), e instanceof IllegalKeyException);
        }
        log.trace("<test07DSAKey()");
    }

    /**
     * test to set specific key usage
     * 
     * @throws Exception if an error occurs...
     */
    @Test
    public void testKeyUsage() throws Exception {
        log.trace(">test06KeyUsage()");

        userAdminSession.setUserStatus(internalAdmin, RSA_USERNAME, UserDataConstants.STATUS_NEW);
        log.debug("Reset status of 'foo' to NEW");

        int keyusage1 = X509KeyUsage.digitalSignature | X509KeyUsage.keyEncipherment;

        X509Certificate cert = (X509Certificate) signSession.createCertificate(internalAdmin, RSA_USERNAME, "foo123", rsakeys.getPublic(), keyusage1,
                null, null);
        assertNotNull("Failed to create certificate", cert);
        log.debug("Cert=" + cert.toString());
        boolean[] retKU = cert.getKeyUsage();
        assertTrue("Fel KeyUsage, digitalSignature finns ej!", retKU[0]);
        assertTrue("Fel KeyUsage, keyEncipherment finns ej!", retKU[2]);
        assertTrue("Fel KeyUsage, cRLSign finns!", !retKU[6]);

        userAdminSession.setUserStatus(internalAdmin, RSA_USERNAME, UserDataConstants.STATUS_NEW);
        log.debug("Reset status of 'foo' to NEW");

        int keyusage2 = X509KeyUsage.keyCertSign | X509KeyUsage.cRLSign;

        X509Certificate cert1 = (X509Certificate) signSession.createCertificate(internalAdmin, RSA_USERNAME, "foo123", rsakeys.getPublic(),
                keyusage2, null, null);

        assertNotNull("Failed to create certificate", cert1);
        retKU = cert1.getKeyUsage();
        assertTrue("Fel KeyUsage, keyCertSign finns ej!", retKU[5]);
        assertTrue("Fel KeyUsage, cRLSign finns ej!", retKU[6]);
        assertTrue("Fel KeyUsage, digitalSignature finns!", !retKU[0]);

        log.debug("Cert=" + cert1.toString());
        log.trace("<test06KeyUsage()");
    }

    /**
     * tests ie pkcs10
     * 
     * @throws Exception
     *             if en error occurs...
     */
    @Test
    public void testIEPKCS10() throws Exception {
        log.trace(">test05TestIEPKCS10()");

        userAdminSession.setUserStatus(internalAdmin, RSA_USERNAME, UserDataConstants.STATUS_NEW);
        log.debug("Reset status of 'foo' to NEW");

        PKCS10RequestMessage p10 = new PKCS10RequestMessage(iep10);
        p10.setUsername(RSA_USERNAME);
        p10.setPassword("foo123");
        ResponseMessage resp = signSession.createCertificate(internalAdmin, p10, X509ResponseMessage.class, null);
        Certificate cert = CertTools.getCertfromByteArray(resp.getResponseMessage());
        assertNotNull("Failed to create certificate", cert);
        log.debug("Cert=" + cert.toString());
        log.trace("<test05TestIEPKCS10()");
    }

    /**
     * tests bouncy PKCS10
     * 
     */
    @Test
    public void testBCPKCS10() throws Exception {
        log.trace(">test03TestBCPKCS10()");
        userAdminSession.setUserStatus(internalAdmin, RSA_USERNAME, UserDataConstants.STATUS_NEW);
        log.debug("Reset status of 'foo' to NEW");
        // Create certificate request
        PKCS10CertificationRequest req = new PKCS10CertificationRequest("SHA1WithRSA", CertTools.stringToBcX509Name("C=SE, O=AnaTom, CN=foo"),
                rsakeys.getPublic(), new DERSet(), rsakeys.getPrivate());
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        DEROutputStream dOut = new DEROutputStream(bOut);
        dOut.writeObject(req);
        dOut.close();
        PKCS10CertificationRequest req2 = new PKCS10CertificationRequest(bOut.toByteArray());
        boolean verify = req2.verify();
        log.debug("Verify returned " + verify);
        assertTrue(verify);
        log.debug("CertificationRequest generated successfully.");
        byte[] bcp10 = bOut.toByteArray();
        PKCS10RequestMessage p10 = new PKCS10RequestMessage(bcp10);
        p10.setUsername(RSA_USERNAME);
        p10.setPassword("foo123");
        ResponseMessage resp = signSession.createCertificate(internalAdmin, p10, X509ResponseMessage.class, null);
        Certificate cert = CertTools.getCertfromByteArray(resp.getResponseMessage());
        assertNotNull("Failed to create certificate", cert);
        log.debug("Cert=" + cert.toString());
        // Verify error handling
        EndEntityInformation badUserData = new EndEntityInformation();
        int rsacaid = caSession.getCAInfo(internalAdmin, getTestCAName()).getCAId();
        badUserData.setCAId(rsacaid);
        p10 = new PKCS10RequestMessage(bcp10);
        try {
            signSession.createCertificate(internalAdmin, p10, X509ResponseMessage.class, badUserData);
            assertFalse("Was able to create certificate when it should have failed.", true);
        } catch (SignRequestException e) {
            log.info("Expected exception caught (no password supplied): " + e.getMessage());
        }
        log.trace("<test03TestBCPKCS10()");
    }

    /**
     * tests keytool pkcs10
     * 
     * @throws Exception
     *             if en error occurs...
     */
    @Test
    public void testKeytoolPKCS10() throws Exception {
        log.trace(">test04TestKeytoolPKCS10()");
        userAdminSession.setUserStatus(internalAdmin, RSA_USERNAME, UserDataConstants.STATUS_NEW);
        log.debug("Reset status of 'foo' to NEW");
        PKCS10RequestMessage p10 = new PKCS10RequestMessage(keytoolp10);
        p10.setUsername(RSA_USERNAME);
        p10.setPassword("foo123");
        ResponseMessage resp = signSession.createCertificate(internalAdmin, p10, X509ResponseMessage.class, null);
        Certificate cert = CertTools.getCertfromByteArray(resp.getResponseMessage());
        assertNotNull("Failed to create certificate", cert);
        log.debug("Cert=" + cert.toString());
        log.trace("<test04TestKeytoolPKCS10()");
    }

    private void createReverseEndEntity() throws Exception {
        CAInfo inforsareverse = null;
        try {
            inforsareverse = caSession.getCAInfo(internalAdmin, "TESTRSAREVERSE");
        } catch (CADoesntExistsException e) {
            CaTestCase.createTestRSAReverseCa(internalAdmin);
            inforsareverse = caSession.getCAInfo(internalAdmin, "TESTRSAREVERSE");
        }

        int rsareversecaid = inforsareverse.getCAId();
        if (!userAdminSession.existsUser(RSA_REVERSE_USERNAME)) {
            userAdminSession.addUser(internalAdmin, RSA_REVERSE_USERNAME, "foo123", "C=SE,O=AnaTom,CN=" + RSA_REVERSE_USERNAME, null,
                    "foo@anatom.se", false, SecConst.EMPTY_ENDENTITYPROFILE, CertificateProfileConstants.CERTPROFILE_FIXED_ENDUSER,
                    EndEntityTypes.ENDUSER.toEndEntityType(), SecConst.TOKEN_SOFT_PEM, 0, rsareversecaid);
            log.debug("created user: " + RSA_REVERSE_USERNAME + ", foo123, C=SE, O=AnaTom, CN=" + RSA_REVERSE_USERNAME);
        } else {
            log.info("User " + RSA_REVERSE_USERNAME + " already exists, resetting status.");
            EndEntityInformation userData = new EndEntityInformation("foorev", "C=SE,O=AnaTom,CN="+ RSA_REVERSE_USERNAME,
                    rsareversecaid, null, "foo@anatom.se", UserDataConstants.STATUS_NEW, EndEntityTypes.ENDUSER.toEndEntityType(),
                    SecConst.EMPTY_ENDENTITYPROFILE, CertificateProfileConstants.CERTPROFILE_FIXED_ENDUSER, null, null, SecConst.TOKEN_SOFT_PEM, 0,
                    null);
            userData.setPassword("foo123");
            userAdminSession.changeUser(internalAdmin, userData, false);
            log.debug("Reset status to NEW");
        }
    }

    /**
     * Tests multiple instances of one altName
     * 
     */
    @Test
    public void testTestMultipleAltNames() throws Exception {
        log.trace(">test09TestMultipleAltNames()");
        // Create a good end entity profile (good enough), allowing multiple UPN
        // names
        final String multipleAltNameEndEntityProfileName = "TESTMULALTNAME";
        endEntityProfileSession.removeEndEntityProfile(internalAdmin, multipleAltNameEndEntityProfileName);
        EndEntityProfile profile = new EndEntityProfile();
        profile.addField(DnComponents.ORGANIZATION);
        profile.addField(DnComponents.COUNTRY);
        profile.addField(DnComponents.COMMONNAME);
        profile.addField(DnComponents.UNIFORMRESOURCEID);
        profile.addField(DnComponents.DNSNAME);
        profile.addField(DnComponents.DNSNAME);
        profile.addField(DnComponents.RFC822NAME);
        profile.addField(DnComponents.IPADDRESS);
        profile.addField(DnComponents.UPN);
        profile.addField(DnComponents.UPN);
        profile.setValue(EndEntityProfile.AVAILCAS, 0, Integer.toString(SecConst.ALLCAS));
        endEntityProfileSession.addEndEntityProfile(internalAdmin, multipleAltNameEndEntityProfileName, profile);
        try {
            int eeprofile = endEntityProfileSession.getEndEntityProfileId(multipleAltNameEndEntityProfileName);
            int rsacaid = caSession.getCAInfo(internalAdmin, getTestCAName()).getCAId();
            // Change a user that we know...
            EndEntityInformation userData = new EndEntityInformation(RSA_USERNAME,  "C=SE,O=AnaTom,CN=foo",
                    rsacaid, "uniformResourceId=http://www.a.se/,upn=foo@a.se,upn=foo@b.se,rfc822name=tomas@a.se,dNSName=www.a.se,dNSName=www.b.se,iPAddress=10.1.1.1", 
                    "foo@anatom.se", UserDataConstants.STATUS_NEW, EndEntityTypes.ENDUSER.toEndEntityType(),
                    eeprofile, CertificateProfileConstants.CERTPROFILE_FIXED_ENDUSER, null, null, SecConst.TOKEN_SOFT_PEM, 0,
                    null);
            userData.setPassword("foo123");
            userAdminSession.changeUser(internalAdmin, userData, false);   
            log.debug("created user: foo, foo123, C=SE, O=AnaTom, CN=foo");
            X509Certificate cert = (X509Certificate) signSession.createCertificate(internalAdmin, RSA_USERNAME, "foo123", rsakeys.getPublic());
            assertNotNull("Failed to create certificate", cert);
            String altNames = CertTools.getSubjectAlternativeName(cert);
            log.debug(altNames);
            ArrayList<String> list = CertTools.getPartsFromDN(altNames, CertTools.UPN);
            assertEquals(2, list.size());
            assertTrue(list.contains("foo@a.se"));
            assertTrue(list.contains("foo@b.se"));
            String name = CertTools.getPartFromDN(altNames, CertTools.URI);
            assertEquals("http://www.a.se/", name);
            name = CertTools.getPartFromDN(altNames, CertTools.EMAIL);
            assertEquals("tomas@a.se", name);
            list = CertTools.getPartsFromDN(altNames, CertTools.DNS);
            assertEquals(2, list.size());
            assertTrue(list.contains("www.a.se"));
            assertTrue(list.contains("www.b.se"));
            name = CertTools.getPartFromDN(altNames, CertTools.IPADDR);
            assertEquals("10.1.1.1", name);
            // Change a user that we know...
            EndEntityInformation endEntity = new EndEntityInformation(RSA_USERNAME,  "C=SE,O=AnaTom,CN=foo",
                    rsacaid, "uri=http://www.a.se/,upn=foo@a.se,upn=foo@b.se,rfc822name=tomas@a.se,dNSName=www.a.se,dNSName=www.b.se,iPAddress=10.1.1.1", 
                    "foo@anatom.se", UserDataConstants.STATUS_NEW, EndEntityTypes.ENDUSER.toEndEntityType(),
                    eeprofile, CertificateProfileConstants.CERTPROFILE_FIXED_ENDUSER, null, null, SecConst.TOKEN_SOFT_PEM, 0,
                    null);
            endEntity.setPassword("foo123");
            userAdminSession.changeUser(internalAdmin, endEntity, false);   
            log.debug("created user: foo, foo123, C=SE, O=AnaTom, CN=foo");
            cert = (X509Certificate) signSession.createCertificate(internalAdmin, RSA_USERNAME, "foo123", rsakeys.getPublic());
            assertNotNull("Failed to create certificate", cert);
            altNames = CertTools.getSubjectAlternativeName(cert);
            log.debug(altNames);
            list = CertTools.getPartsFromDN(altNames, CertTools.UPN);
            assertEquals(2, list.size());
            assertTrue(list.contains("foo@a.se"));
            assertTrue(list.contains("foo@b.se"));
            name = CertTools.getPartFromDN(altNames, CertTools.URI);
            assertEquals("http://www.a.se/", name);
            name = CertTools.getPartFromDN(altNames, CertTools.EMAIL);
            assertEquals("tomas@a.se", name);
            list = CertTools.getPartsFromDN(altNames, CertTools.DNS);
            assertEquals(2, list.size());
            assertTrue(list.contains("www.a.se"));
            assertTrue(list.contains("www.b.se"));
            name = CertTools.getPartFromDN(altNames, CertTools.IPADDR);
            assertEquals("10.1.1.1", name);
        } finally {
            // Clean up
            endEntityProfileSession.removeEndEntityProfile(internalAdmin, multipleAltNameEndEntityProfileName);
        }
        log.trace("<test09TestMultipleAltNames()");
    }

    /** Tests creating a certificate with QC statement */
    @Test
    public void testQcCert() throws Exception {
        log.trace(">test10TestQcCert()");
        final String qcCertProfileName = "TESTQC";
        final String qcCertEndEntityName = "TESTQC";
        // Create a good certificate profile (good enough), using QC statement
        certificateProfileSession.removeCertificateProfile(internalAdmin, qcCertProfileName);
        final CertificateProfile certprof = new CertificateProfile(CertificateProfileConstants.CERTPROFILE_FIXED_ENDUSER);
        certprof.setUseQCStatement(true);
        certprof.setQCStatementRAName("rfc822Name=qc@primekey.se");
        certprof.setUseQCEtsiQCCompliance(true);
        certprof.setUseQCEtsiSignatureDevice(true);
        certprof.setUseQCEtsiValueLimit(true);
        certprof.setQCEtsiValueLimit(50000);
        certprof.setQCEtsiValueLimitCurrency("SEK");
        certificateProfileSession.addCertificateProfile(internalAdmin, qcCertProfileName, certprof);
        int cprofile = certificateProfileSession.getCertificateProfileId(qcCertProfileName);
        // Create a good end entity profile (good enough), allowing multiple UPN
        // names
        endEntityProfileSession.removeEndEntityProfile(internalAdmin, qcCertProfileName);
        EndEntityProfile profile = new EndEntityProfile();
        profile.addField(DnComponents.COUNTRY);
        profile.addField(DnComponents.COMMONNAME);
        profile.setValue(EndEntityProfile.AVAILCAS, 0, Integer.toString(SecConst.ALLCAS));
        profile.setValue(EndEntityProfile.AVAILCERTPROFILES, 0, Integer.toString(cprofile));
        endEntityProfileSession.addEndEntityProfile(internalAdmin, qcCertProfileName, profile);
        int eeprofile = endEntityProfileSession.getEndEntityProfileId(qcCertProfileName);
        int rsacaid = caSession.getCAInfo(internalAdmin, getTestCAName()).getCAId();
        KeyPair anotheKey = KeyTools.genKeys("1024", AlgorithmConstants.KEYALGORITHM_RSA);
        createEndEntity(qcCertEndEntityName, eeprofile, cprofile, rsacaid);
        try {
            // Change a user that we know...
            EndEntityInformation endEntity = new EndEntityInformation(qcCertEndEntityName,  "C=SE,CN=qc",
                    rsacaid, null, 
                    "foo@anatom.nu", UserDataConstants.STATUS_NEW, EndEntityTypes.ENDUSER.toEndEntityType(),
                    eeprofile, cprofile, null, null, SecConst.TOKEN_SOFT_PEM, 0,
                    null);
            endEntity.setPassword("foo123");
            userAdminSession.changeUser(internalAdmin, endEntity, false); 
            log.debug("created user: foo, foo123, C=SE, CN=qc");

            X509Certificate cert = (X509Certificate) signSession.createCertificate(internalAdmin, qcCertEndEntityName, "foo123", anotheKey.getPublic());
            assertNotNull("Failed to create certificate", cert);
            String dn = cert.getSubjectDN().getName();
            assertEquals(CertTools.stringToBCDNString("cn=qc,c=SE"), CertTools.stringToBCDNString(dn));
            assertEquals("rfc822name=qc@primekey.se", QCStatementExtension.getQcStatementAuthorities(cert));
            Collection<String> ids = QCStatementExtension.getQcStatementIds(cert);
            assertTrue(ids.contains(RFC3739QCObjectIdentifiers.id_qcs_pkixQCSyntax_v1.getId()));
            assertTrue(ids.contains(ETSIQCObjectIdentifiers.id_etsi_qcs_QcCompliance.getId()));
            assertTrue(ids.contains(ETSIQCObjectIdentifiers.id_etsi_qcs_QcSSCD.getId()));
            assertTrue(ids.contains(ETSIQCObjectIdentifiers.id_etsi_qcs_LimiteValue.getId()));
            String limit = QCStatementExtension.getQcStatementValueLimit(cert);
            assertEquals("50000 SEK", limit);
        } finally {
            // Clean up
            endEntityProfileSession.removeEndEntityProfile(internalAdmin, qcCertProfileName);
            certificateProfileSession.removeCertificateProfile(internalAdmin, qcCertProfileName);
            userAdminSession.deleteUser(internalAdmin, qcCertEndEntityName);
        }
        log.trace("<test10TestQcCert()");
    }

    /**
     * Tests creating a certificate with QC statement
     * 
     */
    @Test
    public void testTestValidityOverride() throws Exception {
        log.trace(">test11TestValidityOverrideAndCardNumber()");
        final String validityOverrideProfileName = "TESTVALOVERRIDE";
        final String validityOverrideEndEntityName = "TESTVALOVERRIDE";
        // Create a good certificate profile (good enough), using QC statement
        certificateProfileSession.removeCertificateProfile(internalAdmin, validityOverrideProfileName);
        final CertificateProfile certprof = new CertificateProfile(CertificateProfileConstants.CERTPROFILE_FIXED_ENDUSER);
        certprof.setAllowValidityOverride(false);
        certprof.setValidity(298);
        certprof.setUseCardNumber(true);
        certificateProfileSession.addCertificateProfile(internalAdmin, validityOverrideProfileName, certprof);
        int cprofile = certificateProfileSession.getCertificateProfileId(validityOverrideProfileName);
        // Create a good end entity profile (good enough), allowing multiple UPN
        // names
        endEntityProfileSession.removeEndEntityProfile(internalAdmin, validityOverrideProfileName);
        EndEntityProfile profile = new EndEntityProfile();
        profile.addField(DnComponents.COUNTRY);
        profile.addField(DnComponents.COMMONNAME);
        profile.setValue(EndEntityProfile.AVAILCAS, 0, Integer.toString(SecConst.ALLCAS));
        profile.setValue(EndEntityProfile.AVAILCERTPROFILES, 0, Integer.toString(cprofile));
        profile.setUse(EndEntityProfile.CARDNUMBER, 0, true);
        endEntityProfileSession.addEndEntityProfile(internalAdmin, validityOverrideProfileName, profile);
        int eeprofile = endEntityProfileSession.getEndEntityProfileId(validityOverrideProfileName);
        // Change a user that we know...
        int rsacaid = caSession.getCAInfo(internalAdmin, getTestCAName()).getCAId();
        createEndEntity(validityOverrideEndEntityName, eeprofile, cprofile, rsacaid);
        try {
           
            EndEntityInformation user = new EndEntityInformation(validityOverrideEndEntityName, "C=SE,CN=validityoverride", rsacaid, null, "foo@anatom.nu",
                    new EndEntityType(EndEntityTypes.ENDUSER), eeprofile, cprofile, SecConst.TOKEN_SOFT_PEM, 0, null);
            user.setPassword("foo123");
            user.setStatus(UserDataConstants.STATUS_NEW);
            user.setCardNumber("123456789");
            userAdminSession.changeUser(internalAdmin, user, false);
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, 10);
            KeyPair anotherKey = KeyTools.genKeys("1024", AlgorithmConstants.KEYALGORITHM_RSA);
            X509Certificate cert = (X509Certificate) signSession.createCertificate(internalAdmin, validityOverrideEndEntityName, "foo123", anotherKey.getPublic(), -1,
                    null, cal.getTime());
            assertNotNull("Failed to create certificate", cert);
            String dn = cert.getSubjectDN().getName();
            assertEquals(CertTools.stringToBCDNString("cn=validityoverride,c=SE"), CertTools.stringToBCDNString(dn));
            Date notAfter = cert.getNotAfter();
            cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, 297);
            // Override was not enabled, the cert should have notAfter more than 297
            // days in the future (298 to be exact)
            assertTrue(notAfter.compareTo(cal.getTime()) > 0);
            cal.add(Calendar.DAY_OF_MONTH, 2);
            // Override was not enabled, the cert should have notAfter less than 299
            // days in the future (298 to be exact)
            assertTrue(notAfter.compareTo(cal.getTime()) < 0);
            // Check card number extension as well
            String cardNumber = SeisCardNumberExtension.getSeisCardNumber(cert);
            assertEquals("123456789", cardNumber);
            // Change so that we allow override of validity time
            CertificateProfile prof = certificateProfileSession.getCertificateProfile(cprofile);
            prof.setAllowValidityOverride(true);
            prof.setValidity(3065);
            prof.setUseCardNumber(false);
            certificateProfileSession.changeCertificateProfile(internalAdmin, validityOverrideProfileName, prof);
            cal = Calendar.getInstance();
            Calendar notBefore = Calendar.getInstance();
            notBefore.add(Calendar.DAY_OF_MONTH, 2);
            cal.add(Calendar.DAY_OF_MONTH, 10);
            userAdminSession.setUserStatus(internalAdmin, validityOverrideEndEntityName, UserDataConstants.STATUS_NEW);
            cert = (X509Certificate) signSession.createCertificate(internalAdmin, validityOverrideEndEntityName, "foo123", anotherKey.getPublic(), -1,
                    notBefore.getTime(), cal.getTime());
            assertNotNull("Failed to create certificate", cert);
            assertEquals(CertTools.stringToBCDNString("cn=validityoverride,c=SE"), CertTools.stringToBCDNString(dn));
            notAfter = cert.getNotAfter();
            cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, 11);
            // Override was enabled, the cert should have notAfter less than 11 days
            // in the future (10 to be exact)
            assertTrue(notAfter.compareTo(cal.getTime()) < 0);
            notAfter = cert.getNotBefore();
            cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, 1);
            // Override was enabled, the cert should have notBefore more than 1 days
            // in the future (2 to be exact)
            assertTrue(notAfter.compareTo(cal.getTime()) > 0);
            cal.add(Calendar.DAY_OF_MONTH, 2);
            assertTrue(notAfter.compareTo(cal.getTime()) < 0);

            // Check that card number extension is not present
            cardNumber = SeisCardNumberExtension.getSeisCardNumber(cert);
            assertNull(cardNumber);

            // Verify that we can not get a certificate that has notBefore before the
            // current time
            // and that we can not get a certificate valid longer than the
            // certificate profile allows.
            prof = certificateProfileSession.getCertificateProfile(cprofile);
            prof.setValidity(50);
            certificateProfileSession.changeCertificateProfile(internalAdmin, validityOverrideProfileName, prof);
            notBefore = Calendar.getInstance();
            notBefore.add(Calendar.DAY_OF_MONTH, -2);
            cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, 200);
            userAdminSession.setUserStatus(internalAdmin, validityOverrideEndEntityName, UserDataConstants.STATUS_NEW);
            cert = (X509Certificate) signSession.createCertificate(internalAdmin, validityOverrideEndEntityName, "foo123", anotherKey.getPublic(), -1,
                    notBefore.getTime(), cal.getTime());
            assertNotNull("Failed to create certificate", cert);
            assertEquals(CertTools.stringToBCDNString("cn=validityoverride,c=SE"), CertTools.stringToBCDNString(dn));
            Date certNotBefore = cert.getNotBefore();
            // Override was enabled, and we can not get a certificate valid before
            // current time
            cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, -1);
            // the certificate should be valid 2 days before current date...
            assertTrue(certNotBefore.compareTo(cal.getTime()) < 0);
            cal.add(Calendar.DAY_OF_MONTH, -2);
            assertTrue(certNotBefore.compareTo(cal.getTime()) > 0);
            cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, 47);
            notAfter = cert.getNotAfter();
            // Override was enabled, the cert should have notAfter more than 47 days
            // in the future (50 days starting from -2 days since notBefore was set
            // before current date)
            // since we requested 200 and validity is 50
            assertTrue(notAfter.compareTo(cal.getTime()) > 0);
            cal.add(Calendar.DAY_OF_MONTH, 2);
            // Since we are not allowed to request validity longer than the
            // certificate profile allows, validity is less than 51 days, even
            // though we requested 200
            assertTrue(notAfter.compareTo(cal.getTime()) < 0);
            // Clean up
        } finally {
            endEntityProfileSession.removeEndEntityProfile(internalAdmin, validityOverrideProfileName);
            certificateProfileSession.removeCertificateProfile(internalAdmin, validityOverrideProfileName);
            userAdminSession.deleteUser(internalAdmin, validityOverrideEndEntityName);
        }
        log.trace("<test11TestValidityOverride()");
    }

    /**
     * Tests Swedish characters
     * 
     */
   @Test
    public void testSwedishCharacters() throws Exception {
        log.trace(">test08SwedeChars()");
        // Make user that we know...
        final String username = "swede";
        KeyPair swedeKey = KeyTools.genKeys("1024", AlgorithmConstants.KEYALGORITHM_RSA);
        int rsacaid = caSession.getCAInfo(internalAdmin, getTestCAName()).getCAId();
        if (!userAdminSession.existsUser(username)) {
            // We use unicode encoding for the three Swedish character åäö
            userAdminSession.addUser(internalAdmin, username, "foo123", "C=SE, O=\u00E5\u00E4\u00F6, CN=\u00E5\u00E4\u00F6", null, username
                    + "@anatom.se", false, SecConst.EMPTY_ENDENTITYPROFILE, CertificateProfileConstants.CERTPROFILE_FIXED_ENDUSER,
                    new EndEntityType(EndEntityTypes.ENDUSER), SecConst.TOKEN_SOFT_PEM, 0, rsacaid);
            log.debug("created user: " + username + ", foo123, C=SE, O=\u00E5\u00E4\u00F6, CN=\u00E5\u00E4\u00F6");
        } else {
            log.debug("user " + username + " already exists: " + username + ", foo123, C=SE, O=\u00E5\u00E4\u00F6, CN=\u00E5\u00E4\u00F6");
            userAdminSession.setUserStatus(internalAdmin, username, UserDataConstants.STATUS_NEW);
            log.debug("Reset status to NEW");
        }
        try {
            // user that we know exists...; use new key so that the check that
            // two
            // don't prevent the creation of the certificate.
            X509Certificate cert = (X509Certificate) signSession.createCertificate(internalAdmin, username, "foo123", swedeKey.getPublic());
            assertNotNull("Failed to create certificate", cert);
            log.debug("Cert=" + cert.toString());
            assertEquals("Wrong DN with Swedish characters", CertTools.stringToBCDNString("C=SE, O=\u00E5\u00E4\u00F6, CN=\u00E5\u00E4\u00F6"),
                    CertTools.getSubjectDN(cert));
        } finally {
            userAdminSession.deleteUser(internalAdmin, username);
        }
    }

   @Test
    public void testDnOrder() throws Exception {
        log.trace(">test22DnOrder()");
        final String profileName = "TESTDNORDER";
        final String endEntityName = "TESTDNORDER";
        // Create a good certificate profile (good enough), using QC statement
        certificateProfileSession.removeCertificateProfile(internalAdmin, profileName);
        final CertificateProfile certprof = new CertificateProfile(CertificateProfileConstants.CERTPROFILE_FIXED_ENDUSER);
        certificateProfileSession.addCertificateProfile(internalAdmin, profileName, certprof);
        int cprofile = certificateProfileSession.getCertificateProfileId(profileName);

        // Create a good end entity profile (good enough), allowing multiple UPN
        // names
        endEntityProfileSession.removeEndEntityProfile(internalAdmin, profileName);
        EndEntityProfile profile = new EndEntityProfile();
        profile.addField(DnComponents.COUNTRY);
        profile.addField(DnComponents.ORGANIZATION);
        profile.addField(DnComponents.COMMONNAME);
        profile.setValue(EndEntityProfile.AVAILCAS, 0, Integer.toString(SecConst.ALLCAS));
        profile.setValue(EndEntityProfile.AVAILCERTPROFILES, 0, Integer.toString(cprofile));
        endEntityProfileSession.addEndEntityProfile(internalAdmin, profileName, profile);
        KeyPair anotherKey = KeyTools.genKeys("1024", AlgorithmConstants.KEYALGORITHM_RSA);
        int rsacaid = caSession.getCAInfo(internalAdmin, getTestCAName()).getCAId();
        int eeprofile = endEntityProfileSession.getEndEntityProfileId(profileName);
        createEndEntity(endEntityName, eeprofile, cprofile, rsacaid);
        try {
    
            EndEntityInformation user = new EndEntityInformation(endEntityName, "C=SE,O=PrimeKey,CN=dnorder", rsacaid, null, "foo@primekey.se",
                    new EndEntityType(EndEntityTypes.ENDUSER), eeprofile, cprofile, SecConst.TOKEN_SOFT_PEM, 0, null);
            user.setStatus(UserDataConstants.STATUS_NEW);
            // Change a user that we know...
            userAdminSession.changeUser(internalAdmin, user, false);
            log.debug("created user: foo, foo123, C=SE,O=PrimeKey,CN=dnorder");
            X509Certificate cert = (X509Certificate) signSession.createCertificate(internalAdmin, endEntityName, "foo123", anotherKey.getPublic());
            assertNotNull("Failed to create certificate", cert);
            String dn = cert.getSubjectDN().getName();
            // This is the reverse order than what is displayed by openssl
            assertEquals("C=SE, O=PrimeKey, CN=dnorder", dn);

            // Change to X509 DN order
            certprof.setUseLdapDnOrder(false);
            certificateProfileSession.changeCertificateProfile(internalAdmin, profileName, certprof);
            userAdminSession.changeUser(internalAdmin, user, false);
            cert = (X509Certificate) signSession.createCertificate(internalAdmin, endEntityName, "foo123", anotherKey.getPublic());
            assertNotNull("Failed to create certificate", cert);
            dn = cert.getSubjectDN().getName();
            // This is the reverse order than what is displayed by openssl
            assertEquals("CN=dnorder, O=PrimeKey, C=SE", dn);
        } finally {
            // Clean up
            endEntityProfileSession.removeEndEntityProfile(internalAdmin, profileName);
            certificateProfileSession.removeCertificateProfile(internalAdmin, profileName);
            userAdminSession.deleteUser(internalAdmin, endEntityName);
        }
        log.trace("<test22DnOrder()");
    }

    @Test
    public void testOfflineCA() throws Exception {
        // user that we know exists...
        userAdminSession.setUserStatus(internalAdmin, RSA_USERNAME, UserDataConstants.STATUS_NEW);
        X509Certificate cert = (X509Certificate) signSession.createCertificate(internalAdmin, RSA_USERNAME, "foo123", rsakeys.getPublic());
        assertNotNull("Failed to create certificate", cert);
        // Set CA to offline
        int rsacaid = caSession.getCAInfo(internalAdmin, getTestCAName()).getCAId();
        CAInfo inforsa = caSession.getCAInfo(internalAdmin, rsacaid);
        inforsa.setStatus(CAConstants.CA_OFFLINE);
        caAdminSession.editCA(internalAdmin, inforsa);
        userAdminSession.setUserStatus(internalAdmin, RSA_USERNAME, UserDataConstants.STATUS_NEW);
        boolean thrown = false;
        try {
            cert = (X509Certificate) signSession.createCertificate(internalAdmin, RSA_USERNAME, "foo123", rsakeys.getPublic());
        } catch (Exception e) {
            thrown = true;
        }
        assertTrue(thrown);
        inforsa.setStatus(CAConstants.CA_ACTIVE);
        caAdminSession.editCA(internalAdmin, inforsa);
    }
    
    @Test
    public void testCertReqHistory() throws Exception {        
        int rsacaid = caSession.getCAInfo(internalAdmin, getTestCAName()).getCAId();
        // Configure CA not to store certreq history
        CAInfo cainfo = caSession.getCAInfo(internalAdmin, rsacaid);
        cainfo.setUseCertReqHistory(true);
        cainfo.setDoEnforceUniquePublicKeys(false);
        caAdminSession.editCA(internalAdmin, cainfo);
        // New random username and create cert
        String username = genRandomUserName();
        userAdminSession.addUser(internalAdmin, username, "foo123", "C=SE,O=AnaTom,CN=" + username, null, "foo@anatom.se", false, SecConst.EMPTY_ENDENTITYPROFILE,
                CertificateProfileConstants.CERTPROFILE_FIXED_ENDUSER, new EndEntityType(EndEntityTypes.ENDUSER), SecConst.TOKEN_SOFT_PEM, 0, rsacaid);
        X509Certificate cert = (X509Certificate) signSession.createCertificate(internalAdmin, username, "foo123", rsakeys.getPublic());
        assertNotNull("Failed to create certificate", cert);
        // Check that certreq history was created
        List<CertReqHistory> history = certReqHistorySession.retrieveCertReqHistory(internalAdmin, username);
        assertEquals(1, history.size());
        userAdminSession.deleteUser(internalAdmin, username);
        // Configure CA not to store certreq history
        cainfo.setUseCertReqHistory(false);
        caAdminSession.editCA(internalAdmin, cainfo);
        // New random username and create cert
        username = genRandomUserName();
        userAdminSession.addUser(internalAdmin, username, "foo123", "C=SE,O=AnaTom,CN=" + username, null, "foo@anatom.se", false, SecConst.EMPTY_ENDENTITYPROFILE,
                CertificateProfileConstants.CERTPROFILE_FIXED_ENDUSER, new EndEntityType(EndEntityTypes.ENDUSER), SecConst.TOKEN_SOFT_PEM, 0, rsacaid);
        cert = (X509Certificate) signSession.createCertificate(internalAdmin, username, "foo123", rsakeys.getPublic());
        assertNotNull("Failed to create certificate", cert);
        // Check that certreq history was not created
        history = certReqHistorySession.retrieveCertReqHistory(internalAdmin, username);
        assertEquals(0, history.size());
        userAdminSession.deleteUser(internalAdmin, username);
        // Reset CA info
        cainfo.setUseCertReqHistory(true);
        caAdminSession.editCA(internalAdmin, cainfo);
    }
    
    /**
     * Test several cases where certificate generation should fail.
     */
    @Test
    public void testCertCreationErrorHandling() throws Exception {
        log.trace(">test33certCreationErrorHandling");
        log.debug("Trying to use a certificate that isn't selfsigned for certificate renewal.");
        userAdminSession.setUserStatus(internalAdmin, RSA_USERNAME, UserDataConstants.STATUS_NEW);
        KeyPair anotherRsaKey = KeyTools.genKeys("1024", AlgorithmConstants.KEYALGORITHM_RSA);
        final X509Certificate notSelfSignedCert = CertTools.genSelfCert("CN=notSelfSigned", 1, null, rsakeys.getPrivate(), anotherRsaKey.getPublic(),
                AlgorithmConstants.SIGALG_SHA1_WITH_RSA, false);
        try {
            signSession.createCertificate(internalAdmin, RSA_USERNAME, "foo123", notSelfSignedCert);
            assertFalse("Tried to create cert from old certificate that wasn't self signed! Did not throw SignRequestSignatureException.", true);
        } catch (SignRequestSignatureException e) {
            log.info("Got expected exception: " + e.getMessage());
        }
        log.trace("<test33certCreationErrorHandling");
    }
    
    @Test
    public void testProfileSignatureAlgorithm() throws Exception {
        // Create a good certificate profile (good enough), using QC statement
        certificateProfileSession.removeCertificateProfile(internalAdmin, "TESTSIGALG");
        final CertificateProfile certprof = new CertificateProfile(CertificateProfileConstants.CERTPROFILE_FIXED_ENDUSER);
        // Default profile uses "inherit from CA"
        certificateProfileSession.addCertificateProfile(internalAdmin, "TESTSIGALG", certprof);
        int cprofile = certificateProfileSession.getCertificateProfileId("TESTSIGALG");
        // Create a good end entity profile (good enough)
        endEntityProfileSession.removeEndEntityProfile(internalAdmin, "TESTSIGALG");
        EndEntityProfile profile = new EndEntityProfile();
        profile.addField(DnComponents.COUNTRY);
        profile.addField(DnComponents.COMMONNAME);
        profile.setValue(EndEntityProfile.AVAILCAS, 0, Integer.toString(SecConst.ALLCAS));
        profile.setValue(EndEntityProfile.AVAILCERTPROFILES, 0, Integer.toString(cprofile));
        endEntityProfileSession.addEndEntityProfile(internalAdmin, "TESTSIGALG", profile);
        int eeprofile = endEntityProfileSession.getEndEntityProfileId("TESTSIGALG");
        int rsacaid = caSession.getCAInfo(internalAdmin, getTestCAName()).getCAId();
        EndEntityInformation user = new EndEntityInformation(RSA_USERNAME, "C=SE,CN=testsigalg", rsacaid, null, "foo@anatom.nu", new EndEntityType(EndEntityTypes.ENDUSER), eeprofile, cprofile,
                SecConst.TOKEN_SOFT_PEM, 0, null);
        user.setPassword("foo123");
        user.setStatus(UserDataConstants.STATUS_NEW);
        // Change a user that we know...
        userAdminSession.changeUser(internalAdmin, user, false);
        // Create a P10
        // Create PKCS#10 certificate request
        PKCS10CertificationRequest req = new PKCS10CertificationRequest("SHA1WithRSA", new X509Name("C=SE,CN=testsigalg"), rsakeys.getPublic(), null, rsakeys
                .getPrivate());
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        DEROutputStream dOut = new DEROutputStream(bOut);
        dOut.writeObject(req);
        dOut.close();
        byte[] p10bytes = bOut.toByteArray();
        PKCS10RequestMessage p10 = new PKCS10RequestMessage(p10bytes);
        p10.setUsername(RSA_USERNAME);
        p10.setPassword("foo123");
        // See if the request message works...
        ResponseMessage resp = signSession.createCertificate(internalAdmin, p10, X509ResponseMessage.class, null);
        X509Certificate cert = (X509Certificate) CertTools.getCertfromByteArray(resp.getResponseMessage());
        assertNotNull("Failed to create certificate", cert);
        assertEquals("CN=testsigalg,C=SE", cert.getSubjectDN().getName());
        assertEquals(AlgorithmConstants.SIGALG_SHA1_WITH_RSA, AlgorithmTools.getSignatureAlgorithm(cert));
        // Change so that we can override signature algorithm
        CertificateProfile prof = certificateProfileSession.getCertificateProfile(cprofile);
        prof.setSignatureAlgorithm(AlgorithmConstants.SIGALG_SHA256_WITH_RSA);
        certificateProfileSession.changeCertificateProfile(internalAdmin, "TESTSIGALG", prof);
        userAdminSession.changeUser(internalAdmin, user, false);
        resp = signSession.createCertificate(internalAdmin, p10, X509ResponseMessage.class, null);
        cert = (X509Certificate) CertTools.getCertfromByteArray(resp.getResponseMessage());
        assertNotNull("Failed to create certificate", cert);
        assertEquals("CN=testsigalg,C=SE", cert.getSubjectDN().getName());
        assertEquals(AlgorithmConstants.SIGALG_SHA256_WITH_RSA, AlgorithmTools.getSignatureAlgorithm(cert));
    } 
    
    @Test
    public void testExtensionOverride() throws Exception {
        final String altnames = "dNSName=foo1.bar.com,dNSName=foo2.bar.com,dNSName=foo3.bar.com,dNSName=foo4.bar.com,dNSName=foo5.bar.com,dNSName=foo6.bar.com,dNSName=foo7.bar.com,"
                +"dNSName=foo8.bar.com,dNSName=foo9.bar.com,dNSName=foo10.bar.com,dNSName=foo11.bar.com,dNSName=foo12.bar.com,dNSName=foo13.bar.com,dNSName=foo14.bar.com,"
                +"dNSName=foo15.bar.com,dNSName=foo16.bar.com,dNSName=foo17.bar.com,dNSName=foo18.bar.com,dNSName=foo19.bar.com,dNSName=foo20.bar.com,dNSName=foo21.bar.com";
        // Create a good certificate profile (good enough), using QC statement
        certificateProfileSession.removeCertificateProfile(internalAdmin, "TESTEXTENSIONOVERRIDE");
        final CertificateProfile certprof = new CertificateProfile(CertificateProfileConstants.CERTPROFILE_FIXED_ENDUSER);
        // Default profile does not allow Extension override
        certprof.setValidity(298);
        certificateProfileSession.addCertificateProfile(internalAdmin, "TESTEXTENSIONOVERRIDE", certprof);
        int cprofile = certificateProfileSession.getCertificateProfileId("TESTEXTENSIONOVERRIDE");
        // Create a good end entity profile (good enough), allowing multiple UPN
        // names
        endEntityProfileSession.removeEndEntityProfile(internalAdmin, "TESTEXTENSIONOVERRIDE");
        EndEntityProfile profile = new EndEntityProfile();
        profile.addField(DnComponents.COUNTRY);
        profile.addField(DnComponents.COMMONNAME);
        profile.setValue(EndEntityProfile.AVAILCAS, 0, Integer.toString(SecConst.ALLCAS));
        profile.setValue(EndEntityProfile.AVAILCERTPROFILES, 0, Integer.toString(cprofile));
        endEntityProfileSession.addEndEntityProfile(internalAdmin, "TESTEXTENSIONOVERRIDE", profile);
        int eeprofile = endEntityProfileSession.getEndEntityProfileId("TESTEXTENSIONOVERRIDE");
        int rsacaid = caSession.getCAInfo(internalAdmin, getTestCAName()).getCAId();
        EndEntityInformation user = new EndEntityInformation(RSA_USERNAME, "C=SE,CN=extoverride", rsacaid, null, "foo@anatom.nu", new EndEntityType(EndEntityTypes.ENDUSER), eeprofile, cprofile,
                SecConst.TOKEN_SOFT_PEM, 0, null);
        user.setPassword("foo123");
        user.setStatus(UserDataConstants.STATUS_NEW);
        // Change a user that we know...
        userAdminSession.changeUser(internalAdmin, user, false);
        // Create a P10 with extensions, in this case altNames with a lot of DNS
        // names
        ASN1EncodableVector extensionattr = new ASN1EncodableVector();
        extensionattr.add(PKCSObjectIdentifiers.pkcs_9_at_extensionRequest);
        GeneralNames san = CertTools.getGeneralNamesFromAltName(altnames);
        ByteArrayOutputStream extOut = new ByteArrayOutputStream();
        DEROutputStream derOut = new DEROutputStream(extOut);
        try {
            derOut.writeObject(san);
        } catch (IOException e) {
            throw new IllegalArgumentException("error encoding value: " + e);
        }
        Vector<DERObjectIdentifier> oidvec = new Vector<DERObjectIdentifier>();
        oidvec.add(X509Extensions.SubjectAlternativeName);
        Vector<X509Extension> valuevec = new Vector<X509Extension>();
        valuevec.add(new X509Extension(false, new DEROctetString(extOut.toByteArray())));
        X509Extensions exts = new X509Extensions(oidvec, valuevec);
        extensionattr.add(new DERSet(exts));
        // Complete the Attribute section of the request, the set (Attributes)
        // contains one sequence (Attribute)
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(new DERSequence(extensionattr));
        DERSet attributes = new DERSet(v);
        // Create PKCS#10 certificate request
        PKCS10CertificationRequest req = new PKCS10CertificationRequest("SHA1WithRSA", new X509Name("C=SE,CN=extoverride"), rsakeys.getPublic(), attributes,
                rsakeys.getPrivate());
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        DEROutputStream dOut = new DEROutputStream(bOut);
        dOut.writeObject(req);
        dOut.close();
        byte[] p10bytes = bOut.toByteArray();
        // FileOutputStream fos = new FileOutputStream("/tmp/foo.der");
        // fos.write(p10bytes);
        // fos.close();
        PKCS10RequestMessage p10 = new PKCS10RequestMessage(p10bytes);
        p10.setUsername(RSA_USERNAME);
        p10.setPassword("foo123");
        // See if the request message works...
        X509Extensions p10exts = p10.getRequestExtensions();
        assertNotNull(p10exts);
        ResponseMessage resp = signSession.createCertificate(internalAdmin, p10, X509ResponseMessage.class, null);
        X509Certificate cert = (X509Certificate) CertTools.getCertfromByteArray(resp.getResponseMessage());
        assertNotNull("Failed to create certificate", cert);
        assertEquals("CN=extoverride,C=SE", cert.getSubjectDN().getName());
        // check altNames, should be none
        Collection<List<?>> c = cert.getSubjectAlternativeNames();
        assertNull(c);
        // Change so that we allow override of validity time
        CertificateProfile prof = certificateProfileSession.getCertificateProfile(cprofile);
        prof.setAllowExtensionOverride(true);
        certificateProfileSession.changeCertificateProfile(internalAdmin, "TESTEXTENSIONOVERRIDE", prof);
        userAdminSession.changeUser(internalAdmin, user, false);
        resp = signSession.createCertificate(internalAdmin, p10, X509ResponseMessage.class, null);
        cert = (X509Certificate) CertTools.getCertfromByteArray(resp.getResponseMessage());
        assertNotNull("Failed to create certificate", cert);
        assertEquals("CN=extoverride,C=SE", cert.getSubjectDN().getName());
        // check altNames, should be one altName
        c = cert.getSubjectAlternativeNames();
        assertNotNull(c);
        assertEquals(21, c.size());
        String retAltNames = CertTools.getSubjectAlternativeName(cert);
        List<String> originalNames = Arrays.asList(altnames.split(","));
        List<String> returnNames = Arrays.asList(retAltNames.split(", "));
        assertTrue(originalNames.containsAll(returnNames));
    } 
    
    /**
     * creates cert
     * 
     */
    @Test
    public void testMultiRequests() throws Exception {
        log.trace(">test20MultiRequests()");
        // Test that it works correctly with end entity profiles using the
        // counter
        int pid = 0;
        int rsacaid = caSession.getCAInfo(internalAdmin, getTestCAName()).getCAId();
        EndEntityProfile profile = new EndEntityProfile();
        profile.addField(DnComponents.ORGANIZATION);
        profile.addField(DnComponents.COUNTRY);
        profile.addField(DnComponents.COMMONNAME);
        profile.setValue(EndEntityProfile.AVAILCAS, 0, "" + rsacaid);
        profile.setUse(EndEntityProfile.ALLOWEDREQUESTS, 0, true);
        profile.setValue(EndEntityProfile.ALLOWEDREQUESTS, 0, "3");
        endEntityProfileSession.addEndEntityProfile(internalAdmin, "TESTREQUESTCOUNTER", profile);
        pid = endEntityProfileSession.getEndEntityProfileId("TESTREQUESTCOUNTER");
        // Change already existing user
        EndEntityInformation user = new EndEntityInformation(RSA_USERNAME, "C=SE,O=AnaTom,CN=foo", rsacaid, null, null, new EndEntityType(
                EndEntityTypes.ENDUSER), pid, CertificateProfileConstants.CERTPROFILE_FIXED_ENDUSER, SecConst.TOKEN_SOFT_PEM, 0, null);
        userAdminSession.changeUser(internalAdmin, user, false);
        userAdminSession.setUserStatus(internalAdmin, RSA_USERNAME, UserDataConstants.STATUS_NEW);
        // create first cert
        X509Certificate cert = (X509Certificate) signSession.createCertificate(internalAdmin, RSA_USERNAME, "foo123", rsakeys.getPublic());
        assertNotNull("Failed to create cert", cert);
        // log.debug("Cert=" + cert.toString());
        // Normal DN order
        assertEquals(cert.getSubjectX500Principal().getName(), "C=SE,O=AnaTom,CN=foo");
        try {
            X509Certificate rsacacert = (X509Certificate) caSession.getCAInfo(internalAdmin, getTestCAName()).getCertificateChain().toArray()[0]; 
            cert.verify(rsacacert.getPublicKey());
        } catch (Exception e) {
            assertTrue("Verify failed: " + e.getMessage(), false);
        }
        // It should only work once, not twice times
        boolean authstatus = false;
        try {
            cert = (X509Certificate) signSession.createCertificate(internalAdmin, RSA_USERNAME, "foo123", rsakeys.getPublic());
        } catch (AuthStatusException e) {
            authstatus = true;
        }
        assertTrue("Should have failed to create cert", authstatus);
        // Change already existing user to add extended information with counter
        ExtendedInformation ei = new ExtendedInformation();
        int allowedrequests = 2;
        ei.setCustomData(ExtendedInformationFields.CUSTOM_REQUESTCOUNTER, String.valueOf(allowedrequests));
        user.setExtendedinformation(ei);
        user.setStatus(UserDataConstants.STATUS_NEW);
        userAdminSession.changeUser(internalAdmin, user, false);
        // create first cert
        cert = (X509Certificate) signSession.createCertificate(internalAdmin, RSA_USERNAME, "foo123", rsakeys.getPublic());
        assertNotNull("Failed to create cert", cert);
        // log.debug("Cert=" + cert.toString());
        // Normal DN order
        assertEquals(cert.getSubjectX500Principal().getName(), "C=SE,O=AnaTom,CN=foo");
        X509Certificate rsacacert = (X509Certificate) caSession.getCAInfo(internalAdmin, getTestCAName()).getCertificateChain().toArray()[0]; 
        try {
            cert.verify(rsacacert.getPublicKey());
        } catch (Exception e) {
            assertTrue("Verify failed: " + e.getMessage(), false);
        }
        String serno = cert.getSerialNumber().toString(16);
        // It should work to get two certificates
        cert = (X509Certificate) signSession.createCertificate(internalAdmin, RSA_USERNAME, "foo123", rsakeys.getPublic());
        assertNotNull("Failed to create cert", cert);
        // log.debug("Cert=" + cert.toString());
        // Normal DN order
        assertEquals(cert.getSubjectX500Principal().getName(), "C=SE,O=AnaTom,CN=foo");
        try {
            cert.verify(rsacacert.getPublicKey());
        } catch (Exception e) {
            assertTrue("Verify failed: " + e.getMessage(), false);
        }
        String serno1 = cert.getSerialNumber().toString(16);
        assertFalse(serno1.equals(serno));
        // It should only work twice, not three times
        authstatus = false;
        try {
            cert = (X509Certificate) signSession.createCertificate(internalAdmin, RSA_USERNAME, "foo123", rsakeys.getPublic());
        } catch (AuthStatusException e) {
            authstatus = true;
        }
        assertTrue("Should have failed to create cert", authstatus);

        log.trace("<test20MultiRequests()");
    }
    
    @Test
    public void testDNOverride() throws Exception {
        // Create a good certificate profile (good enough), using QC statement
        certificateProfileSession.removeCertificateProfile(internalAdmin, "TESTDNOVERRIDE");
        final CertificateProfile certprof = new CertificateProfile(CertificateProfileConstants.CERTPROFILE_FIXED_ENDUSER);
        // Default profile does not allow DN override
        certprof.setValidity(298);
        certificateProfileSession.addCertificateProfile(internalAdmin, "TESTDNOVERRIDE", certprof);
        int cprofile = certificateProfileSession.getCertificateProfileId("TESTDNOVERRIDE");
        // Create a good end entity profile (good enough), allowing multiple UPN
        // names
        endEntityProfileSession.removeEndEntityProfile(internalAdmin, "TESTDNOVERRIDE");
        EndEntityProfile profile = new EndEntityProfile();
        profile.addField(DnComponents.COUNTRY);
        profile.addField(DnComponents.COMMONNAME);
        profile.setValue(EndEntityProfile.AVAILCAS, 0, Integer.toString(SecConst.ALLCAS));
        profile.setValue(EndEntityProfile.AVAILCERTPROFILES, 0, Integer.toString(cprofile));
        endEntityProfileSession.addEndEntityProfile(internalAdmin, "TESTDNOVERRIDE", profile);
        int eeprofile = endEntityProfileSession.getEndEntityProfileId("TESTDNOVERRIDE");
        int rsacaid = caSession.getCAInfo(internalAdmin, getTestCAName()).getCAId();
        final String dnOverrideEndEntityName = "DnOverride";
        createEndEntity(dnOverrideEndEntityName, eeprofile, cprofile, rsacaid);
        try {
            EndEntityInformation user = new EndEntityInformation(dnOverrideEndEntityName, "C=SE,CN=dnoverride", rsacaid, null, "foo@anatom.nu",
                    new EndEntityType(EndEntityTypes.ENDUSER), eeprofile, cprofile, SecConst.TOKEN_SOFT_PEM, 0, null);
            user.setPassword("foo123");
            user.setStatus(UserDataConstants.STATUS_NEW);
            // Change a user that we know...
            userAdminSession.changeUser(internalAdmin, user, false);
            // Create a P10 with strange order DN
            PKCS10CertificationRequest req = new PKCS10CertificationRequest("SHA1WithRSA", new X509Name("CN=foo,C=SE, Name=AnaTom, O=My org"),
                    rsakeys.getPublic(), new DERSet(), rsakeys.getPrivate());
            ByteArrayOutputStream bOut = new ByteArrayOutputStream();
            DEROutputStream dOut = new DEROutputStream(bOut);
            dOut.writeObject(req);
            dOut.close();
            PKCS10CertificationRequest req2 = new PKCS10CertificationRequest(bOut.toByteArray());
            boolean verify = req2.verify();
            log.debug("Verify returned " + verify);
            assertTrue(verify);
            log.debug("CertificationRequest generated successfully.");
            byte[] bcp10 = bOut.toByteArray();
            PKCS10RequestMessage p10 = new PKCS10RequestMessage(bcp10);
            p10.setUsername(dnOverrideEndEntityName);
            p10.setPassword("foo123");
            ResponseMessage resp = signSession.createCertificate(internalAdmin, p10, X509ResponseMessage.class, null);
            X509Certificate cert = (X509Certificate) CertTools.getCertfromByteArray(resp.getResponseMessage());
            assertNotNull("Failed to create certificate", cert);
            assertEquals("CN=dnoverride,C=SE", cert.getSubjectDN().getName());
            // Change so that we allow override of validity time
            CertificateProfile prof = certificateProfileSession.getCertificateProfile(cprofile);
            prof.setAllowDNOverride(true);
            certificateProfileSession.changeCertificateProfile(internalAdmin, "TESTDNOVERRIDE", prof);
            userAdminSession.changeUser(internalAdmin, user, false);
            resp = signSession.createCertificate(internalAdmin, p10, X509ResponseMessage.class, null);
            cert = (X509Certificate) CertTools.getCertfromByteArray(resp.getResponseMessage());
            assertNotNull("Failed to create certificate", cert);
            assertEquals("CN=foo,C=SE,Name=AnaTom,O=My org", cert.getSubjectDN().getName());
        } finally {
            userAdminSession.deleteUser(internalAdmin, dnOverrideEndEntityName);
        }
    }


   @Test
    public void testCVCertificateRsaKeys() throws Exception {
        createDefaultCvcRsaCA();
        final String cvcEndEntityName = "cvc";
        try {
            log.trace(">test21CVCertificate()");
            KeyPair rsakeys = KeyTools.genKeys("1024", AlgorithmConstants.KEYALGORITHM_RSA);
            CAInfo infocvcca = caSession.getCAInfo(internalAdmin, TEST_CVC_RSA_CA_NAME); //"TESTDV-D"
            int cvccaid = infocvcca.getCAId();
            EndEntityInformation user = new EndEntityInformation(cvcEndEntityName, "C=SE,CN=TESTCVC", cvccaid, null, null, new EndEntityType(EndEntityTypes.ENDUSER),
                    SecConst.EMPTY_ENDENTITYPROFILE, CertificateProfileConstants.CERTPROFILE_FIXED_ENDUSER, SecConst.TOKEN_SOFT_PEM, 0, null);
            user.setPassword("cvc");
            userAdminSession.addUser(internalAdmin, user, false);
            userAdminSession.setUserStatus(internalAdmin, cvcEndEntityName, UserDataConstants.STATUS_NEW);
            userAdminSession.setPassword(internalAdmin, cvcEndEntityName, "foo123");
            log.debug("Reset status of 'cvc' to NEW");
            // user that we know exists...
            Certificate cert = (Certificate) signSession.createCertificate(internalAdmin, "cvc", "foo123", rsakeys.getPublic());
            assertNotNull("Failed to create cert", cert);
            log.debug("Cert=" + cert.toString());
            // Normal DN order
            assertEquals(CertTools.getSubjectDN(cert), "CN=TESTCVC,C=SE");
            assertEquals("CVC", cert.getType());
            Certificate cvccacert = (Certificate) infocvcca.getCertificateChain().toArray()[0];
            assertEquals(CertTools.getIssuerDN(cert), CertTools.getSubjectDN(cvccacert));
            try {
                cert.verify(cvccacert.getPublicKey());
            } catch (Exception e) {
                assertTrue("Verify failed: " + e.getMessage(), false);
            }
            CardVerifiableCertificate cvcert = (CardVerifiableCertificate) cert;
            String role = cvcert.getCVCertificate().getCertificateBody().getAuthorizationTemplate().getAuthorizationField().getRole().name();
            assertEquals("IS", role);
            PublicKey pk = cvcert.getPublicKey();
            if (pk instanceof RSAPublicKey) {
                RSAPublicKey epk = (RSAPublicKey) pk;
                assertEquals(epk.getAlgorithm(), "RSA");
                int len = KeyTools.getKeyLength(epk);
                assertEquals(1024, len);
            } else {
                assertTrue("Public key is not RSA", false);
            }
        } finally {
            removeTestCA(TEST_CVC_RSA_CA_NAME);
            userAdminSession.deleteUser(internalAdmin, cvcEndEntityName);
        }
    }
    
    @Test
    public void testsignSessionDSAWithRSACA() throws Exception {
        log.trace(">test23SignSessionDSAWithRSACA()");
        userAdminSession.setUserStatus(internalAdmin, RSA_USERNAME, UserDataConstants.STATUS_NEW);
        log.debug("Reset status of 'foo' to NEW");
        // user that we know exists...
        KeyPair dsakeys = KeyTools.genKeys("1024", AlgorithmConstants.KEYALGORITHM_DSA);
        X509Certificate selfcert = CertTools.genSelfCert("CN=selfsigned", 1, null, dsakeys.getPrivate(), dsakeys.getPublic(),
                AlgorithmConstants.SIGALG_SHA1_WITH_DSA, false);
        X509Certificate cert = (X509Certificate) signSession.createCertificate(internalAdmin, RSA_USERNAME, "foo123", selfcert);
        assertNotNull("Failed to create certificate", cert);
        log.debug("Cert=" + cert.toString());
        PublicKey pk = cert.getPublicKey();
        if (pk instanceof DSAPublicKey) {
            DSAPublicKey ecpk = (DSAPublicKey) pk;
            assertEquals(ecpk.getAlgorithm(), "DSA");
        } else {
            fail("Public key is not DSA");
        }
        try {
            X509Certificate rsacacert = (X509Certificate) caSession.getCAInfo(internalAdmin, getTestCAName()).getCertificateChain().toArray()[0]; 
            cert.verify(rsacacert.getPublicKey());
        } catch (Exception e) {
            fail("Verification failed: " + e.getMessage());
        }
        log.trace("<test23SignSessionDSAWithRSACA()");
    }
    
    @Test
    public void testBCPKCS10DSAWithRSACA() throws Exception {
        log.trace(">test24TestBCPKCS10DSAWithRSACA()");

        userAdminSession.setUserStatus(internalAdmin, RSA_USERNAME, UserDataConstants.STATUS_NEW);
        log.debug("Reset status of 'foo' to NEW");
        // Create certificate request
        KeyPair dsakeys = KeyTools.genKeys("1024", AlgorithmConstants.KEYALGORITHM_DSA);
        PKCS10CertificationRequest req = new PKCS10CertificationRequest("SHA1WithDSA", CertTools.stringToBcX509Name("C=SE, O=AnaTom, CN=foo"), dsakeys
                .getPublic(), new DERSet(), dsakeys.getPrivate());
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        DEROutputStream dOut = new DEROutputStream(bOut);
        dOut.writeObject(req);
        dOut.close();

        PKCS10CertificationRequest req2 = new PKCS10CertificationRequest(bOut.toByteArray());
        boolean verify = req2.verify();
        log.debug("Verify returned " + verify);
        assertTrue(verify);
        log.debug("CertificationRequest generated successfully.");
        byte[] bcp10 = bOut.toByteArray();
        PKCS10RequestMessage p10 = new PKCS10RequestMessage(bcp10);
        p10.setUsername(RSA_USERNAME);
        p10.setPassword("foo123");
        ResponseMessage resp = signSession.createCertificate(internalAdmin, p10, X509ResponseMessage.class, null);
        Certificate cert = CertTools.getCertfromByteArray(resp.getResponseMessage());
        assertNotNull("Failed to create certificate", cert);
        log.debug("Cert=" + cert.toString());
        PublicKey pk = cert.getPublicKey();
        if (pk instanceof DSAPublicKey) {
            DSAPublicKey dsapk = (DSAPublicKey) pk;
            assertEquals(dsapk.getAlgorithm(), "DSA");
        } else {
            fail("Public key is not DSA");
        }
        try {
            X509Certificate rsacacert = (X509Certificate) caSession.getCAInfo(internalAdmin, getTestCAName()).getCertificateChain().toArray()[0]; 
            cert.verify(rsacacert.getPublicKey());
        } catch (Exception e) {
            fail("Verify failed: " + e.getMessage());
        }
        log.trace("<test24TestBCPKCS10DSAWithRSACA()");
    }
    
    @Override
    public String getRoleName() {
        return RsaSignSessionTest.class.getSimpleName();
    }

}
