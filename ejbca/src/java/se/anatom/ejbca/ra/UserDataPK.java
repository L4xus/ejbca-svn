package se.anatom.ejbca.ra;

import se.anatom.ejbca.util.StringTools;

/**
 * The primary key of the User is the username fingerprint which should be unique.
 *
 * @version $Id: UserDataPK.java,v 1.4 2002-06-27 11:00:25 anatom Exp $
 **/
public class UserDataPK implements java.io.Serializable {
    public String username;

    public UserDataPK(String username) {
        this.username = StringTools.strip(username);
    }
    public UserDataPK() {
    }
    public int hashCode( ){
        return username.hashCode();
    }
    public boolean equals(Object obj){
            return ((UserDataPK)obj).username.equals(username);
    }
    public String toString(){
       return username.toString();
    }

}
