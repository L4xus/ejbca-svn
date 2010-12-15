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
 
package org.ejbca.ui.web.pub.inspect;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.util.ASN1Dump;
import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.ejbca.cvc.CVCObject;
import org.ejbca.cvc.CertificateParser;
import org.ejbca.cvc.exception.CvcException;
import org.ejbca.util.CertTools;
import org.ejbca.util.RequestMessageUtils;

/**
 * This bean dumps contents of certificate and certificate request as text. CVC dump or ASN.1 dump.
 * 
 * To make it easy to use from JSTL pages, most methods take no arguments.
 * The arguments are supplied as member variables instead. <br>
 * 
 * @version $Id$
 */
public class CertAndRequestDumpBean {
	
	private byte[] bytes;
	private String type = "unknown";

	/**
	 * Empty default constructor.
	 */
	public CertAndRequestDumpBean() {
	}

	public void setBytes(byte[] b) {
		if (b.length < 10000) {
			// don't accept anything too large
			this.bytes = b;
			// Figure out type of request
			getDump(); // as side effect sets type variable
		} else {
			type = "too large";
		}
	}
	
	/** Dumps contents, and updates "type" variable as side-effect.
	 * 
	 * @return String containing raw text output or null of input is null, or error message if input invalid.
	 */
	public String getDump() {
		String ret = null;
		if (bytes == null) {
			return null;
		}
		byte[] requestBytes = RequestMessageUtils.getDecodedBytes(bytes);
		ret = getCvcDump(false);
		if ((ret == null) && (requestBytes != null) && (requestBytes.length > 0)) {
			// Not a CVC request, perhaps a PKCS10 request
			try {
				PKCS10CertificationRequest pkcs10 = new PKCS10CertificationRequest(requestBytes);
//				ret = pkcs10.toString();
				ASN1InputStream ais = new ASN1InputStream(new ByteArrayInputStream(pkcs10.getEncoded()));
				DERObject obj = ais.readObject();
				ret = ASN1Dump.dumpAsString(obj);
				type = "PKCS#10";
			} catch (IOException e1) {
				 // ignore, move on to certificate decoding
			} catch (IllegalArgumentException e1) {
				// ignore, move on to certificate decoding
			} catch (ClassCastException e2) {
				// ignore, move on to certificate decoding
			}
		} else if (ret != null) {
			type = "CVC";
		}
		if (ret == null) {
			// Not a CVC object or PKCS10 request message, perhaps a X.509 certificate?
			try {
				Certificate cert = getCert(bytes);
				ret = CertTools.dumpCertificateAsString(cert);
				type = "X.509";
			} catch (Exception e) {
				// Not a X.509 certificate either...try to simply decode asn.1
				try {
					ASN1InputStream ais = new ASN1InputStream(new ByteArrayInputStream(bytes));
					DERObject obj = ais.readObject();
					if (obj != null) {
						ret = ASN1Dump.dumpAsString(obj);
						type = "ASN.1";						
					}
				} catch (IOException e1) {
					// Last stop, say what the error is
					ret = e1.getMessage();
				}
			}						
		}
		return ret;
	}

	public String getType() {
		return type;
	}

	public String getCvcDump(boolean returnMessageOnError) {
		String ret = null;
		byte[] requestBytes = RequestMessageUtils.getDecodedBytes(bytes);
		try {
			CVCObject obj = getCVCObject(requestBytes);
			ret = obj.getAsText("");
		} catch (Exception e) {
			// Not a CVC request, perhaps a PKCS10 request
			if (returnMessageOnError) {
				ret = e.getMessage();				
			}
		}
		return ret;
	}

	private static CVCObject getCVCObject(byte[] cvcdata) throws IOException, CvcException, CertificateException {
		CVCObject ret = null;
		try {
			ret = CertificateParser.parseCVCObject(cvcdata);
		} catch (Exception e) {
			try {
				// this was not parseable, try to see it it was a PEM certificate
				Collection col = CertTools.getCertsFromPEM(new ByteArrayInputStream(cvcdata));
				Certificate cert = (Certificate)col.iterator().next();
	        	ret = CertificateParser.parseCVCObject(cert.getEncoded());
			} catch (Exception ie) {
				// this was not a PEM cert, try to see it it was a PEM certificate req
				byte[] req = RequestMessageUtils.getRequestBytes(cvcdata);
				ret = CertificateParser.parseCVCObject(req);
			}
		}
		return ret;
	}

	private Certificate getCert(byte[] certbytes) throws CertificateException {
		  Certificate cert = null;
		  Collection cachain = null;
		  try {
			  Collection certs = CertTools.getCertsFromPEM(new ByteArrayInputStream(certbytes));
			  Iterator iter = certs.iterator();
			  cert = (Certificate) iter.next();	
			  if (iter.hasNext()) {
				  // There is a complete certificate chain returned here
				  cachain = new ArrayList();
				  while (iter.hasNext()) {
					  Certificate chaincert = (Certificate) iter.next();
					  cachain.add(chaincert);
				  }
			  }
		  } catch (IOException e) {
			  // See if it is a single binary certificate
			  cert = CertTools.getCertfromByteArray(certbytes);
		  }
		  return cert;
	}

}
