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

package se.anatom.ejbca.util.passgen;
import java.util.Random;
import java.util.Date;

/**
 * BasePasswordGenerator is a baseclass for generating random passwords.
 * Inheriting classes should overload the constants USEDCHARS, MIN_CHARS
 * and MAX_CHARS.
 *
 * @version $Id: BasePasswordGenerator.java,v 1.5 2004-06-10 15:10:46 sbailliez Exp $
 */
public abstract class BasePasswordGenerator implements IPasswordGenerator{

    protected BasePasswordGenerator(char[] usedchars){

       this.usedchars = usedchars;
    }

	/**
	 * @see se.anatom.ejbca.util.passgen.IPasswordGenerator
	 */

	public String getNewPassword(int minlength, int maxlength){
		int difference = maxlength - minlength;
		char[] password = null;

		Random ran = new Random();

		// Calculate the length of password
		int passlen = maxlength;
		if(minlength != maxlength)
		  passlen = minlength + ran.nextInt(difference);

		password = new char[passlen];
		for(int i=0; i < passlen; i++){
		  password[i] = usedchars[ran.nextInt(usedchars.length)];
		}

		return new String(password);
	}


    private final char[] usedchars;
}
