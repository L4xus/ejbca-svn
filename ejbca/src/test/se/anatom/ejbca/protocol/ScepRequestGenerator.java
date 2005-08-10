package se.anatom.ejbca.protocol;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERPrintableString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.DERUTF8String;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.smime.SMIMECapability;
import org.bouncycastle.cms.CMSEnvelopedData;
import org.bouncycastle.cms.CMSEnvelopedDataGenerator;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessable;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.jce.PKCS10CertificationRequest;

import se.anatom.ejbca.util.Base64;
import se.anatom.ejbca.util.CertTools;
import se.anatom.ejbca.util.KeyTools;

public class ScepRequestGenerator {
    private static Logger log = Logger.getLogger(ScepResponseMessage.class);

    private X509Certificate cert = null;
    private X509Certificate cacert = null;
    private String reqdn = null;
    private KeyPair keys = null;
    private PKCS10CertificationRequest p10request;
    int keysize = 1024;
    private String senderNonce = null;
    private String transactionId = null;
    
    public void setKeySize(int size) {
        this.keysize = size;
    }
    public void setKeys(KeyPair myKeys) {
        this.keys = myKeys;
    }
    /** Base 64 encode senderNonce
     */
    public String getSenderNonce() {
        return senderNonce;
    }
    public String getTransactionId() {
        return transactionId;
    }
    public byte[] generate(String dn, String password, X509Certificate ca) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException, IOException, CMSException, InvalidAlgorithmParameterException, CertStoreException {
        this.cacert = ca;
        this.reqdn = dn;
        // Generate keys
        if (keys == null) {
            keys = KeyTools.genKeys(keysize);            
        }

        // Create challenge password attribute for PKCS10
        // Attributes { ATTRIBUTE:IOSet } ::= SET OF Attribute{{ IOSet }}
        //
        // Attribute { ATTRIBUTE:IOSet } ::= SEQUENCE {
        //    type    ATTRIBUTE.&id({IOSet}),
        //    values  SET SIZE(1..MAX) OF ATTRIBUTE.&Type({IOSet}{\@type})
        // }
        ASN1EncodableVector vec = new ASN1EncodableVector();
        vec.add(PKCSObjectIdentifiers.pkcs_9_at_challengePassword); 
        ASN1EncodableVector values = new ASN1EncodableVector();
        values.add(new DERUTF8String(password));
        vec.add(new DERSet(values));
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(new DERSequence(vec));
        DERSet set = new DERSet(v);
        // Create PKCS#10 certificate request
        p10request = new PKCS10CertificationRequest("SHA1WithRSA",
                CertTools.stringToBcX509Name(reqdn), keys.getPublic(), set, keys.getPrivate());
        
        // Create self signed cert, validity 1 day
        cert = CertTools.genSelfCert(reqdn,24*60*60*1000,null,keys.getPrivate(),keys.getPublic(),false);
        
        // wrap message in pkcs#7
        byte[] msg = wrap();
        return msg;
        
    }
    
    private CMSEnvelopedData envelope(CMSProcessable envThis) throws NoSuchAlgorithmException, NoSuchProviderException, CMSException {
        CMSEnvelopedDataGenerator edGen = new CMSEnvelopedDataGenerator();
        // Envelope the CMS message
        edGen.addKeyTransRecipient(cacert);
        CMSEnvelopedData ed = edGen.generate(envThis, SMIMECapability.dES_CBC.getId(), "BC");
        return ed;
    }
    private CMSSignedData sign(CMSProcessable signThis) throws NoSuchAlgorithmException, NoSuchProviderException, CMSException, IOException, InvalidAlgorithmParameterException, CertStoreException {
        CMSSignedDataGenerator gen1 = new CMSSignedDataGenerator();

        // add authenticated attributes...status, transactionId, sender- and more...
        Hashtable attributes = new Hashtable();
        DERObjectIdentifier oid;
        Attribute attr;
        DERSet value;
        
        // Message type (certreq)
        oid = new DERObjectIdentifier(ScepRequestMessage.id_messageType);
        value = new DERSet(new DERPrintableString("19"));
        attr = new Attribute(oid, value);
        attributes.put(attr.getAttrType(), attr);

        // TransactionId
        byte[] digest = CertTools.generateMD5Fingerprint(p10request.getCertificationRequestInfo().getEncoded());
        transactionId = new String(Base64.encode(digest));
        oid = new DERObjectIdentifier(ScepRequestMessage.id_transId);
        value = new DERSet(new DERPrintableString(Base64.encode(digest)));
        attr = new Attribute(oid, value);
        attributes.put(attr.getAttrType(), attr);

        // senderNonce
        byte[] nonce = new byte[16];
        SecureRandom randomSource = SecureRandom.getInstance("SHA1PRNG");
        randomSource.nextBytes(nonce);
        senderNonce = new String(Base64.encode(nonce));
        if (nonce != null) {
            oid = new DERObjectIdentifier(ScepRequestMessage.id_senderNonce);
            log.debug("Added senderNonce: " + senderNonce);
            value = new DERSet(new DEROctetString(nonce));
            attr = new Attribute(oid, value);
            attributes.put(attr.getAttrType(), attr);
        }

        // Add our signer info and sign the message
        ArrayList certList = new ArrayList();
        certList.add(cert);
        CertStore certs = CertStore.getInstance("Collection", new CollectionCertStoreParameters(certList), "BC");
        gen1.addCertificatesAndCRLs(certs);
        gen1.addSigner(keys.getPrivate(), cert, CMSSignedDataGenerator.DIGEST_SHA1,
                new AttributeTable(attributes), null);
        // The signed data to be enveloped
        CMSSignedData s = gen1.generate(signThis, true, "BC");
        return s;
    }
    private byte[] wrap() throws IOException, NoSuchAlgorithmException, NoSuchProviderException, CMSException, InvalidAlgorithmParameterException, CertStoreException {

        // 
        // Create inner enveloped data
        //
        CMSEnvelopedData ed = envelope(new CMSProcessableByteArray(p10request.getEncoded()));
        log.debug("Enveloped data is " + ed.getEncoded().length + " bytes long");
        CMSProcessable msg = new CMSProcessableByteArray(ed.getEncoded());
        //
        // Create the outer signed data
        //
        CMSSignedData s = sign(msg);
        
        byte[] ret = s.getEncoded();
        return ret;
        
    }
}
