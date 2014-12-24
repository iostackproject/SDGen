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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import com.ibm.characterization.Histogram;
import com.ibm.compression.BzipCompression;
import com.ibm.compression.LZ4Compression;
import com.ibm.compression.LZMACompression;
import com.ibm.compression.ZlibCompression;
import com.ibm.generation.user.MotifDataGenerator;
import com.ibm.scan.DataScanner;
import com.ibm.utils.Utils;

/**
 * @author Raul Gracia-Tinedo (raulgraciatinedo@gmail.com)
 *
 */
public class DataFeatureScanTest {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		String testPath = "variable_size_motif_test/";
		Utils.createDirectory(testPath);
		PrintWriter printWriter = new PrintWriter(testPath+"128KB_results.dat");
		//Scan the target data
		String fileName = "/home/raul/Desktop/test_data/text/";
		int regionSize = 64*1024;
		DataScanner dataScanner = new DataScanner();
		dataScanner.scanDirectory(fileName, regionSize);
		//Histogram repetitions = dataScanner.getRepetitionsHistogram();
		/*try {
			//TestUtils.writeMapToFile(repetitions, "repetitionsHistogram.dat");
			//TestUtils.writeTestResults(dataScanner.getDataFeaturesScanner().getTimeSeriesAnalisys(), "repetitionsDistribution.dat");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		//int characterRange = dataScanner.getDataFeaturesScanner().getCharacterRange();
		//System.out.println(characterRange);
		//Generate data
		for (int i=1; i <= 10; i+=1){
			//double expectedCompression = dataScanner.getCompressionHistogram().getHistogramBasedValue();
			//MotifDataGenerator generator = new MotifDataGenerator(characterRange, repetitions);
			//byte [] syntheticData = generator.generate(expectedCompression, regionSize);
			ZlibCompression zlibCompression = new ZlibCompression();
			LZ4Compression lz4Compression = new LZ4Compression();
			BzipCompression bzipCompression = new BzipCompression();
			LZMACompression lzmaCompression = new LZMACompression();
			StringBuilder builder = new StringBuilder();
			//builder.append(expectedCompression + "\t");
			//double compressionRatio = Utils.getCompressionRatio(syntheticData, 
			//		zlibCompression.compress(syntheticData));
			/*builder.append(compressionRatio + "\t");
			//builder.append(expectedCompression + "\t");
			compressionRatio = Utils.getCompressionRatio(syntheticData, 
					lz4Compression.compress(syntheticData));
			builder.append(compressionRatio + "\t");
			//builder.append(expectedCompression + "\t");
			compressionRatio = Utils.getCompressionRatio(syntheticData, 
					bzipCompression.compress(syntheticData));
			builder.append(compressionRatio + "\t");
			//builder.append(expectedCompression + "\t");
			compressionRatio = Utils.getCompressionRatio(syntheticData, 
					lzmaCompression.compress(syntheticData));
			builder.append(compressionRatio + "\n");
			printWriter.write(builder.toString());
			System.out.println("Expected compression: " + expectedCompression + ", actual compression: " +
					Utils.characterizationCompressAndCompare(syntheticData));*/
		}
		printWriter.close();
		//TestUtils.writeMapToFile(repetitions, "allTextRepetitions.dat");
		System.exit(0);
	}
}
