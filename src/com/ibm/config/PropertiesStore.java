/*
 * Copyright (C) 2014 Raul Gracia-Tinedo
 * 
 * This program is free software: you can redistribute it and/or modify it under 
 * the terms of the GNU General Public License as published by the Free Software 
 * Foundation, either version 3 of the License, or (at your option) any later 
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with 
 * this program. If not, see http://www.gnu.org/licenses/.
 */
package com.ibm.config;

import java.util.Properties;

/**
 * @author Raul Gracia-Tinedo (raulgraciatinedo@gmail.com)
 *
 */
public class PropertiesStore {
	
	@PropertiesHolder(file="application.properties", autoLoad=true)
	private static Properties appProperties;

	public static String getString(String propName) {
		return getAppProperties().getProperty(propName);
	}
	
	public static int getInt(String propName) {
		return Integer.valueOf(getAppProperties().getProperty(propName));
	}
	
	public static double getDouble(String propName) {
		return Double.valueOf(getAppProperties().getProperty(propName));
	}

	public static boolean getBoolean(String propName) {
		return Boolean.valueOf(getAppProperties().getProperty(propName));
	}
	
	public static String[] getStrings(String propName) {
		return getAppProperties().getProperty(propName).split(",");
	}
	
	public static Properties getAppProperties() {
		if (appProperties==null){
			try {
				PropertiesLoader.load();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return appProperties;
	}
	protected static void setAppProperties(Properties appProperties) {
		PropertiesStore.appProperties = appProperties;
	}
}