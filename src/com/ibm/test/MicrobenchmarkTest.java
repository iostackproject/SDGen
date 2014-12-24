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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.ibm.generation.SyntheticByteArrayBuilder;
import com.ibm.utils.Utils;

/**
 * @author Raul Gracia-Tinedo (raulgraciatinedo@gmail.com)
 *
 */
public class MicrobenchmarkTest {

	private static int chunkSize = 64*1024;
	
	private static int fileSize = 4*1024*1024;
	
	private static int characterRange = 255;

	private static String datasetPath = "/home/raul/Desktop/test_data/";
	
	private static Map<Integer, byte[]> motifs = new HashMap<>();
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		createRandomFile(fileSize, characterRange);
		createAllZeroFile(fileSize);
		createDefinableRepetitionFile(fileSize, characterRange, 4, true, 1.5);
		createDefinableRepetitionFile(fileSize, characterRange, 4, false, 1.5);
		createDefinableRepetitionFile(fileSize, characterRange, 4, true, 6);
		createDefinableRepetitionFile(fileSize, characterRange, 4, false, 6);
		createDefinableRepetitionFile(fileSize, characterRange, 50, true, 1.5);
		createDefinableRepetitionFile(fileSize, characterRange, 50, false, 1.5);
		createDefinableRepetitionFile(fileSize, characterRange, 50, true, 6);
		createDefinableRepetitionFile(fileSize, characterRange, 50, false, 6);
		System.exit(0);
	}

	/**
	 * @param fileSize2
	 * @param characterRange2
	 * @param i
	 * @param d
	 */
	private static void createDefinableRepetitionFile(int fileSize,
			int characterRange, int repetitionLength, boolean changeSequences, double compression) {
		double uniqueness = 1.0/compression;
		SyntheticByteArrayBuilder builder = new SyntheticByteArrayBuilder();
		Random rng = new Random();
		initializeMotifMap(characterRange);
		byte [] data = new byte[fileSize];
		int n = data.length, i=0; 
		while (i < n) {		
			int chunk = repetitionLength;
			if (i + chunk > n) chunk = n-i;
			if (rng.nextDouble() < uniqueness) {
				//Check the destination array boundary boundary
				//int chunkEnd  = Math.min(n, i + chunk);
				// New sequence of unique bytes
				for (int j = i; j < i+chunk; j++) {
					data[j] = (byte)(rng.nextInt(characterRange));					
				}
			}else{
				System.arraycopy(motifs.get(repetitionLength), 0, data, i, chunk);
				if (changeSequences && i%5==0) 
					motifs.put(repetitionLength, builder.getRandomByteArray(repetitionLength, characterRange));
			}
			i+=chunk;
		}
		
		Utils.writeDataToFile(data, datasetPath+"rep_len_" + 
				repetitionLength + "_vary_rep_" + changeSequences + "_comp_" + compression + ".txt");		
	}

	/**
	 * @param fileSize2
	 */
	private static void createAllZeroFile(int fileSize) {
		int size = 0;
		SyntheticByteArrayBuilder builder = new SyntheticByteArrayBuilder();
		FileOutputStream writer = null;
		try {
			writer = new FileOutputStream(datasetPath+"allZeroes.txt");
			while (size<fileSize){
					writer.write(builder.getZeroData(chunkSize));
					size+=chunkSize;
			}
			writer.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param fileSize2
	 * @param characterRange2
	 */
	private static void createRandomFile(int fileSize, int characterRange) {
		int size = 0;
		SyntheticByteArrayBuilder builder = new SyntheticByteArrayBuilder();
		FileOutputStream writer = null;
		try {
			writer = new FileOutputStream(datasetPath+"random.txt");		
			while (size<fileSize){
				writer.write(builder.getRandomByteArray(chunkSize, characterRange));
				size+=chunkSize;
			}
		writer.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void initializeMotifMap(int characterRange) {
		SyntheticByteArrayBuilder builder = new SyntheticByteArrayBuilder();
		//motifs.put(0, builder.getRandomByteArray(1, characterRange));
		for (int i=1; i < 1024; i++)
			//motifs.put(i, Utils.joinByteArrays(motifs.get(i-1), getRandomData(1)));
			motifs.put(i, builder.getRandomByteArray(i, characterRange));
	}
}