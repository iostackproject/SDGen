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
package com.ibm.test.performance;

import java.io.FileNotFoundException;

import com.ibm.characterization.DatasetCharacterization;
import com.ibm.generation.DataProducer;
import com.ibm.scan.DataScanner;

public class SyntheticDataGenerationPerformanceTest {
	
	public static void main(String[] args) throws FileNotFoundException {
		String datasetPath = "/home/user/Desktop/datasets/text.tar";
		int chunkSize = 32*1024;
		DataScanner scanner = new DataScanner();
		scanner.scan(datasetPath, chunkSize);
		DatasetCharacterization characterization = scanner.finishScanAndBuildCharacterization();
		int syntheticDataSize = 0; 
		DataProducer generator = new DataProducer(characterization);
		generator.startProducing();
		long time = System.currentTimeMillis();
		for (int i=0; i < 16000; i++) {//ChunkCharacterization cs: scanner.getChunkCharacterization()) {
			byte[] chunk = generator.getSyntheticData();
			syntheticDataSize+=chunk.length;
		}
		System.out.println((syntheticDataSize/(1024*1024))/((System.currentTimeMillis()-time)/1000.0) + " MBps");
		System.out.println("Generated data:" + (syntheticDataSize/(1024*1024)) + "MB");
		generator.endProducing();
		System.exit(0);
	}
}