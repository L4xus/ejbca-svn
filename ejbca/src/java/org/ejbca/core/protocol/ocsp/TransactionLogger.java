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
package org.ejbca.core.protocol.ocsp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.ejbca.core.model.InternalResources;
import org.ejbca.util.PatternLogger;

/**
 * This class is used for logging information about ocsp-requests.
 * 
 * @author tham
 * @version $Id$
 *
 */
public class TransactionLogger extends PatternLogger{
	public static final Logger auditlog = Logger.getLogger(TransactionLogger.class.getName());
	public static final String STATUS="STATUS";//The status of the OCSP-Request. SUCCESSFUL = 0;MALFORMED_REQUEST = 1;INTERNAL_ERROR = 2;																				//TRY_LATER = 3;SIG_REQUIRED = 5;UNAUTHORIZED = 6;
	public static final String REQ_NAME="REQ_NAME";//The Common Name (CN) of the client making the request
	public static final String SIGN_ISSUER_NAME_DN="SIGN_ISSUER_NAME_DN";//DN of the issuer of the certificate used to sign the request.
	public static final String SIGN_SUBJECT_NAME="SIGN_SUBJECT_NAME";//Subject Name of the certificate used to sign the request.
	public static final String SIGN_SERIAL_NO="SIGN_SERIAL_NO";//Certificate serial number of the certificate used to sign the request.
	public static final String NUM_CERT_ID="NUM_CERT_ID"; // The number of certificates to check revocation status for
	public static final String ISSUER_NAME_DN="ISSUER_NAME_DN";// The subject DN of the issuer of a requested certificate
	public static final String ISSUER_NAME_HASH="ISSUER_NAME_HASH";//MD5 hash of the issuer DN
	public static final String ISSUER_KEY="ISSUER_KEY";//The public key of the issuer of a requested certificate
	public static final String DIGEST_ALGOR="DIGEST_ALGOR";//Algorithm used by requested certificate to hash issuer key and issuer name
	public static final String SERIAL_NOHEX="SERIAL_NOHEX";// Serial number of the requested certificate.
	public static final String CERT_STATUS="CERT_STATUS";//The requested certificate revocation status.
	public static final String REPLY_TIME = "REPLY_TIME";
	public static final String CLIENT_IP="CLIENT_IP";//IP of the client making the request


	/** Internal localization of logs and errors */
	private static final InternalResources intres = InternalResources.getInstance();

	/** regexp pattern to match ${identifier} patterns */// ${DN};${IP}
	// private final static Pattern PATTERN = Pattern.compile("\\$\\{(.+?)\\}"); // TODO this should be configurable from file
	private  static Pattern PATTERN;// = Pattern.compile("\\$\\{(.+?)\\}");// TODO this should be configurable from file

	//  = "${LOG_ID};${STATUS};\"${CLIENT_IP}\";\"${SIGN_ISSUER_NAME_DN}\";\"${SIGN_SUBJECT_NAME}\";${SIGN_SERIAL_NO};" +
	// 		"\"${LOG_TIME}\";${NUM_CERT_ID};0;0;0;0;0;0;0;" +
	//		"\"${ISSUER_NAME_DN}\";${ISSUER_NAME_HASH};${ISSUER_KEY};${DIGEST_ALGOR};${SERIAL_NOHEX};${CERT_STATUS}";
	private  static Matcher m_matcher; 
	private  static String orderString;
	private static String mLogDateFormat; 
	private static String mTimeZone;
	public TransactionLogger () {
		super(PATTERN.matcher(orderString), orderString, auditlog, mLogDateFormat, mTimeZone);
		cleanParams();
	}

	/**
	 * This Method needs to be called before creating any instances
	 * 
	 * @param auditLogPattern
	 * @param auditLogOrder
	 * @param logDateFormat
	 */
	public static void configure(String auditLogPattern, String auditLogOrder, String logDateFormat, String timeZone) {
		PATTERN = Pattern.compile(auditLogPattern);
		orderString = auditLogOrder;
		m_matcher = PATTERN.matcher(orderString);
		mLogDateFormat = logDateFormat;
		mTimeZone = timeZone;
	}

	protected void cleanParams() {
		super.cleanParams();
		super.paramPut(LOG_ID, "0");
		super.paramPut(STATUS,"0");
		super.paramPut(CLIENT_IP,"0");
		super.paramPut(REQ_NAME,"0");
		super.paramPut(SIGN_ISSUER_NAME_DN,"0");
		super.paramPut(SIGN_SUBJECT_NAME,"0");
		super.paramPut(SIGN_SERIAL_NO,"0");
		super.paramPut(NUM_CERT_ID,"0");
		super.paramPut(ISSUER_NAME_DN,"0");
		super.paramPut(ISSUER_NAME_HASH,"0");
		super.paramPut(ISSUER_KEY,"0");
		super.paramPut(DIGEST_ALGOR,"0");
		super.paramPut(SERIAL_NOHEX,"0");
		super.paramPut(CERT_STATUS,"0");
		super.paramPut(REPLY_TIME,"REPLY_TIME");
	}
	
	/**
	 * Writes all the rows created by writeln() to the Logger
	 */
	public void flush(String replytime) {
		String logstring = super.logmessage.toString();
		logstring = logstring.replaceAll("REPLY_TIME", replytime);
		super.logger.debug(logstring);
	}
	
	public void flush() {
		String logstring = super.logmessage.toString();
		logstring = logstring.replaceAll("REPLY_TIME", "0");
		super.logger.debug(logstring);
	}
	
}
