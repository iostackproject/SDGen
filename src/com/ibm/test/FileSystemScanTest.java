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
package com.ibm.test;

import com.ibm.characterization.DatasetCharacterization;
import com.ibm.config.PropertiesStore;
import com.ibm.config.PropertyNames;
import com.ibm.scan.DataScanner;

/**
 * @author Raul Gracia-Tinedo (raulgraciatinedo@gmail.com)
 *
 */
public class FileSystemScanTest {

	private static int chunkSize = PropertiesStore.getInt(PropertyNames.GENERATION_CHUNK_SIZE);
	
	private static String toScan = "/home/raul/workspace/impressions/impress_home/";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DataScanner scanner = new DataScanner();
		//long time = System.currentTimeMillis();
		scanner.scan(toScan, chunkSize);
		DatasetCharacterization characterization = scanner.finishScanAndBuildCharacterization();
		characterization.save("/home/raul/Desktop/test2_char.ser");
		DatasetCharacterization newCharacterization = new DatasetCharacterization();
		newCharacterization.load("/home/raul/Desktop/test2_char.ser");
		System.exit(0);
	}
}