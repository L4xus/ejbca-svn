package org.ejbca.core.model.ra;

import org.ejbca.core.model.ra.raadmin.EndEntityProfile;
import org.ejbca.util.dn.DnComponents;

import junit.framework.Assert;
import junit.framework.TestCase;

public class TestUserDataFiller extends TestCase {
	EndEntityProfile profile;
	UserDataVO userData = new UserDataVO();
	public TestUserDataFiller(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();  
        userData = new UserDataVO("userName","CN=userName,O=linagora",-1688117755,"",
        		"user@linagora.com",1,3,1,2,0,new ExtendedInformation());
        profile = new EndEntityProfile();
        profile.addField(EndEntityProfile.USERNAME);//0
        profile.addField(EndEntityProfile.PASSWORD);//1
        profile.addField(EndEntityProfile.ADMINISTRATOR);//27
        profile.addField(EndEntityProfile.CLEARTEXTPASSWORD);//2
        profile.addField(EndEntityProfile.KEYRECOVERABLE);//28
        profile.addField(EndEntityProfile.SENDNOTIFICATION);//35
        profile.addField(EndEntityProfile.EMAIL);//26
        profile.addField(DnComponents.COUNTRY);//16
        profile.addField(DnComponents.ORGANIZATION);//12
        profile.setValue(EndEntityProfile.USERNAME, 0, "defaultUserName");
        profile.setValue(EndEntityProfile.PASSWORD, 0, "defaultPassword");
        profile.setValue(EndEntityProfile.ADMINISTRATOR, 0, "false");
        profile.setValue(EndEntityProfile.CLEARTEXTPASSWORD, 0, "true");
        profile.setValue(EndEntityProfile.KEYRECOVERABLE, 0, "false");
        profile.setValue(EndEntityProfile.SENDNOTIFICATION, 0, "false");
        profile.setValue(EndEntityProfile.EMAIL, 0, "defaultMail@linagora.com");
        profile.setValue(DnComponents.COMMONNAME, 0, "defaultCN");
        profile.setValue(DnComponents.COUNTRY, 0, "fr");
        profile.setValue(DnComponents.ORGANIZATION, 0, "linagora");
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    /**
     * Test dn is merged
     */
    public void testFillUserDataWithDefaultValuesDnOnly() {
    	userData.setSendNotification(true);
    	userData.setPassword("userPassword");
    	String expectedUserDn="CN=userName,O=linagora,C=fr";
    	UserDataFiller.fillUserDataWithDefaultValues(userData, profile);
    	Assert.assertTrue(userData.getUsername().equals("userName"));
    	Assert.assertTrue(userData.getSendNotification()==true);
    	Assert.assertTrue(userData.getEmail().equals("user@linagora.com"));
    	Assert.assertTrue(userData.getPassword().equals("userPassword"));
    	Assert.assertTrue(userData.getDN().equals(expectedUserDn));
    }
    /**
     * userName is merged
     */
    public void testFillUserDataWithDefaultValuesUserName() {
    	userData.setUsername("");
    	UserDataFiller.fillUserDataWithDefaultValues(userData, profile);
    	Assert.assertTrue(!userData.getUsername().equals("userName"));
    	Assert.assertTrue(userData.getUsername().equals("defaultUserName"));
    }
    /**
     * SendNotification is merged
     */
    public void testFillUserDataWithDefaultValuesSendNotification() {
    	profile.setValue(EndEntityProfile.SENDNOTIFICATION, 0, "true");
    	UserDataFiller.fillUserDataWithDefaultValues(userData, profile);
    	Assert.assertTrue(userData.getSendNotification()==true);
    }
    /**
     * Email is merged
     */
    public void testFillUserDataWithDefaultValuesEmail() {
    	userData.setEmail("");
    	UserDataFiller.fillUserDataWithDefaultValues(userData, profile);
    	Assert.assertTrue(userData.getEmail().equals("defaultMail@linagora.com"));
    	userData.setEmail("");
    	//Email is not merged because profile's email is not a valid email
    	profile.setValue(EndEntityProfile.EMAIL, 0, "@linagora.com");
    	UserDataFiller.fillUserDataWithDefaultValues(userData, profile);
    	//@linagora.com is not a valid e-mail address, no merge
    	Assert.assertTrue(userData.getEmail().equals(""));
    	
    }
    /**
     * Password is merged
     */
    public void testFillUserDataWithDefaultValuesPassword() {
    	UserDataFiller.fillUserDataWithDefaultValues(userData, profile);
    	Assert.assertTrue(userData.getPassword().equals("defaultPassword"));
    }
}
