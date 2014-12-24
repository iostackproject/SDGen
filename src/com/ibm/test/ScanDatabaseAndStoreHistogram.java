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

/**
 * @author Raul Gracia-Tinedo (raulgraciatinedo@gmail.com)
 *
 */
public class ScanDatabaseAndStoreHistogram {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		DataScanner dataScanner = new DataScanner();
		dataScanner.scanDirectory("/home/raul/Desktop/synthetic_32KB_payload", 256*1024);
		//Histogram histogram = dataScanner.getHistogram();
		//original_128KB_payload.sql, original_output.txt
		//histogram.finishScan();
		//Utils.serializeObject(histogram, "/home/raul/Desktop/linkbench.ser");
		//System.out.println(histogram.toString());
		//TestUtils.writeMapValuesToFile(histogram, "synthetic_db_32KB_payload.dat");

		/*histogram = dataScanner.scanDirectory(
				"/home/raul/Desktop/synthetic_128KB_payload/", 256*1024);
		histogram.finishScan();
		System.out.println(histogram.toString());
		//Utils.serializeObject(histogram, "linkbench.ser");
		//System.out.println(((Histogram)Utils.deserializeObject("linkbench.ser")).toString());
		TestUtils.writeKeysFrequenciesToFile(histogram, "synthetic_128KB_payload_d.dat");*/
		System.exit(0);
	}

}
