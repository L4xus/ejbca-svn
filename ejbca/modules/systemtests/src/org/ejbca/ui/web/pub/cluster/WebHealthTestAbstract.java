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
package org.ejbca.ui.web.pub.cluster;

import org.apache.log4j.Logger;

import junit.framework.TestCase;

/**
 * @author mikek
 *
 */
public abstract class WebHealthTestAbstract extends TestCase {
    
    private static final Logger log = Logger.getLogger(WebHealthTestAbstract.class);
    
    protected String httpPort;
    protected String httpReqPath;

    public WebHealthTestAbstract(String name) {
        super(name);
    }

    public void createThreads() throws InterruptedException {
   
        Thread no1 = new Thread(new WebEjbcaHealthRunner(httpReqPath), "no1");
        Thread no2 = new Thread(new WebEjbcaHealthRunner(httpReqPath), "no2");
        Thread no3 = new Thread(new WebEjbcaHealthRunner(httpReqPath), "no3");
        Thread no4 = new Thread(new WebEjbcaHealthRunner(httpReqPath), "no4");
        Thread no5 = new Thread(new WebEjbcaHealthRunner(httpReqPath), "no5");
        no1.start();
        log.info("Started no1");
        no2.start();
        log.info("Started no2");
        no3.start();
        log.info("Started no3");
        no4.start();
        log.info("Started no4");
        no5.start();
        log.info("Started no5");
        no1.join();
        no2.join();
        no3.join();
        no4.join();
        no5.join();
    }
    
}
