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

package se.anatom.ejbca.ca.sign;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.rmi.RemoteException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.DuplicateKeyException;
import javax.naming.Context;
import javax.naming.NamingException;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.bouncycastle.asn1.DEROutputStream;
import org.ejbca.core.ejb.ca.caadmin.ICAAdminSessionHome;
import org.ejbca.core.ejb.ca.caadmin.ICAAdminSessionRemote;
import org.ejbca.core.ejb.ca.sign.ISignSessionHome;
import org.ejbca.core.ejb.ca.sign.ISignSessionRemote;
import org.ejbca.core.ejb.ra.IUserAdminSessionHome;
import org.ejbca.core.ejb.ra.IUserAdminSessionRemote;
import org.ejbca.core.ejb.ra.raadmin.IRaAdminSessionHome;
import org.ejbca.core.ejb.ra.raadmin.IRaAdminSessionRemote;
import org.ejbca.core.model.SecConst;
import org.ejbca.core.model.ca.IllegalKeyException;
import org.ejbca.core.model.log.Admin;
import org.ejbca.core.model.ra.UserDataConstants;
import org.ejbca.core.model.ra.raadmin.EndEntityProfile;
import org.ejbca.core.protocol.ExtendedPKCS10CertificationRequest;
import org.ejbca.core.protocol.IResponseMessage;
import org.ejbca.core.protocol.PKCS10RequestMessage;
import org.ejbca.util.Base64;
import org.ejbca.util.CertTools;


/**
 * Tests signing session.
 *
 * @version $Id: TestSignSession.java,v 1.9 2006-01-17 20:33:58 anatom Exp $
 */
