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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import com.ibm.characterization.DoublesHistogram;
import com.ibm.characterization.Histogram;
import com.ibm.compression.BzipCompression;
import com.ibm.compression.LZ4Compression;
import com.ibm.compression.LZMACompression;
import com.ibm.compression.ZlibCompression;
import com.ibm.compression.AbstractCompression;
import com.ibm.config.PropertiesStore;
import com.ibm.config.PropertyNames;
import com.ibm.utils.Utils;

/**
 * @author Raul Gracia-Tinedo (raulgraciatinedo@gmail.com)
 *
 */
public class TestUtils {
	
	private static String compressorsPath = "com.ibm.compression.";
	private static List<Class<? extends AbstractCompression>> compressorClasses =
			new ArrayList<Class<? extends AbstractCompression>>();
	
	static {
		for (String compressorClassName: PropertiesStore.getStrings(PropertyNames.TEST_COMPRESSORS)) {
			try {
				compressorClasses.add((Class<? extends AbstractCompression>) 
					Class.forName(compressorsPath + compressorClassName));
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void writeTestResults(List<Double> results, String fileName) throws IOException{
		FileWriter fileWriter = new FileWriter(new File(fileName));
		writeTestResults(results, fileWriter);
		fileWriter.close();
	}
	
	public static void writeTestResults(List<Double> results, FileWriter fileWriter){
		StringBuilder builder = new StringBuilder();
		for (double result: results) {
			builder.append(result);
			builder.append("\t");
		}
		builder.append("\r\n");
		try {
			fileWriter.write(builder.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void writeMapValuesToFile (Histogram histogram, String fileName) throws IOException {
		FileWriter fileWriter = new FileWriter(new File(fileName));
		for (Integer key: histogram.getMap().keySet()){
			for (long l = 0; l < histogram.getMap().get(key); l++){
				fileWriter.write(new Double(key*DoublesHistogram.BIN_SIZE).toString() + "\n");
			}
		}
		fileWriter.close();
	}
	public static void writeMapToFile (Histogram histogram, String fileName) throws IOException {
		FileWriter fileWriter = new FileWriter(new File(fileName));
		for (Integer key: histogram.getMap().keySet()){
			fileWriter.write(key + "\t" + histogram.getMap().get(key) + "\t"+ 
					(histogram.getMap().get(key)/(double)histogram.getAccumulatedFrequencies()) + "\n");
		}
		fileWriter.close();
	}
	
	public static byte[] getFileBytes (String fileName) {
		int regionSize = 8*1024;
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			FileInputStream is = new FileInputStream(new File(fileName));
		    byte[] chunk = new byte[regionSize];
		    int bytesRead = 0;
		    while (true) {
		    	bytesRead = is.read(chunk);
		    	if (bytesRead == -1) break;
		    	if (bytesRead < chunk.length) chunk = Arrays.copyOfRange(chunk, 0, bytesRead);
		    	outputStream.write(chunk);
		    }
		    is.close();
		} catch (FileNotFoundException fileNotFoundException) {
		    fileNotFoundException.printStackTrace();
		} catch (IOException ioException) {
		    ioException.printStackTrace();
		}
		return outputStream.toByteArray();
	}
	
	public static List<Double> calculateCompressionPerformance (byte[] chunk) {
		List<Double> results = new ArrayList<Double>();
		//Get compression time, compression ratio and decompression time
		for (Class<? extends AbstractCompression> compression: compressorClasses){
			try {
				results.addAll(getCompressionTimesAndRatio(compression.newInstance(), chunk));
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		//Calculate data entropy
		results.add(Utils.getShannonEntropy(chunk));
		return results;
	}
	
	private static synchronized List<Double> getCompressionTimesAndRatio(AbstractCompression compressor, byte[] chunk) {
		List<Double> results = new ArrayList<Double>();
		//Calculate compression performance
		long time = System.nanoTime();
		byte[] syntheticData = compressor.compress(chunk);
		time = System.nanoTime() - time;
		results.add((double) time);
		results.add(Utils.getCompressionRatio(chunk, syntheticData));
		//Calculate decompression performance
		time = System.nanoTime();
		byte[] decompressed = compressor.decompress(syntheticData);
		time = System.nanoTime() - time;
		results.add((double) time);
		assert Arrays.equals(chunk, decompressed): "Wrong decompression!";
		return results;
	}
}
