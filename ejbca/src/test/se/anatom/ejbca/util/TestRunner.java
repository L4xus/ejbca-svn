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

package se.anatom.ejbca.util;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;


/**
 * Main test class
 *
 * @version $Id: TestRunner.java,v 1.2 2005-02-16 13:11:12 anatom Exp $
 */
public class TestRunner extends Object {
    private static Logger log = Logger.getLogger(TestRunner.class);

    /**
     * Main
     *
     * @param args cmd line args
     */
    public static void main(String[] args) {
        BasicConfigurator.configure();
        junit.textui.TestRunner.run(suite());
    }

    /**
     * sets up test suite
     *
     * @return cmd line args
     */
    public static Test suite() {
        log.debug(">suite()");

        TestSuite suite = new TestSuite();
        suite.addTest(new TestSuite(TestCertTools.class));
        suite.addTest(new TestSuite(TestKeyTools.class));
        suite.addTest(new TestSuite(TestStringTools.class));

        log.debug("<suite()");

        return suite;
    }
}
