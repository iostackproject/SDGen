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
import java.util.Arrays;

import com.ibm.characterization.Histogram;
import com.ibm.compression.LZ4Compression;
import com.ibm.compression.ZlibCompression;
import com.ibm.config.PropertiesStore;
import com.ibm.config.PropertyNames;
import com.ibm.generation.SyntheticByteArrayBuilder;
import com.ibm.generation.user.MotifDataGenerator;
import com.ibm.scan.user.DataCompressibilityScanner;
import com.ibm.utils.Utils;

/**
 * @author Raul Gracia-Tinedo (raulgraciatinedo@gmail.com)
 *
 */
public class SingleBlockRepetitionsAndPerformanceTest {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		String originalFile = "/home/raul/Desktop/test_data/text/book2";
		String syntheticFile = "/home/raul/Desktop/test_data/text_" + 
				PropertiesStore.getString(PropertyNames.SCAN_COMP_ALG) + ".dat";
		int regionSize = 64*1024;
		//Read text file
		SyntheticByteArrayBuilder builder = new SyntheticByteArrayBuilder();
		byte[] originalData = Arrays.copyOfRange(TestUtils.getFileBytes(originalFile), 
				regionSize*3 , regionSize*4);
		DataCompressibilityScanner dataFeatures = new DataCompressibilityScanner();
		ZlibCompression zlibCompression = new ZlibCompression();//new LZ4Compression();
		//Scant the repetitions and character range
		//dataFeatures.scan(originalData, regionSize);
		//int rangeOfBytes = dataFeatures.getCharacterRange();
		Histogram repetitionsHistogram = dataFeatures.getHistogram();
		//Calculate compression time
		long originalTime = System.nanoTime();
		byte[] compressedData = zlibCompression.compress(originalData);
		originalTime = System.nanoTime() - originalTime;
		//Get compression ratio
		double originalCompressionRatio = Utils.getCompressionRatio(originalData, compressedData);
		System.out.println("Original Data -> Compression Ratio: " + originalCompressionRatio +
				" Compression Time: " + originalTime);
		//Generate similar data
		zlibCompression = new ZlibCompression();//new LZ4Compression();
		//System.out.println("Range of characters: " + rangeOfBytes);
		//MotifDataGenerator generator = new MotifDataGenerator(rangeOfBytes, 
		//		repetitionsHistogram);
		//byte[] syntheticData = generator.generate(originalCompressionRatio, regionSize);
		//Calculate compression time
		long time = System.nanoTime();
		//compressedData = zlibCompression.compress(syntheticData);
		time = System.nanoTime() - time;
		//Get compression ratio
		//double compressionRatio = Utils.getCompressionRatio(syntheticData, compressedData);
		//System.out.println("Synthetic Data -> Compression Ratio: " + compressionRatio +
		//	"(" + (compressionRatio/originalCompressionRatio)*100 + "%)\n" +
		//		" Compression Time: " + time + "(" + ((double)time/originalTime)*100 + "%)");
		TestUtils.writeMapToFile(repetitionsHistogram, "repetitionsHistogramBible.dat");
		
		dataFeatures = new DataCompressibilityScanner();
		//dataFeatures.scan(syntheticData, regionSize);
		repetitionsHistogram = dataFeatures.getHistogram();
		TestUtils.writeMapToFile(repetitionsHistogram, "syntheticRepetitionsHistogramBible.dat");
		System.exit(0);
	}
}