package se.anatom.ejbca.ra.raadmin;

import java.util.HashMap;
import javax.ejb.EntityContext;
import javax.ejb.CreateException;
import org.apache.log4j.Logger;
import se.anatom.ejbca.BaseEntityBean;

/** Entity bean should not be used directly, use though Session beans.
 *
 * Entity Bean representing admin preference.
 * Information stored:
 * <pre>
 * Id  (BigInteger SerialNumber)
 * AdminPreference
 * </pre>
 *
 * @version $Id: AdminPreferencesDataBean.java,v 1.4 2003-02-28 09:32:17 koen_serry Exp $
 */
public abstract class AdminPreferencesDataBean extends BaseEntityBean {

    private static Logger log = Logger.getLogger(AdminPreferencesDataBean.class);

    public abstract String getId();
    public abstract void setId(String id);
    public abstract HashMap getData();
    public abstract void setData(HashMap data);
    
    /** 
     * Method that returns the admin preference and updates it if nessesary.
     */    
    
    public AdminPreference getAdminPreference(){
      AdminPreference returnval = new AdminPreference();
      returnval.loadData((Object) getData());
      return returnval;              
    }
    
    /** 
     * Method that saves the admin preference to database.
     */    
    public void setAdminPreference(AdminPreference adminpreference){
       setData((HashMap) adminpreference.saveData());          
    }


    //
    // Fields required by Container
    //

    /**
     * Entity Bean holding data of admin preferences.
     * @param id the serialnumber.
     * @param adminpreference is the AdminPreference.
     * @return the primary key
     *
     **/

    public String ejbCreate(String id, AdminPreference adminpreference) throws CreateException {

        setId(id);
        setAdminPreference(adminpreference);

        log.debug("Created admin preference "+id);
        return id;
    }

    public void ejbPostCreate(String id, AdminPreference adminpreference) {
        // Do nothing. Required.
    }
}
