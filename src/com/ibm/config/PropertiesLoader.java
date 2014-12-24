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

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Properties;

import com.ibm.utils.Utils;

/**
 * @author Raul Gracia-Tinedo (raulgraciatinedo@gmail.com)
 *
 */
public class PropertiesLoader {
    
    public static void load() throws Exception {
        Field[] fields = PropertiesStore.class.getDeclaredFields();
        for(Field field:fields) {
            if(field.isAnnotationPresent(PropertiesHolder.class) ) {
                PropertiesHolder propsHolder = field.getAnnotation(PropertiesHolder.class);
                loadPropsAndWatch(field.getName(), propsHolder);
            }
        }
    }

    private static void loadPropsAndWatch(String fieldName, 
    		PropertiesHolder propsHolder) throws Exception {
        String propsFile = propsHolder.file();
        loadProperties(fieldName, propsFile);
        if(propsHolder.autoLoad()) {
            PropertiesWatcherTask.watch(fieldName, propsFile, propsHolder);
        }
    }

    protected static void loadProperties(String fieldName, String propsFile)
            throws Exception {
        String setterName = "set" + Character.toUpperCase(fieldName.charAt(0)) + 
        		fieldName.substring(1);
        Method setter = PropertiesStore.class.getDeclaredMethod(setterName, 
        		Properties.class); 
        Properties props = new Properties();
        props.load(new FileInputStream(new File(Utils.APPLICATION_PATH+propsFile))); 
        setter.invoke(null, props);
    }
}