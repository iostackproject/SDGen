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
package com.ibm.test.unit;

import java.io.FileWriter;
import java.io.PrintWriter;

import junit.framework.Assert;
import junit.framework.JUnit4TestAdapter;

import org.junit.Before;
import org.junit.Test;

import com.ibm.config.PropertiesLoader;
import com.ibm.config.PropertiesStore;
import com.ibm.utils.Utils;

/**
 * @author Raul Gracia-Tinedo (raulgraciatinedo@gmail.com)
 *
 */
public class PropertiesStoreTest {

	@Before
	public void load() {
		try {
			PropertiesLoader.load();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Test
	public void testAutoLoad() throws Exception {
		write("APP_TITLE=New ABC Desktop");
		synchronized(this) {
			this.wait(45 * 1000);
		}
		Assert.assertEquals("New ABC Desktop", 
				PropertiesStore.getAppProperties().getProperty("APP_TITLE"));
		write("APP_TITLE=ABC Desktop");
	}
	@Test
	public void testProperties() throws Exception{
		Assert.assertEquals("ABC Desktop", 
				PropertiesStore.getAppProperties().getProperty("APP_TITLE"));
	}
	private void write(String text) throws Exception {
		PrintWriter pw = new PrintWriter( new FileWriter(
				Utils.APPLICATION_PATH+ "application.properties"));
		pw.print(text);
		pw.close();
	}
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(PropertiesStoreTest.class);
	}
}