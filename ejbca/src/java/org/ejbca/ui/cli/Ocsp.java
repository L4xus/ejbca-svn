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
 
package org.ejbca.ui.cli;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.math.BigInteger;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import org.bouncycastle.ocsp.OCSPRespGenerator;
import org.ejbca.core.protocol.ocsp.OCSPUnidClient;
import org.ejbca.core.protocol.ocsp.OCSPUnidResponse;
import org.ejbca.util.CertTools;
import org.ejbca.util.FileTools;
import org.ejbca.util.PerformanceTest;
import org.ejbca.util.PerformanceTest.Command;
import org.ejbca.util.PerformanceTest.CommandFactory;

/**
 * Implements the OCSP simple query command line query interface
 *
 * @version $Id$
 */
public class Ocsp extends ClientToolBox {
    private class StressTest {
        final PerformanceTest performanceTest;
        final String ocspurl;
        final Certificate cacert;
        final SerialNrs serialNrs;
        final String keyStoreFileName;
        final String keyStorePassword;
        private class MyCommandFactory implements CommandFactory {
            MyCommandFactory() {
                super();
            }
            public Command[] getCommands() throws Exception {
                return new Command[]{new Lookup()};
            }
        }
        private class SerialNrs {
            final private List<BigInteger> vSerialNrs;
            SerialNrs(String fileName) throws FileNotFoundException, IOException, ClassNotFoundException {
                final InputStream is = new BufferedInputStream(new FileInputStream(fileName));
                is.mark(1);
                this.vSerialNrs = new ArrayList<BigInteger>();
                try {
                    ObjectInput oi = null;
                    while( true ) {
                        for ( int i=100; oi==null && i>0; i--) {
                            is.reset();
                            try {
                                is.mark(i);
                                oi = new ObjectInputStream(is);
                            } catch( StreamCorruptedException e) {
                                is.reset();
                                is.read();
                            }
                        }
                        if ( oi==null )
                            break;
                        try {
                            is.mark(100);
                            vSerialNrs.add((BigInteger)oi.readObject());
                        } catch( StreamCorruptedException e ) {
                            oi=null;
                        }
                    }
                } catch( EOFException e) {/* do nothing*/}
                System.out.println("Number of certificates in list: "+this.vSerialNrs.size());
            }
            BigInteger getRandom() {
                return vSerialNrs.get(performanceTest.getRandom().nextInt(vSerialNrs.size()));
            }
        }
        private class Lookup implements Command {
            private final OCSPUnidClient client;
            Lookup() throws Exception {
                this.client = OCSPUnidClient.getOCSPUnidClient(keyStoreFileName, keyStorePassword, ocspurl, keyStoreFileName!=null, false);
            }
            public boolean doIt() throws Exception {
                OCSPUnidResponse response = client.lookup(serialNrs.getRandom(),
                                                          cacert);
                if (response.getErrorCode() != OCSPUnidResponse.ERROR_NO_ERROR) {
                    performanceTest.getLog().error("Error querying OCSP server. Error code is: "+response.getErrorCode());
                    return false;
                }
                if (response.getHttpReturnCode() != 200) {
                    performanceTest.getLog().error("Http return code is: "+response.getHttpReturnCode());
                    return false;
                }
                performanceTest.getLog().info("OCSP return value is: "+response.getStatus());
                return true;
            }
            public String getJobTimeDescription() {
                return "OCSP lookup";
            }
        }
        StressTest(String args[]) throws Exception {
            if ( args.length<7 ) {
                System.out.println("Usage: OCSP stress <OCSP URL> <Certificate serial number file> <ca cert file> <number of threads> <wait time between requests> [<request signing keystore file>] [<request signing password>]");
                System.exit(1);
            }
            this.ocspurl = args[2];
            this.serialNrs = new SerialNrs(args[3]);
            this.cacert = getCertFromPemFile(args[4]);
            final int numberOfThreads = Integer.parseInt(args[5]);
            final int waitTime = Integer.parseInt(args[6]);
            if( args.length>7 )
                this.keyStoreFileName = args[7];
            else
                this.keyStoreFileName = null;
            if( args.length>8 )
                this.keyStorePassword = args[8];
            else
                this.keyStorePassword = null;
            this.performanceTest = new PerformanceTest();
            this.performanceTest.execute(new MyCommandFactory(), numberOfThreads, waitTime, System.out);
        }
    }
    static Certificate getCertFromPemFile(String fileName) throws IOException, CertificateException {
        byte[] bytes = FileTools.getBytesFromPEM(FileTools.readFiletoBuffer(fileName),
                                                 "-----BEGIN CERTIFICATE-----", "-----END CERTIFICATE-----");
        return CertTools.getCertfromByteArray(bytes);
    }
    /* (non-Javadoc)
     * @see org.ejbca.ui.cli.ClientToolBox#execute(java.lang.String[])
     */
    @Override
    void execute(String[] args) {
        try {
            CertTools.installBCProvider();

            final String ksfilename;
            final String kspwd;
            final String ocspurl;
            final String certfilename;
            final String cacertfilename;
            boolean signRequest = false;
            if ( args.length>1 && args[1].equals("stress") ) {
                new StressTest(args);
                return;
            } else if (args.length == 6) {
                ksfilename = args[1];
                kspwd = args[2];
                ocspurl = args[3].equals("null") ? null : args[3];
                certfilename = args[4];
                cacertfilename = args[5];            	
                signRequest = true;
            } else if (args.length == 4) {
                ksfilename = null;
                kspwd = null;
                ocspurl = args[1].equals("null") ? null : args[1];
                certfilename = args[2];
                cacertfilename = args[3];
            } else {
                System.out.println("Usage 1: OCSP KeyStoreFilename Password, OCSPUrl CertificateFileName CA-certificateFileName");
                System.out.println("Usage 2: OCSP OCSPUrl CertificateFileName CA-certificateFileName");
                System.out.println("Usage 3: OCSP stress ...");
                System.out.println("Keystore should be a PKCS12.");
                System.out.println("OCSPUrl is like: http://127.0.0.1:8080/ejbca/publicweb/status/ocsp or https://127.0.0.1:8443/ejbca/publicweb/status/ocsp");
                System.out.println("OCSP response status is: GOOD="+OCSPUnidResponse.OCSP_GOOD+", REVOKED="+OCSPUnidResponse.OCSP_REVOKED+", UNKNOWN="+OCSPUnidResponse.OCSP_UNKNOWN);
                System.out.println("OcspUrl can be set to 'null', in that case the program looks for an AIA extension containing the OCSP URI.");
                System.out.println("Just the stress argument gives further info about the stress test.");
                return;
            }
            
            OCSPUnidClient client = OCSPUnidClient.getOCSPUnidClient(ksfilename, kspwd, ocspurl, signRequest, true);
            OCSPUnidResponse response = client.lookup(getCertFromPemFile(certfilename),
                                                      getCertFromPemFile(cacertfilename));
            if (response.getErrorCode() != OCSPUnidResponse.ERROR_NO_ERROR) {
            	System.out.println("Error querying OCSP server.");
            	System.out.println("Error code is: "+response.getErrorCode());
            }
            if (response.getHttpReturnCode() != 200) {
            	System.out.println("Http return code is: "+response.getHttpReturnCode());
            }
            if (response.getResponseStatus() == 0) {
                System.out.print("OCSP return value is: "+response.getStatus()+" (");
                switch (response.getStatus()) {
	                case OCSPUnidResponse.OCSP_GOOD: System.out.println("good)"); break;
	                case OCSPUnidResponse.OCSP_REVOKED: System.out.println("revoked)"); break;
	                case OCSPUnidResponse.OCSP_UNKNOWN: System.out.println("unknown)"); break;
                }
                if (response.getFnr() != null) {
                    System.out.println("Returned Fnr is: "+response.getFnr());            	
                }            	
            } else {
            	System.out.print("OCSP response status is: "+response.getResponseStatus()+" (");
            	switch (response.getResponseStatus()) {
            		case OCSPRespGenerator.MALFORMED_REQUEST: System.out.println("malformed request)"); break;
            		case OCSPRespGenerator.INTERNAL_ERROR: System.out.println("internal error"); break;
            		case OCSPRespGenerator.TRY_LATER: System.out.println("try later)"); break;
            		case OCSPRespGenerator.SIG_REQUIRED: System.out.println("signature required)"); break;
            		case OCSPRespGenerator.UNAUTHORIZED: System.out.println("unauthorized)"); break;
            	}
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }
    }
    /**
     * @param args command line arguments
     */
    public static void main(String[] args) {
        final List<String> lArgs = new ArrayList<String>();
        lArgs.add("dummy");
        for ( int i=0; i<args.length; i++) // remove first argument
            lArgs.add(args[i]);
        new Ocsp().execute(lArgs.toArray(new String[]{}));
    }
    /* (non-Javadoc)
     * @see org.ejbca.ui.cli.ClientToolBox#getName()
     */
    @Override
    String getName() {
        return "OCSP";
    }
}
