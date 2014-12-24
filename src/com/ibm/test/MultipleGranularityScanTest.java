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

import com.ibm.scan.CompressionTimeScanner;
import com.ibm.utils.Utils;

/**
 * @author Raul Gracia-Tinedo (raulgraciatinedo@gmail.com)
 *
 */
public class MultipleGranularityScanTest {

	private static String testPath = "scanGranularityTest/";
	
	private static String dataType = "syntheticFileallText";

	private static String datasetPath = "/home/user/Desktop/";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Utils.createDirectory(testPath);
		int[] granularities = {64, 16};
		for (int chunkSize: granularities){
			System.out.println("Scannign at granularity: " + chunkSize + "KB");
			//Scan original dataset
			CompressionTimeScanner cts = new CompressionTimeScanner(testPath + 
					dataType + "_performance_" + chunkSize +"KB.dat");
			cts.scan(datasetPath + dataType + ".tar", chunkSize*1024);
			cts.finishScan();
		}
		System.exit(0);
	}
}