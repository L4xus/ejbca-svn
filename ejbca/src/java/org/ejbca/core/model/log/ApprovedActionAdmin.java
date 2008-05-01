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

package org.ejbca.core.model.log;

import java.security.cert.X509Certificate;

import org.ejbca.core.model.authorization.AdminEntity;
import org.ejbca.core.model.authorization.AdminInformation;

/**
 * 
 * Special Admin type that should be used for approved actions.
 * Is a mix where the request admin is shown in the log but hte
 * authorization is done with the internal admin.
 * 
 * @author Philip Vendil 2007 jun 24
 *
 * @version $Id$
 */

public class ApprovedActionAdmin extends Admin {
	
	public ApprovedActionAdmin(X509Certificate certificate){
		super(certificate);
		this.type = TYPE_CLIENTCERT_USER;
	}

	
	public AdminInformation getAdminInformation() {		
		return  new AdminInformation(AdminEntity.SPECIALADMIN_INTERNALUSER);
	}


	
	
	
	

}
