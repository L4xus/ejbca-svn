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
 
package org.ejbca.core.ejb.ra.raadmin;

import java.util.HashMap;

import javax.ejb.CreateException;

import org.apache.log4j.Logger;
import org.ejbca.core.ejb.BaseEntityBean;
import org.ejbca.core.model.ra.raadmin.EndEntityProfile;




/**
 * Entity bean should not be used directly, use though Session beans. Entity Bean representing a
 * end entity profile in the ra. Information stored:
 * <pre>
 *  id (Primary key)
 * Profile name
 * Profile data
 * </pre>
 *
 * @version $Id$
 *
 * @ejb.bean description="This enterprise bean entity represents a profile"
 * display-name="EndEntityProfileDataEB"
 * name="EndEntityProfileData"
 * jndi-name="EndEntityProfileData"
 * view-type="local"
 * type="CMP"
 * reentrant="False"
 * cmp-version="2.x"
 * transaction-type="Container"
 * schema="EndEntityProfileDataBean"
 * primkey-field="id"
 *
 * @ejb.pk class="java.lang.Integer"
 * generate="false"
 *
 * @ejb.persistence table-name = "EndEntityProfileData"
 *
 * @ejb.transaction type="Required"
 * 
 * @ejb.home
 * local-extends="javax.ejb.EJBLocalHome"
 * local-class="org.ejbca.core.ejb.ra.raadmin.EndEntityProfileDataLocalHome"
 *
 * @ejb.interface
 * local-extends="javax.ejb.EJBLocalObject"
 * local-class="org.ejbca.core.ejb.ra.raadmin.EndEntityProfileDataLocal"
 *
 * @ejb.finder
 *   description="findByProfileName"
 *   signature="org.ejbca.core.ejb.ra.raadmin.EndEntityProfileDataLocal findByProfileName(java.lang.String name)"
 *   query="SELECT OBJECT(a) from EndEntityProfileDataBean a WHERE a.profileName=?1"
 *
 * @ejb.finder
 *   description="findAll"
 *   signature="java.util.Collection findAll()"
 *   query="SELECT OBJECT(a) from EndEntityProfileDataBean a"
 *
 * @jboss.method-attributes
 *   pattern = "get*"
 *   read-only = "true"
 *
 */
public abstract class EndEntityProfileDataBean extends BaseEntityBean implements java.io.Serializable {
    private static final Logger log = Logger.getLogger(EndEntityProfileDataBean.class);

    /**
     * @ejb.pk-field
     * @ejb.persistence column-name="id"
     * @ejb.interface-method
     */
    public abstract Integer getId();

    /**
     */
    public abstract void setId(Integer id);

    /**
     * @ejb.persistence column-name="profileName"
     * @ejb.interface-method
     */
    public abstract String getProfileName();

    /**
     * @ejb.interface-method
     */
    public abstract void setProfileName(String profilename);

    /**
     * @ejb.persistence column-name="data"
     * @weblogic.ora.columntyp@
     */
    public abstract HashMap getData();

    /**
     */
    public abstract void setData(HashMap data);

    /**
     * Method that returns the end entity profiles and updates it if nessesary.
     *
     * @return DOCUMENT ME!
     * @ejb.interface-method
     */
    public EndEntityProfile getProfile() {
        EndEntityProfile returnval = new EndEntityProfile();
        returnval.loadData(getData());

        return returnval;
    }

    /**
     * Method that saves the admin preference to database.
     *
     * @param profile DOCUMENT ME!
     * @ejb.interface-method
     */
    public void setProfile(EndEntityProfile profile) {
        setData((HashMap) profile.saveData());
    }

    //
    // Fields required by Container
    //

    /**
     * Entity Bean holding data of a end entity profile.
     *
     * @param profilename DOCUMENT ME!
     * @param profilename
     * @param profile is the EndEntityProfile.
     *
     * @return null
     * @ejb.create-method
     */
    public Integer ejbCreate(Integer id, String profilename, EndEntityProfile profile)
        throws CreateException {
        setId(id);
        setProfileName(profilename);
        setProfile(profile);
        log.debug("Created profile " + profilename);

        return id;
    }

    /**
     * DOCUMENT ME!
     *
     * @param id DOCUMENT ME!
     * @param profilename DOCUMENT ME!
     * @param profile DOCUMENT ME!
     */
    public void ejbPostCreate(Integer id, String profilename, EndEntityProfile profile) {
        // Do nothing. Required.
    }
}