public class TestSignSession extends TestCase {
    static byte[] keytoolp10 = Base64.decode(("MIIBbDCB1gIBADAtMQ0wCwYDVQQDEwRUZXN0MQ8wDQYDVQQKEwZBbmFUb20xCzAJBgNVBAYTAlNF" +
            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDY+ATE4ZB0oKfmXStu8J+do0GhTag6rOGtoydI" +
            "eNX9DdytlsmXDyONKl8746478/3HXdx9rA0RevUizKSataMpDsb3TjprRjzBTvYPZSIfzko6s8g6" +
            "AZLO07xCFOoDmyRzb9k/KEZsMls0ujx79CQ9p5K4rg2ksjmDeW7DaPMphQIDAQABoAAwDQYJKoZI" +
            "hvcNAQEFBQADgYEAyJVobqn6wGRoEsdHxjoqPXw8fLrQyBGEwXccnVpI4kv9iIZ45Xres0LrOwtS" +
            "kFLbpn0guEzhxPBbL6mhhmDDE4hbbHJp1Kh6gZ4Bmbb5FrwpvUyrSjTIwwRC7GAT00A1kOjl9jCC" +
            "XCfJkJH2QleCy7eKANq+DDTXzpEOvL/UqN0=").getBytes());
    static byte[] oldbcp10 = Base64.decode(("MIIBbDCB1gIBADAtMQswCQYDVQQGEwJTRTEPMA0GA1UEChMGQW5hVG9tMQ0wCwYDVQQDEwRUZXN0" +
            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCzN9nDdwmq23/RLGisvR3CRO9JSem2QZ7JC7nr" +
            "NlbxQBLVqlkypT/lxMMur+lTX1S+jBaqXjtirhZTVaV5C/+HObWZ5vrj30lmsCdgzFybSzVxBz0l" +
            "XC0UEDbgBml/hO70cSDdmyw3YE9g5eH3wdYs2FCTzexRF3kNAVHNUa8svwIDAQABoAAwDQYJKoZI" +
            "hvcNAQEFBQADgYEAm6uRSyEmyCcs652Ttg2npm6JZPFT2qwSl4dviyIKJbn6j+meCzvn2TMP10d8" +
            "7Ak5sv5NJew1XGkM4mGpF9cfcVshxLVlW+cgq0749fWbyS8KlgQP/ANh3DkLl8k5E+3Wnbi0JjCV" +
            "Xe1s44+K2solX8jOtryoR4TMJ6p9HpsuO68=").getBytes());
    static byte[] iep10 = Base64.decode(("MIICnTCCAgYCAQAwGzEZMBcGA1UEAxMQNkFFSzM0N2Z3OHZXRTQyNDCBnzANBgkq" +
            "hkiG9w0BAQEFAAOBjQAwgYkCgYEAukW70HN9bt5x2AiSZm7y8GXQuyp1jN2OIvqU" +
            "sr0dzLIOFt1H8GPJkL80wx3tLDj3xJfWJdww3TqExsxMSP+qScoYKIOeNBb/2OMW" +
            "p/k3DThCOewPebmt+M08AClq5WofXTG+YxyJgXWbMTNfXKIUyR0Ju4Spmg6Y4eJm" +
            "GXTG7ZUCAwEAAaCCAUAwGgYKKwYBBAGCNw0CAzEMFgo1LjAuMjE5NS4yMCAGCisG" +
            "AQQBgjcCAQ4xEjAQMA4GA1UdDwEB/wQEAwIE8DCB/wYKKwYBBAGCNw0CAjGB8DCB" +
            "7QIBAR5cAE0AaQBjAHIAbwBzAG8AZgB0ACAARQBuAGgAYQBuAGMAZQBkACAAQwBy" +
            "AHkAcAB0AG8AZwByAGEAcABoAGkAYwAgAFAAcgBvAHYAaQBkAGUAcgAgAHYAMQAu" +
            "ADADgYkAjuYPzZPpbLgCWYnXoNeX2gS6nuI4osrWHlQQKcS67VJclhELlnT3hBb9" +
            "Blr7I0BsJ/lguZvZFTZnC1bMeNULRg17bhExTg+nUovzPcJhMvG7G3DR17PrJ7V+" +
            "egHAsQV4dQC2hOGGhOnv88JhP9Pwpso3t2tqJROa5ZNRRSJSkw8AAAAAAAAAADAN" +
            "BgkqhkiG9w0BAQQFAAOBgQCL5k4bJt265j63qB/9GoQb1XFOPSar1BDFi+veCPA2" +
            "GJ/vRXt77Vcr4inx9M51iy87FNcGGsmyesBoDg73p06UxpIDhkL/WpPwZAfQhWGe" +
            "o/gWydmP/hl3uEfE0E4WG02UXtNwn3ziIiJM2pBCGQQIN2rFggyD+aTxwAwOU7Z2" + "fw==").getBytes());
    static byte[] openscep = Base64.decode(("MIIFSwYJKoZIhvcNAQcCoIIFPDCCBTgCAQExDjAMBggqhkiG9w0CBQUAMIICMwYJ" +
            "KoZIhvcNAQcBoIICJASCAiAwggIcBgkqhkiG9w0BBwOgggINMIICCQIBADGB1TCB" +
            "0gIBADA7MC8xDzANBgNVBAMTBlRlc3RDQTEPMA0GA1UEChMGQW5hVG9tMQswCQYD" +
            "VQQGEwJTRQIIbzEhUVZYO3gwDQYJKoZIhvcNAQEBBQAEgYDJP3tsx1KMC+Ws3gcV" +
            "gpvatMgxocUrKS2Z5BRj7z8HE/BySwa40fwzpBXq3xhakclrdK9D6Bb7I2oTqaNo" +
            "y25tk2ykow8px1HEerGg5eCIDeAwX4IGurKn+ajls4vWntybgtosAFPLuBO2sdfy" +
            "VhTv+iFxkl+lZgcRfpJhmqfOJjCCASoGCSqGSIb3DQEHATARBgUrDgMCBwQIapUt" +
            "FKgA/KmAggEIpzjb5ONkiT7gPs5VeQ6a2e3IdXMgZTRknqZZRRzRovKwp17LJPkA" +
            "AF9vQKCk6IQwM1dY4NAhu/mCvkfQwwVgML+rbsx7cYH5VuMxw6xw79CnGZbcgOoE" +
            "lhfYR9ytfZFAVjs8TF/cx1GfuxxN/3RdXzwIFmvPRX1SPh83ueMbGTHjmk0/kweE" +
            "9XcLkI85jTyG/Dsq3mUlWDS4qQg4sSbFAvkHgmCl0DQd2qW3eV9rCDbfPNjc+2dq" +
            "nG5EwjX1UVYS2TSWy7vu6MQvKtEWFP4B10+vGBcVE8fZ4IxL9TDQ4UMz3gfFIQSc" +
            "Moq4lw7YKmywbbyieGGYJuXDX/0gUBKj/MrP9s3L12bLoIIBajCCAWYwggEQoAMC" +
            "AQMCIDNGREQzNUM5NzZDODlENjcwRjNCM0IxOTgxQjhDMzA2MA0GCSqGSIb3DQEB" +
            "BAUAMCwxCzAJBgNVBAYTAlNFMQ8wDQYDVQQKEwZBbmFUb20xDDAKBgNVBAMTA2Zv" +
            "bzAeFw0wMzA2MTkwODQ3NDlaFw0wMzA3MTkwODQ3NDlaMCwxCzAJBgNVBAYTAlNF" +
            "MQ8wDQYDVQQKEwZBbmFUb20xDDAKBgNVBAMTA2ZvbzBcMA0GCSqGSIb3DQEBAQUA" +
            "A0sAMEgCQQDLfHDEOse6Mbi02egr2buI9mgWC0ur9dvGmLiIxmNg1TNhn1WHj5Zy" +
            "VsjKyLoVuVqgGRPYVA73ItANF8RNBAt9AgMBAAEwDQYJKoZIhvcNAQEEBQADQQCw" +
            "9kQsl3M0Ag1892Bu3izeZOYKpze64kJ7iGuYmN8atkdO8Rpp4Jn0W6vvUYQcat2a" +
            "Jzf6h3xfEQ7m8CzvaQ2/MYIBfDCCAXgCAQEwUDAsMQswCQYDVQQGEwJTRTEPMA0G" +
            "A1UEChMGQW5hVG9tMQwwCgYDVQQDEwNmb28CIDNGREQzNUM5NzZDODlENjcwRjNC" +
            "M0IxOTgxQjhDMzA2MAwGCCqGSIb3DQIFBQCggcEwEgYKYIZIAYb4RQEJAjEEEwIx" +
            "OTAYBgkqhkiG9w0BCQMxCwYJKoZIhvcNAQcBMBwGCSqGSIb3DQEJBTEPFw0wMzA2" +
            "MTkwODQ3NDlaMB8GCSqGSIb3DQEJBDESBBCevtHE4n3my5B7Q+MiKj04MCAGCmCG" +
            "SAGG+EUBCQUxEgQQwH1TAMlSzz1d3SNXoOARkTAwBgpghkgBhvhFAQkHMSITIDNG" +
            "REQzNUM5NzZDODlENjcwRjNCM0IxOTgxQjhDMzA2MA0GCSqGSIb3DQEBAQUABEAW" +
            "r+9YB3t1750Aj4bm5JAHv80VhzkrPmVLZqsJdC2DGn3UQFp1FhXo4od2xGpeg+pZ" +
            "b0B6kUt+uxvuq3PbagLi").getBytes());
    static byte[] keytooldsa = Base64.decode(("MIICNjCCAfQCAQAwMTERMA8GA1UEAxMIRFNBIFRlc3QxDzANBgNVBAoTBkFuYXRvbTELMAkGA1UE" +
            "BhMCU0UwggG4MIIBLAYHKoZIzjgEATCCAR8CgYEA/X9TgR11EilS30qcLuzk5/YRt1I870QAwx4/" +
            "gLZRJmlFXUAiUftZPY1Y+r/F9bow9subVWzXgTuAHTRv8mZgt2uZUKWkn5/oBHsQIsJPu6nX/rfG" +
            "G/g7V+fGqKYVDwT7g/bTxR7DAjVUE1oWkTL2dfOuK2HXKu/yIgMZndFIAccCFQCXYFCPFSMLzLKS" +
            "uYKi64QL8Fgc9QKBgQD34aCF1ps93su8q1w2uFe5eZSvu/o66oL5V0wLPQeCZ1FZV4661FlP5nEH" +
            "EIGAtEkWcSPoTCgWE7fPCTKMyKbhPBZ6i1R8jSjgo64eK7OmdZFuo38L+iE1YvH7YnoBJDvMpPG+" +
            "qFGQiaiD3+Fa5Z8GkotmXoB7VSVkAUw7/s9JKgOBhQACgYEAiVCUaC95mHaU3C9odWcuJ8j3fT6z" +
            "bSR02CVFC0F6QO5s2Tx3JYWrm5aAjWkXWJfeYOR6qBSwX0R1US3rDI0Kepsrdco2q7wGSo+235KL" +
            "Yfl7tQ9RLOKUGX/1c5+XuvN1ZbGy0yUw3Le16UViahWmmx6FM1sW6M48U7C/CZOyoxagADALBgcq" +
            "hkjOOAQDBQADLwAwLAIUQ+S2iFA1y7dfDWUCg7j1Nc8RW0oCFFhnDlU69xFRMeXXn1C/Oi+8pwrQ").getBytes());
    private static Logger log = Logger.getLogger(TestSignSession.class);
    private static Context ctx;
    private static ISignSessionHome home;
    private static ISignSessionRemote remote;
    private static IUserAdminSessionRemote usersession;
    private static IRaAdminSessionRemote rasession;
    private static KeyPair keys;
    private static int caid = 0;
    private Admin admin;

