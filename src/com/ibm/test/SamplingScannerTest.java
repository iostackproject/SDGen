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

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.ibm.characterization.DatasetCharacterization;
import com.ibm.config.PropertiesStore;
import com.ibm.config.PropertyNames;
import com.ibm.generation.DataProducer;
import com.ibm.scan.DataScanner;
import com.ibm.utils.Utils;

/**
 * @author Raul Gracia-Tinedo (raulgraciatinedo@gmail.com)
 * 
 * This test has been specifically created to test the accuracy of generating
 * data with sampling on large datasets.
 * 
 * Potential problems: memory consumption of generating large datasets with SDGen.
 * We should do more inspection and tests to verify if there is any memory leakage.
 * 
 * Procedure: gather the dataset in a single file to facilitate the scan process (.tar).
 * Point the datasetPath field to that file and set the syntheticDatasetPath to the synthetic
 * file that should be similar in terms of compression to the original one. The chunk size
 * will be the same for the scan and for the generation process. After this process finishes
 * you would have two files: the original and the synthetic. Then, you should compress both
 * datasets with a compression engine and verify that the compression time and compression
 * ratios are similar (even other metrics like CPU usage would be interesting). If these
 * metrics are similar, this means that both datasets are similar from the compression 
 * algorithm viewpoint. Also check the size of the dataset_characterization.ser file, to
 * see that with small characterizations we can get good accuracy.
 *
 */
public class SamplingScannerTest {

	//directory where datasets are
	private static String datasetDirectory = "/home/raul/Desktop/"; 
	
	//dataset to scan
	private static String datasetName = "text1.tar"; 
	 
	//Output file obtained from the data generation process
	private static String syntheticDataset = "/home/raul/Desktop/syntheticFile"; 
	
	//Chunk size specified in the properties file
	private static int chunkSize = PropertiesStore.getInt(PropertyNames.GENERATION_CHUNK_SIZE); 
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {		
		//SCAN PHASE
		
		long fileSize = Utils.getFileSize(datasetDirectory + datasetName);
		//Instantiate the scanner	
		DataScanner scanner = new DataScanner();
		long time = System.currentTimeMillis();
		//Scan the original dataset
		scanner.scan(datasetDirectory + datasetName, chunkSize);
		//Build and persist our characterization
		DatasetCharacterization characterization = scanner.finishScanAndBuildCharacterization();
		//Output the sanning time
		System.out.println((fileSize/(1024*1024))/((System.currentTimeMillis()-time)/1000.0) + " MBps");
		// Save the characterization file
		characterization.save(datasetDirectory + "dataset_characterization.ser");
		
		//GENERATION PHASE
		
		//Instantiate the generator	
		int syntheticDataSize = 0; 
		DataProducer generator = new DataProducer(characterization);
		generator.startProducing();
		//Output for the synthetic file
		BufferedOutputStream syntheticFileWriter = new BufferedOutputStream(new FileOutputStream(syntheticDataset));
		time = System.currentTimeMillis();
		ByteArrayOutputStream bOutputStream = new ByteArrayOutputStream();
		//Generate a file of the same size of the original one
		while (syntheticDataSize<fileSize) {
			byte[] chunk = generator.getSyntheticData();
			syntheticFileWriter.write(chunk);
			syntheticDataSize+=chunkSize;
		}
		//Close files
		syntheticFileWriter.write(bOutputStream.toByteArray());
		bOutputStream.close();
		syntheticFileWriter.close();
		generator.endProducing();
		System.out.println((syntheticDataSize/(1024*1024))/((System.currentTimeMillis()-time)/1000.0) + " MBps");	
		System.exit(0);
	}
}