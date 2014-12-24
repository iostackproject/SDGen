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
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Raul Gracia-Tinedo (raulgraciatinedo@gmail.com)
 *
 */
public class PropertiesWatcherTask extends TimerTask {
    private String propsFile;
    private long lastMod;
    private String fieldName;
    private final static Timer timer = new Timer();
    private static final long INITIAL_DELAY = 1000 * 60 * 5;
    private static final long INTERVAL  = 1000 * 60 * 5;
 
    private PropertiesWatcherTask(long lastMod, String fieldName, String propsFileName) {
        this.propsFile = propsFileName;
        this.lastMod = lastMod;
        this.fieldName = fieldName;
    }
    @Override
    public void run() {
        //check last modified time.
        long newModTime = new File(propsFile).lastModified();
        if( newModTime > lastMod ) {
            try {
                PropertiesLoader.loadProperties(fieldName, propsFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.lastMod = newModTime;
        }
    }
    protected static void watch(String fieldName, String propsFileName, PropertiesHolder propsHolder) {
        File propsFile = new File(propsFileName);
        long lastMod = propsFile.lastModified();
        timer.scheduleAtFixedRate(new PropertiesWatcherTask(lastMod, fieldName, 
        		propsFileName), INITIAL_DELAY, INTERVAL);
    }
}