    /**
     * Creates a new TestSignSession object.
     *
     * @param name name
     */
    public TestSignSession(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        log.debug(">setUp()");

        // Install BouncyCastle provider
        CertTools.installBCProvider();

        admin = new Admin(Admin.TYPE_BATCHCOMMANDLINE_USER);

        ctx = getInitialContext();
        Object obj = ctx.lookup("CAAdminSession");
        ICAAdminSessionHome cahome = (ICAAdminSessionHome) javax.rmi.PortableRemoteObject.narrow(obj, ICAAdminSessionHome.class);
        ICAAdminSessionRemote casession = cahome.create();
        Collection caids = casession.getAvailableCAs(admin);
        Iterator iter = caids.iterator();
        if (iter.hasNext()) {
            caid = ((Integer) iter.next()).intValue();
        } else {
            assertTrue("No active CA! Must have at least one active CA to run tests!", false);
        }
        obj = ctx.lookup("RSASignSession");
        home = (ISignSessionHome) javax.rmi.PortableRemoteObject.narrow(obj, ISignSessionHome.class);
        remote = home.create();

        obj = ctx.lookup("UserAdminSession");
        IUserAdminSessionHome userhome = (IUserAdminSessionHome) javax.rmi.PortableRemoteObject.narrow(obj, IUserAdminSessionHome.class);
        usersession = userhome.create();
        
        obj = ctx.lookup("RaAdminSession");
        IRaAdminSessionHome rahome = (IRaAdminSessionHome) javax.rmi.PortableRemoteObject.narrow(obj, IRaAdminSessionHome.class);
        rasession = rahome.create();

        log.debug("<setUp()");
    }

