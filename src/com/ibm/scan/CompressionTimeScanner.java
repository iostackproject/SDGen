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
package com.ibm.scan;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.ibm.characterization.AbstractChunkCharacterization;
import com.ibm.test.TestUtils;

/**
 * @author Raul Gracia-Tinedo (raulgraciatinedo@gmail.com)
 *
 */
public class CompressionTimeScanner extends AbstractScanner {

	private static final double ITERATIONS = 5;
	private FileWriter results = null;
	private List<AbstractChunkCharacterization> chunks = null;	
	
	public CompressionTimeScanner(String outputFile, List<AbstractChunkCharacterization> chunks) {
		try {
			results = new FileWriter(outputFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.chunks = chunks;
	}
	
	/**
	 * @param string
	 */
	public CompressionTimeScanner(String outputFile) {
		try {
			results = new FileWriter(outputFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void fullScan(String fileName, int regionSize) {	    
		try {			
			FileInputStream is = new FileInputStream(new File(fileName));
		    byte[] chunk = null;
		    int bytesRead = 0;
		    while (true) {
		    	if (chunks!=null && !chunks.isEmpty()) 
		    		regionSize = chunks.remove(0).getSize();
		    	chunk = new byte[regionSize];
		    	bytesRead = is.read(chunk);
		    	if (bytesRead == -1) break;
		    	if (bytesRead < chunk.length) 
		    		chunk = Arrays.copyOfRange(chunk, 0, bytesRead);
		    	if (bytesRead > 128) 
		    		scan(chunk);
		    }
		    is.close();
		} catch (FileNotFoundException fileNotFoundException) {
		    fileNotFoundException.printStackTrace();
		} catch (IOException ioException) {
		    ioException.printStackTrace();
		}
	}	
	
	@Override
	public void scan(byte[] toScan) {
		List<Double> times = new ArrayList<>();
		//Add ITERATION samples of compression times		
		for (int i=0; i < ITERATIONS; i++){
			List<Double> iterationTimes = TestUtils.calculateCompressionPerformance(toScan);
			if (times.isEmpty()){
				times.addAll(iterationTimes);
			}else{
				for (int j=0; j < iterationTimes.size();j++)
					times.set(j, times.get(j)+iterationTimes.get(j));
			}
		}
		//Divide the accumulated results by ITERATIONS
		for (int j=0; j < times.size();j++)
			times.set(j, times.get(j)/ITERATIONS);
		TestUtils.writeTestResults(times, results);
	}

	@Override
	public void finishScan() {
		try {
			results.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}