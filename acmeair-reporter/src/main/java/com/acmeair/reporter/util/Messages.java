package com.acmeair.reporter.util;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.commons.configuration.XMLConfiguration;

public class Messages {
	static ResourceBundle RESOURCE_BUNDLE;
	
	static CompositeConfiguration config;

	static {
			
		try {
			config = new CompositeConfiguration();
			config.addConfiguration(new SystemConfiguration());
			config.addConfiguration(new PropertiesConfiguration("messages.properties"));
			config.addConfiguration(new XMLConfiguration("config.xml"));

		} catch (Exception e) {
			System.out.println(e);
		}
	}

	private Messages() {
	}

	public static String getString(String key) {
		try {
			return config.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
	
	public static Configuration getConfiguration(){
		return config;
	}
}