    protected void tearDown() throws Exception {
    }

    private Context getInitialContext() throws NamingException {
        log.debug(">getInitialContext");

        Context ctx = new javax.naming.InitialContext();
        log.debug("<getInitialContext");

        return ctx;
    }

    /**
     * Generates a RSA key pair.
     *
     * @return KeyPair the generated key pair
     *
     * @throws Exception if en error occurs...
     */
    private static KeyPair genKeys() throws Exception {
        KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA", "BC");
        keygen.initialize(512);
        log.debug("Generating keys, please wait...");
        KeyPair rsaKeys = keygen.generateKeyPair();
        log.debug("Generated " + rsaKeys.getPrivate().getAlgorithm() + " keys with length" +
                ((RSAPrivateKey) rsaKeys.getPrivate()).getModulus().bitLength());

        return rsaKeys;
    } // genKeys

    /**
     * creates new user
     *
     * @throws Exception if en error occurs...
     */
    public void test01CreateNewUser() throws Exception {
        log.debug(">test01CreateNewUser()");

        // Make user that we know...
        boolean userExists = false;
        try {
            usersession.addUser(admin,"foo","foo123","C=SE,O=AnaTom,CN=foo",null,"foo@anatom.se",false,SecConst.EMPTY_ENDENTITYPROFILE,SecConst.CERTPROFILE_FIXED_ENDUSER,SecConst.USER_ENDUSER,SecConst.TOKEN_SOFT_PEM,0,caid);
            log.debug("created user: foo, foo123, C=SE, O=AnaTom, CN=foo");
        } catch (RemoteException re) {
            if (re.detail instanceof DuplicateKeyException) {
                userExists = true;
            }
        } catch (DuplicateKeyException dke) {
            userExists = true;
        }
        if (userExists) {
            log.info("User foo already exists, resetting status.");
            usersession.setUserStatus(admin,"foo",UserDataConstants.STATUS_NEW);
            log.debug("Reset status to NEW");
        }

        log.debug("<test01CreateNewUser()");
    }

