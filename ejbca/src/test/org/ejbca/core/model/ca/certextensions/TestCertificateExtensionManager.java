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

package org.ejbca.core.model.ca.certextensions;

import java.util.Properties;

import junit.framework.TestCase;

import org.bouncycastle.asn1.DERPrintableString;

/**
 * 
 * Test the functionality of the CertificateExtensionManager
 * 
 * @author Philip Vendil 2007 jan 7
 *
 * @version $Id: TestCertificateExtensionManager.java,v 1.1 2007-01-09 16:47:20 herrvendil Exp $
 */

public class TestCertificateExtensionManager extends TestCase {
	

	
	public void test01CertificateExtensionFactory() throws Exception{
	    Properties props = new Properties();
	    props.put("id1.oid", "1.2.3.4");
	    props.put("id1.classpath", "org.ejbca.core.model.ca.certextensions.BasicCertificateExtension");
	    props.put("id1.displayname", "TESTEXTENSION");
	    props.put("id1.used", "TRUE");
	    props.put("id1.translatable", "FALSE");
	    props.put("id1.critical", "TRUE");	    
	    props.put("id1.property.encoding", "DERPRINTABLESTRING");
	    props.put("id1.property.value", "Test 123");
	    
	    props.put("id2.oid", "2.2.3.4");
	    props.put("id2.classpath", "org.ejbca.core.model.ca.certextensions.BasicCertificateExtension");
	    props.put("id2.displayname", "TESTEXTENSION2");
	    props.put("id2.used", "false");
	    props.put("id2.translatable", "FALSE");
	    props.put("id2.critical", "TRUE");
	    props.put("id2.property.encoding", "DERPRINTABLESTRING");
	    props.put("id2.property.value", "Test 123");

	    props.put("id3.oid", "3.2.3.4");
	    props.put("id3.classpath", "org.ejbca.core.model.ca.certextensions.DummyAdvancedCertificateExtension");
	    props.put("id3.displayname", "TESTEXTENSION3");
	    props.put("id3.used", "TRUE");
	    props.put("id3.translatable", "TRUE");
	    props.put("id3.critical", "FALSE");
	    props.put("id3.property.value", "Test 321");		
		
		CertificateExtensionFactory fact = CertificateExtensionFactory.getInstance(props);
		
		assertTrue(fact.getAvailableCertificateExtensions().size()+"",fact.getAvailableCertificateExtensions().size() ==2);
		AvailableCertificateExtension availExt = (AvailableCertificateExtension) fact.getAvailableCertificateExtensions().get(0);
		assertTrue(availExt.getId() == 1);
		assertTrue(availExt.getOID().equals("1.2.3.4"));
		assertTrue(availExt.getDisplayName().equals("TESTEXTENSION"));
		assertTrue(availExt.isTranslatable() == false);
		
		availExt = (AvailableCertificateExtension) fact.getAvailableCertificateExtensions().get(1);
		assertTrue(availExt.getId() == 3);
		assertTrue(availExt.getOID().equals("3.2.3.4"));
		assertTrue(availExt.getDisplayName().equals("TESTEXTENSION3"));
		assertTrue(availExt.isTranslatable() == true);
		
		CertificateExtension certExt = fact.getCertificateExtensions(new Integer(1));
		assertTrue(certExt != null);
		assertTrue(certExt.getId() == 1);
		assertTrue(certExt.getOID().equals("1.2.3.4"));
		assertTrue(certExt.isCriticalFlag());
		assertTrue(certExt.getValue(null, null, null) instanceof DERPrintableString);
		assertTrue(((DERPrintableString) certExt.getValue(null, null, null)).getString().equals("Test 123"));
		
		assertNull(fact.getCertificateExtensions(new Integer(2)));
		
		certExt = fact.getCertificateExtensions(new Integer(3));
		assertTrue(certExt != null);
		assertTrue(certExt.getId() == 3);
		assertTrue(certExt.getOID().equals("3.2.3.4"));
		assertTrue(!certExt.isCriticalFlag());
		assertTrue(certExt.getValue(null, null, null) instanceof DERPrintableString);
		assertTrue(((DERPrintableString) certExt.getValue(null, null, null)).getString().equals("Test 321"));
		
	}
	


}
