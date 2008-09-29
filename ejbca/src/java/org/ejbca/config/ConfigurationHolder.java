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

package org.ejbca.config;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * This is a singleton. Used to configure common-configuration with our sources.
 * 
 * Use like this:
 *   String value = EjbcaConfiguration.instance().getString("my.conf.property.key"); or
 *   String value = EjbcaConfiguration.instance().getString("my.conf.property.key", "default value");
 * or
 *   String value = EjbcaConfiguration.getExpandedString("my.conf.property.key", "default value");
 * to be able to parse values containing ${property}
 * 
 * See in-line comments below for the sources added to the configuration.
 */
public class ConfigurationHolder {

	private static final Logger log = Logger.getLogger(ConfigurationHolder.class);

	private static CompositeConfiguration config = null;
	
	/** This is a singleton so it's not allowed to create an instance explicitly */ 
	private ConfigurationHolder() {}
	
	public static final String[] CONFIG_FILES = {"ejbca.properties", "web.properties"};

	public static final String CONFIGALLOWEXTERNAL = "allow.external-dynamic.configuration";

	public static Configuration instance() {
		if (config == null) {
			// Default values build into jar file, this is last prio used if no of the other sources override this
			boolean allowexternal = false;
			try {
				URL url = ConfigurationHolder.class.getResource("/conf/"+CONFIG_FILES[0]);
				if (url != null) {
					PropertiesConfiguration pc = new PropertiesConfiguration(url);
					allowexternal = "true".equalsIgnoreCase(pc.getString(CONFIGALLOWEXTERNAL, "false"));
					log.info("Allow external re-configuration: " + allowexternal);
				}
			} catch (ConfigurationException e) {
				log.error("Error intializing configuration: ", e);
			}
			config = new CompositeConfiguration();

			// Only add these config sources if we allow external configuration
			if (allowexternal) {
				// Override with system properties, this is prio 1 if it exists (java -Dscep.test=foo)
				config.addConfiguration(new SystemConfiguration());
				log.info("Added system properties to configuration source (java -Dfoo.prop=bar).");

				// Override with file in "application server home directory"/conf, this is prio 2
				for (int i=0; i<CONFIG_FILES.length; i++) {
					File f = null;
					try {
						f = new File("conf"+File.separator+CONFIG_FILES[i]);
						if (f.exists()) {
							PropertiesConfiguration pc = new PropertiesConfiguration(f);
							pc.setReloadingStrategy(new FileChangedReloadingStrategy());
							config.addConfiguration(pc);
							log.info("Added file to configuration source: "+f.getAbsolutePath());
						}
					} catch (ConfigurationException e) {
						log.error("Failed to load configuration from file " + f.getAbsolutePath());
					}
				}
				// Override with file in "/etc/ejbca/conf/, this is prio 3
				for (int i=0; i<CONFIG_FILES.length; i++) {
					File f = null;
					try {
						f = new File("/etc/ejbca/conf/" + CONFIG_FILES[i]);
						if (f.exists()) {
							PropertiesConfiguration pc = new PropertiesConfiguration(f);
							pc.setReloadingStrategy(new FileChangedReloadingStrategy());
							config.addConfiguration(pc);
							log.info("Added file to configuration source: "+f.getAbsolutePath());	        		
						}
					} catch (ConfigurationException e) {
						log.error("Failed to load configuration from file " + f.getAbsolutePath());
					}
				}
			}
			// Default values build into jar file, this is last prio used if no of the other sources override this
			for (int i=0; i<CONFIG_FILES.length; i++) {
				try {
					URL url = ConfigurationHolder.class.getResource("/conf/" + CONFIG_FILES[i]);
					if (url != null) {
						PropertiesConfiguration pc = new PropertiesConfiguration(url);
						config.addConfiguration(pc);
						log.info("Added url to configuration source: " + url);
					}
				} catch (ConfigurationException e) {
					log.error("Failed to load configuration from resource " + "/conf/" + CONFIG_FILES[i], e);
				}
			}
		} 
		return config;
	}

	/**
	 * Return a the expanded version of a property. E.g.
	 *  property1=foo
	 *  property2=${property1}bar
	 * would return "foobar" for property2
	 * @param defaultValue to use if no property of such a name is found
	 */
	public static String getExpandedString(String property, String defaultValue) {
		//String orderString = "${jboss.home}/server/default";
		String ret = instance().getString(property, defaultValue);
		while (ret.indexOf("${") != -1) {
			ret = interpolate(ret);
		}
		return ret;
	}
	
	private static String interpolate(String orderString) {
		final Pattern PATTERN = Pattern.compile("\\$\\{(.+?)\\}");
		final Matcher m = PATTERN.matcher(orderString);
		final StringBuffer sb = new StringBuffer(orderString.length());
		m.reset();
		while (m.find()) {
			// when the pattern is ${identifier}, group 0 is 'identifier'
			String key = m.group(1);
			String value = getExpandedString(key, "");
			
			// if the pattern does exists, replace it by its value
			// otherwise keep the pattern ( it is group(0) )
			if (value != null) {
				m.appendReplacement(sb, value);
			} else {
				// I'm doing this to avoid the backreference problem as there will be a $
				// if I replace directly with the group 0 (which is also a pattern)
				m.appendReplacement(sb, "");
				String unknown = m.group(0);
				sb.append(unknown);
			}
		}
		m.appendTail(sb);
		return sb.toString();
	}

}