    /**
     * creates cert
     *
     * @throws Exception if en error occurs...
     */
    public void test02SignSession() throws Exception {
        log.debug(">test02SignSession()");
        keys = genKeys();

        // user that we know exists...
        X509Certificate cert = (X509Certificate) remote.createCertificate(admin, "foo", "foo123", keys.getPublic());
        assertNotNull("Misslyckades skapa cert", cert);
        log.debug("Cert=" + cert.toString());

        //FileOutputStream fos = new FileOutputStream("testcert.crt");
        //fos.write(cert.getEncoded());
        //fos.close();
        log.debug("<test02SignSession()");
    }

    /**
     * tests bouncy PKCS10
     *
     * @throws Exception if en error occurs...
     */
    public void test03TestBCPKCS10() throws Exception {
        log.debug(">test03TestBCPKCS10()");
        usersession.setUserStatus(admin,"foo",UserDataConstants.STATUS_NEW);
        log.debug("Reset status of 'foo' to NEW");
        // Create certificate request
        ExtendedPKCS10CertificationRequest req = new ExtendedPKCS10CertificationRequest("SHA1WithRSA",
                CertTools.stringToBcX509Name("C=SE, O=AnaTom, CN=foo"), keys.getPublic(), null,
                keys.getPrivate());
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        DEROutputStream dOut = new DEROutputStream(bOut);
        dOut.writeObject(req);
        dOut.close();

        ExtendedPKCS10CertificationRequest req2 = new ExtendedPKCS10CertificationRequest(bOut.toByteArray());
        boolean verify = req2.verify();
        log.debug("Verify returned " + verify);
        if (verify == false) {
            log.debug("Aborting!");

            return;
        }
        log.debug("CertificationRequest generated successfully.");
        byte[] bcp10 = bOut.toByteArray();
        PKCS10RequestMessage p10 = new PKCS10RequestMessage(bcp10);
        p10.setUsername("foo");
        p10.setPassword("foo123");
        IResponseMessage resp = remote.createCertificate(admin,
                p10, Class.forName("org.ejbca.core.protocol.X509ResponseMessage"));
        X509Certificate cert = CertTools.getCertfromByteArray(resp.getResponseMessage());
        assertNotNull("Failed to create certificate", cert);
        log.debug("Cert=" + cert.toString());
        log.debug("<test03TestBCPKCS10()");
    }

