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

import com.ibm.scan.DataScanner;
import com.ibm.utils.Utils;

/**
 * @author Raul Gracia-Tinedo (raulgraciatinedo@gmail.com)
 *
 */
public class CombinedCompressionAndDeduplicationScanTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String fileName = "/home/raul/Desktop/test_data/text/";
		int regionSize = 128*1024;
		long time = System.currentTimeMillis();
		DataScanner dataScanner = new DataScanner();
		dataScanner.scanDirectory(fileName, regionSize);
		//System.out.println(dataScanner.getCompressionHistogram().toString());
		//System.out.println(dataScanner.getDeduplicationHistogram().toString());
		System.out.println(dataScanner.getDeduplicationRatio());
		System.out.println((System.currentTimeMillis()-time)/1000);
		System.out.println((Utils.getFileSize(fileName)/(1024.0*1024)));
		System.out.println("Throughput (MBps): " + 
				(((Utils.getFileSize(fileName)/(1024.0*1024)))/((System.currentTimeMillis()-time)/1000.0)));
		//dataScanner.getDeduplicationScanner().getTimeSeriesAnalisys();
		System.exit(0);
	}
}
