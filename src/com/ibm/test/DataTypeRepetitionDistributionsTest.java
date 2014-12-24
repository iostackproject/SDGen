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

import java.io.IOException;

import com.ibm.characterization.Histogram;
import com.ibm.scan.DataScanner;
import com.ibm.utils.Utils;

/**
 * @author Raul Gracia-Tinedo (raulgraciatinedo@gmail.com)
 *
 */
public class DataTypeRepetitionDistributionsTest {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		String originalFile = "/home/raul/Desktop/test_data/";
		String testDir = "repetitionsDataTypes/";
		String dataType = "Trajectory";
		Utils.createDirectory(testDir);
		for (int regionSize=32*1024; regionSize <= 256*1024; regionSize*=2){
			DataScanner dataScanner = new DataScanner();
			dataScanner.scan(originalFile+dataType, regionSize);
			//Histogram repetitionsHistogram = dataScanner.getRepetitionsHistogram();
			//TestUtils.writeMapToFile(repetitionsHistogram, 
			//		testDir + "repetitions_" + dataType + "_" + regionSize/1024 + ".dat");
		}
		System.exit(0);
	}
}