    /**
     * tests keytool pkcs10
     *
     * @throws Exception if en error occurs...
     */
    public void test04TestKeytoolPKCS10() throws Exception {
        log.debug(">test04TestKeytoolPKCS10()");

        usersession.setUserStatus(admin,"foo",UserDataConstants.STATUS_NEW);
        log.debug("Reset status of 'foo' to NEW");

        PKCS10RequestMessage p10 = new PKCS10RequestMessage(keytoolp10);
        p10.setUsername("foo");
        p10.setPassword("foo123");
        IResponseMessage resp = remote.createCertificate(admin,
                p10, Class.forName("org.ejbca.core.protocol.X509ResponseMessage"));
        X509Certificate cert = CertTools.getCertfromByteArray(resp.getResponseMessage());
        assertNotNull("Failed to create certificate", cert);
        log.debug("Cert=" + cert.toString());
        log.debug("<test04TestKeytoolPKCS10()");
    }

    /**
     * tests ie pkcs10
     *
     * @throws Exception if en error occurs...
     */
    public void test05TestIEPKCS10() throws Exception {
        log.debug(">test05TestIEPKCS10()");

        usersession.setUserStatus(admin,"foo",UserDataConstants.STATUS_NEW);
        log.debug("Reset status of 'foo' to NEW");

        PKCS10RequestMessage p10 = new PKCS10RequestMessage(iep10);
        p10.setUsername("foo");
        p10.setPassword("foo123");
        IResponseMessage resp = remote.createCertificate(admin,
                p10, Class.forName("org.ejbca.core.protocol.X509ResponseMessage"));
        X509Certificate cert = CertTools.getCertfromByteArray(resp.getResponseMessage());
        assertNotNull("Failed to create certificate", cert);
        log.debug("Cert=" + cert.toString());
        log.debug("<test05TestIEPKCS10()");
    }

    /**
     * test to set specific key usage
     *
     * @throws Exception if en error occurs...
     */
    public void test06KeyUsage() throws Exception {
        log.debug(">test06KeyUsage()");

        usersession.setUserStatus(admin,"foo",UserDataConstants.STATUS_NEW);
        log.debug("Reset status of 'foo' to NEW");

        // Create an array for KeyUsage acoording to X509Certificate.getKeyUsage()
        boolean[] keyusage1 = new boolean[9];
        Arrays.fill(keyusage1, false);
        // digitalSignature
        keyusage1[0] = true;
        // keyEncipherment
        keyusage1[2] = true;

        X509Certificate cert = (X509Certificate) remote.createCertificate(admin, "foo", "foo123", keys.getPublic(), keyusage1);
        assertNotNull("Misslyckades skapa cert", cert);
        log.debug("Cert=" + cert.toString());
        boolean[] retKU = cert.getKeyUsage();
        assertTrue("Fel KeyUsage, digitalSignature finns ej!", retKU[0]);
        assertTrue("Fel KeyUsage, keyEncipherment finns ej!", retKU[2]);
        assertTrue("Fel KeyUsage, cRLSign finns!", !retKU[6]);

        usersession.setUserStatus(admin,"foo",UserDataConstants.STATUS_NEW);
        log.debug("Reset status of 'foo' to NEW");

        boolean[] keyusage2 = new boolean[9];
        Arrays.fill(keyusage2, false);
        // keyCertSign
        keyusage2[5] = true;
        // cRLSign
        keyusage2[6] = true;

        X509Certificate cert1 = (X509Certificate) remote.createCertificate(admin, "foo", "foo123", keys.getPublic(), keyusage2);
        assertNotNull("Misslyckades skapa cert", cert1);
        retKU = cert1.getKeyUsage();
        assertTrue("Fel KeyUsage, keyCertSign finns ej!", retKU[5]);
        assertTrue("Fel KeyUsage, cRLSign finns ej!", retKU[6]);
        assertTrue("Fel KeyUsage, digitalSignature finns!", !retKU[0]);

        log.debug("Cert=" + cert1.toString());
        log.debug("<test06KeyUsage()");
    }

