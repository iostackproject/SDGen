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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.ibm.generation.SyntheticByteArrayBuilder;
import com.ibm.utils.Utils;

public class CompressionAndChunkTest {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		String compressionAlgorithm = "zlib";
		int max_chunk_size = 8*1024;
		
		FileWriter fileWriterFixed = new FileWriter(
			new File(compressionAlgorithm + "_results_fixed_size_chunks_" + 
					max_chunk_size/1024 + "KB.dat"));
		FileWriter fileWriterVariable = new FileWriter(
			new File(compressionAlgorithm + "_results_variable_size_chunks_"+ 
					max_chunk_size/1024 + "KB.dat"));

		//Execute the test
		for (int chunkSize = 1; chunkSize < max_chunk_size; chunkSize++){
			//List<Double> results_fixed = fixedChunkRandomAndZeroDataTest(testPath, chunkSize);
			List<Double> results_variable_size = 
					variableChunkRandomAndZeroDataTest(chunkSize, max_chunk_size-chunkSize);
			//TestUtils.writeTestResults(results_fixed, fileWriterFixed);
			TestUtils.writeTestResults(results_variable_size, fileWriterVariable);
		}
		fileWriterFixed.close();
		fileWriterVariable.close();
	}
	
	private static List<Double> variableChunkRandomAndZeroDataTest ( 
			int randomSize, int zeroSize) throws IOException {
		SyntheticByteArrayBuilder builder = new SyntheticByteArrayBuilder();

		byte[] randomData = builder.getRandomByteArray(randomSize);
		byte[] zeroData = builder.getZeroData(zeroSize);
		List<Double> results = new ArrayList<Double>();
		
		// Write the merged arrays
		byte[] mergedArray = Utils.joinByteArrays(randomData, zeroData);
		Utils.shuffle(mergedArray);
		//double mergedArrayEntropy = Utils.getShannonEntropy(mergedArray);
		results.add(Utils.compressAndCompare(mergedArray));	
		results.add(((double)randomSize));
		//results.add(mergedArrayEntropy);
		// Write shuffle arrays
		//Utils.shuffle(mergedArray);
		//double shuffledArrayEntropy = Utils.getShannonEntropy(mergedArray);
		//results.add(Utils.compressAndCompare(mergedArray));
		//results.add(shuffledArrayEntropy);
		return results;
	}
}