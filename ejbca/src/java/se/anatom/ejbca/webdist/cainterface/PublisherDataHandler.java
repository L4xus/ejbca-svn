package se.anatom.ejbca.webdist.cainterface;

import java.io.Serializable;

import se.anatom.ejbca.authorization.AuthorizationDeniedException;
import se.anatom.ejbca.authorization.AvailableAccessRules;
import se.anatom.ejbca.authorization.IAuthorizationSessionLocal;
import se.anatom.ejbca.ca.caadmin.ICAAdminSessionLocal;
import se.anatom.ejbca.ca.exception.PublisherConnectionException;
import se.anatom.ejbca.ca.exception.PublisherExistsException;
import se.anatom.ejbca.ca.publisher.BasePublisher;
import se.anatom.ejbca.ca.publisher.IPublisherSessionLocal;
import se.anatom.ejbca.ca.store.ICertificateStoreSessionLocal;
import se.anatom.ejbca.log.Admin;
import se.anatom.ejbca.webdist.webconfiguration.InformationMemory;
/**
 * A class handling the hardtoken profile data in the webinterface.
 *
 * @author  TomSelleck
 */
public class PublisherDataHandler implements Serializable {

    
    
    /** Creates a new instance of PublisherDataHandler */
    public PublisherDataHandler(Admin administrator, IPublisherSessionLocal publishersession, IAuthorizationSessionLocal authorizationsession, 
                                ICAAdminSessionLocal caadminsession,ICertificateStoreSessionLocal certificatestoresession, InformationMemory info) {
       this.publishersession = publishersession;           
       this.authorizationsession = authorizationsession;
       this.caadminsession = caadminsession;
       this.certificatestoresession = certificatestoresession;
       this.administrator = administrator;          
       this.info = info;       
    }
    
       /** Method to add a publisher. Throws PublisherExitsException if profile already exists  */
    public void addPublisher(String name, BasePublisher publisher) throws PublisherExistsException, AuthorizationDeniedException {
      if(authorizedToEditPublishers()){
        publishersession.addPublisher(administrator, name, publisher);
        this.info.publishersEdited();
      }else
        throw new AuthorizationDeniedException("Not authorized to add publisher");  
    }    

       /** Method to change a publisher. */     
    public void changePublisher(String name, BasePublisher publisher) throws AuthorizationDeniedException{
      if(authorizedToEditPublishers()){ 
        publishersession.changePublisher(administrator, name,publisher);   
		this.info.publishersEdited();
      }else
        throw new AuthorizationDeniedException("Not authorized to edit publisher");      
    }
    
    /** Method to remove a publisher, returns true if deletion failed.*/ 
    public boolean removePublisher(String name) throws AuthorizationDeniedException{
      boolean returnval = true;  

      if(authorizedToEditPublishers()){
      	int publisherid = publishersession.getPublisherId(administrator, name);
        if(!caadminsession.exitsPublisherInCAs(administrator, publisherid) && !certificatestoresession.existsPublisherInCertificateProfiles(administrator,publisherid)){      	
		  publishersession.removePublisher(administrator, name);
		  this.info.publishersEdited();
		  returnval = false;
        }  
      }else
        throw new AuthorizationDeniedException("Not authorized to remove publisher.");
        
      return returnval;          
    }
    
    /** Metod to rename a publisher */
    public void renamePublisher(String oldname, String newname) throws PublisherExistsException, AuthorizationDeniedException{
     if(authorizedToEditPublishers()){    
		publishersession.renamePublisher(administrator, oldname,newname);
	   this.info.publishersEdited();
     }else
       throw new AuthorizationDeniedException("Not authorized to rename publisher");
    }
    

    public void clonePublisher(String originalname, String newname) throws PublisherExistsException, AuthorizationDeniedException{         
      if(authorizedToEditPublishers()){
        publishersession.clonePublisher(administrator, originalname,newname);
        this.info.publishersEdited();
      }else
         throw new AuthorizationDeniedException("Not authorized to clone publisher");          
    }
    
    public void testConnection(String name) throws PublisherConnectionException, AuthorizationDeniedException{         
    	if(authorizedToPublisherName(name)){
    		publishersession.testConnection(administrator, publishersession.getPublisherId(administrator, name));    		
    	}else
    		throw new AuthorizationDeniedException("Not authorized to clone publisher");          
    }        
    
      /** Method to get a reference to a publisher.*/ 
    public BasePublisher getPublisher(int id) throws AuthorizationDeniedException{
      if(!authorizedToPublisherId(id))
        throw new AuthorizationDeniedException("Not authorized to publisher");            
      
      return publishersession.getPublisher(administrator, id); 
    }      
          
    public BasePublisher getPublisher(String name) throws AuthorizationDeniedException{
     if(!authorizedToPublisherName(name))
        throw new AuthorizationDeniedException("Not authorized to publisher");            
         
      return publishersession.getPublisher(administrator, name);
    }
   
      
    public int getPublisherId(String name){
      return publishersession.getPublisherId(administrator, name);  
    }
    
    
    /**
     * Help function that checks if administrator is authorized to edit publisher with given name.
     */
    private boolean authorizedToPublisherName(String name){
	  int id = publishersession.getPublisherId(administrator, name);
      return authorizedToPublisherId(id);
    }
     
    
    /**
     * Help function that checks if administrator is authorized to edit publisher with given id.
     */
    private boolean authorizedToPublisherId(int id){      
      return info.getAuthorizedPublisherNames().values().contains(new Integer(id));
    }
    
    /**
     * Help function that checks if administrator is authorized to edit publisher.
     */    
    private boolean authorizedToEditPublishers(){          
        try{
          authorizationsession.isAuthorizedNoLog(administrator, AvailableAccessRules.ROLE_SUPERADMINISTRATOR);
          return true;  
        }catch(AuthorizationDeniedException ade){}
              
       return false;  
    }    
   
    private IPublisherSessionLocal         publishersession; 
    private Admin                          administrator;
    private IAuthorizationSessionLocal     authorizationsession;
    private ICAAdminSessionLocal           caadminsession;
    private ICertificateStoreSessionLocal  certificatestoresession;
    private InformationMemory              info;
}