    /**
     * test DSA keys instead of RSA
     *
     * @throws Exception if en error occurs...
     */
    public void test07DSAKey() throws Exception {
        log.debug(">test07DSAKey()");

        usersession.setUserStatus(admin,"foo",UserDataConstants.STATUS_NEW);
        log.debug("Reset status of 'foo' to NEW");

        try {
            PKCS10RequestMessage p10 = new PKCS10RequestMessage(keytooldsa);
            p10.setUsername("foo");
            p10.setPassword("foo123");
            IResponseMessage resp = remote.createCertificate(admin,
                    p10, Class.forName("org.ejbca.core.protocol.X509ResponseMessage"));
            X509Certificate cert = CertTools.getCertfromByteArray(resp.getResponseMessage());
            log.info("cert with DN '"+cert.getSubjectDN().getName()+"' should not be issued?");
        } catch (Exception e) {
            // RSASignSession should throw an IllegalKeyException here.
            assertTrue("Expected IllegalKeyException: " + e.toString(),
                    e instanceof IllegalKeyException);
        }

        log.debug("<test07DSAKey()");
    }

    /**
     * Tests international characters
     *
     * @throws Exception if en error occurs...
     */
    public void test08SwedeChars() throws Exception {
        log.debug(">test08SwedeChars()");
        // Make user that we know...
        boolean userExists = false;
        try {
            usersession.addUser(admin,"swede","foo123","C=SE, O=���, CN=���",null,"swede@anatom.se",false,SecConst.EMPTY_ENDENTITYPROFILE,SecConst.CERTPROFILE_FIXED_ENDUSER,SecConst.USER_ENDUSER,SecConst.TOKEN_SOFT_PEM,0,caid);
            log.debug("created user: swede, foo123, C=SE, O=���, CN=���");
        } catch (RemoteException re) {
            if (re.detail instanceof DuplicateKeyException) {
                userExists = true;
            }
        } catch (DuplicateKeyException dke) {
            userExists = true;
        }
        if (userExists) {
            log.debug("user swede already exists.");

            usersession.setUserStatus(admin,"swede",UserDataConstants.STATUS_NEW);
            log.debug("Reset status to NEW");
        }

        keys = genKeys();
        // user that we know exists...
        X509Certificate cert = (X509Certificate) remote.createCertificate(admin, "swede", "foo123", keys.getPublic());
        assertNotNull("Failed to create certificate", cert);
        log.debug("Cert=" + cert.toString());
        assertEquals("Wrong DN med swedechars", CertTools.getSubjectDN(cert),
                CertTools.stringToBCDNString("C=SE, O=���, CN=���"));
        //FileOutputStream fos = new FileOutputStream("swedecert.crt");
        //fos.write(cert.getEncoded());
        //fos.close();
        log.debug("<test08SwedeChars()");
    }


