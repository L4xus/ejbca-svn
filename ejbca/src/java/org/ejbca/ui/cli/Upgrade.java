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


/**
 * Implements call to the upgrade function
 *
 * @version $Id$
 */
public class Upgrade extends BaseCommand {

    /**
     * 
     */
    public Upgrade() {
        super();
    }
    
    public boolean upgrade() {
        debug(">upgrade");
        
        boolean ret = false;
        String database = System.getProperty("ejbcaDB");
        debug("ejbcaDB="+database);
        String upgradeFromVersion = System.getProperty("ejbcaUpgradeFromVersion");
        debug("ejbcaUpgradeFromVersion="+upgradeFromVersion);
        
        String upgradefrom33 = System.getProperty("ejbcaUpgradeFrom33");
        String upgradefrom31 = System.getProperty("ejbcaUpgradeFrom31");
        debug("ejbcaUpgradeFrom31="+database);
        
        // Check pre-requisites
        if (!appServerRunning()) {
           error("The application server must be running.");
           return false;
        }
       // Upgrade the database
       try {
          String[] args = new String[3];
          args[0] = database;
          args[1] = upgradefrom33;
          args[2] = upgradefrom31;
          ret = getUpgradeSession().upgrade(administrator, args);
       } catch (Exception e) {
           error("Can't upgrade: ", e);
           ret = false;
       }
       
      debug("<upgrade");
      return ret;
    }

    /**
     * main Upgrade
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        Upgrade upgrade = new Upgrade();
        try {
            boolean ret = upgrade.upgrade();
            if (!ret) {
                upgrade.error("Upgrade not performed, see server log for details.");
            } else {
            	upgrade.info("Upgrade completed.");   
            }
        } catch (Exception e) {
            //System.out.println(e.getMessage());
            upgrade.error("Error doing upgrade: ", e);
            System.exit(-1);
        }
    }

}