    /** Tests multiple instances of one altName
     * 
     */
    public void test09TestMultipleAltNames() throws Exception {
        log.debug(">test09TestMultipleAltNames()");

        // Create a good end entity profile (good enough), allowing multiple UPN names
        rasession.removeEndEntityProfile(admin, "TESTMULALTNAME");
        EndEntityProfile profile = new EndEntityProfile();
        profile.addField(EndEntityProfile.ORGANIZATION);
        profile.addField(EndEntityProfile.COUNTRY);
        profile.addField(EndEntityProfile.COMMONNAME);
        profile.addField(EndEntityProfile.UNIFORMRESOURCEID);
        profile.addField(EndEntityProfile.DNSNAME);
        profile.addField(EndEntityProfile.DNSNAME);
        profile.addField(EndEntityProfile.RFC822NAME);
        profile.addField(EndEntityProfile.IPADDRESS);
        profile.addField(EndEntityProfile.UPN);
        profile.addField(EndEntityProfile.UPN);
        profile.setValue(EndEntityProfile.AVAILCAS,0, Integer.toString(SecConst.ALLCAS));
        rasession.addEndEntityProfile(admin, "TESTMULALTNAME", profile);
        int eeprofile = rasession.getEndEntityProfileId(admin, "TESTMULALTNAME");
        try {
            // Change a user that we know...
            usersession.changeUser(admin, "foo", "foo123", "C=SE,O=AnaTom,CN=foo",
                    "uniformResourceIdentifier=http://www.a.se/,upn=foo@a.se,upn=foo@b.se,rfc822name=tomas@a.se,dNSName=www.a.se,dNSName=www.b.se,iPAddress=10.1.1.1",
                    "foo@anatom.se", false,
                    eeprofile,
                    SecConst.CERTPROFILE_FIXED_ENDUSER,
                    SecConst.USER_ENDUSER,
                    SecConst.TOKEN_SOFT_PEM, 0, UserDataConstants.STATUS_NEW, caid);
            log.debug("created user: foo, foo123, C=SE, O=AnaTom, CN=foo");
        } catch (RemoteException re) {
            assertTrue("User foo does not exist, or error changing user", false);
        } 
        X509Certificate cert = (X509Certificate) remote.createCertificate(admin, "foo", "foo123", keys.getPublic());
        assertNotNull("Failed to create certificate", cert);
        FileOutputStream fos = new FileOutputStream("cert.crt");
        fos.write(cert.getEncoded());
        fos.close();
        String altNames = CertTools.getSubjectAlternativeName(cert);
        log.debug(altNames);
        ArrayList list = CertTools.getPartsFromDN(altNames,CertTools.UPN);
        assertEquals(2, list.size());
        assertTrue(list.contains("foo@a.se"));
        assertTrue(list.contains("foo@b.se"));
        String name = CertTools.getPartFromDN(altNames,CertTools.URI);
        assertEquals("http://www.a.se/", name);
        name = CertTools.getPartFromDN(altNames,CertTools.EMAIL);
        assertEquals("tomas@a.se", name);
        list = CertTools.getPartsFromDN(altNames,CertTools.DNS);
        assertEquals(2, list.size());
        assertTrue(list.contains("www.a.se"));
        assertTrue(list.contains("www.b.se"));
        name = CertTools.getPartFromDN(altNames,CertTools.IPADDR);
        assertEquals("10.1.1.1", name);

        // Clean up
        rasession.removeEndEntityProfile(admin, "TESTMULALTNAME");

        log.debug("<test09TestMultipleAltNames()");        
    }
    
    /**
     * Tests scep message
     */
/*
    public void test10TestOpenScep() throws Exception {
        log.debug(">test10TestOpenScep()");
        UserDataPK pk = new UserDataPK("foo");
        UserDataRemote data = userhome.findByPrimaryKey(pk);
        data.setStatus(UserDataRemote.STATUS_NEW);
        log.debug("Reset status of 'foo' to NEW");
        IResponseMessage resp = remote.createCertificate(admin, new ScepRequestMessage(openscep), -1, Class.forName("org.ejbca.core.protocol.ScepResponseMessage"));
        assertNotNull("Failed to create certificate", resp);
        byte[] msg = resp.getResponseMessage();
        log.debug("Message: "+new String(Base64.encode(msg,true)));
        assertNotNull("Failed to get encoded response message", msg);
        log.debug("<test10TestOpenScep()");
    }
*/
}
