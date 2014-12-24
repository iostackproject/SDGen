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
package com.ibm.benchmarks.impressions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import com.ibm.characterization.DatasetCharacterization;
import com.ibm.generation.DataProducer;

/**
 * @author Raul Gracia-Tinedo (raulgraciatinedo@gmail.com)
 *
 */
public class ImpressionsExecutor {

	private static DataProducer producer = null;
	
	private static final String FIFO = "generation_pipe";

	private static final String FIELDS_SEPARATOR = " ";
	
	private static final String QUIT = "quit";
	
	public static void main(String[] args) throws IOException, InterruptedException {
		System.out.println("Synthetic Data Generator: Execution entry point...");
		System.out.println("Characterization path: " + args[0]);
		BufferedReader in = null;
		int servedRequests = 0;
		try {
			in = new BufferedReader(new FileReader(FIFO));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}	
		System.out.println("Synthetic Data Generator: Opened input requests file...");
		//Load the characterization
		DatasetCharacterization characterization = new DatasetCharacterization();
		//The characterization to be loaded should be passed by parameter
		characterization.load(args[0]);
		System.out.println("Synthetic Data Generator: Loaded characterization...");
		//Start the producer process
		producer = new DataProducer(characterization);
		producer.startProducing();
		System.out.println("Synthetic Data Generator: Started data producer...");
		//Listen to the input		
		boolean finish = false;
		String request, prevRead = "";
		while(!finish){
			if((request=in.readLine())!=null) {
				request = prevRead + request; 
				if (request.startsWith(QUIT)) finish = true;
				//There request is not complete (partially written)
				String[] fields = request.split(FIELDS_SEPARATOR);
				if (fields.length < 2) {
					prevRead = request;
				} else {
					//Request arguments
					System.out.println("Request: " + request);
					processFileRequest(fields[0], Integer.valueOf(fields[1]));
					servedRequests++;
					if (servedRequests%100==0) 
						System.out.println("Served Requests: " + servedRequests);
					prevRead = "";
				}
			//Wait for some input in the pipe
			}else Thread.sleep(10);						
	    }	
		System.out.println("Shutting down sythetic data generator...");
		in.close();
		producer.endProducing();
		System.exit(0);
	}
	/**
	 * @throws IOException 
	 * 
	 */
	private static void processFileRequest(String filePath, int size) throws IOException {
		int writtenBytes = 0;
		byte[] toWrite;
		File file = new File(filePath);
		file.getParentFile().mkdirs();
		FileOutputStream fw = new FileOutputStream(file);
		while (writtenBytes<size) {
			toWrite = producer.getSyntheticData();
			if (toWrite.length>size){
				fw.write(Arrays.copyOfRange(toWrite, 0, size));
			}else fw.write(toWrite);
			writtenBytes+=toWrite.length;
		}
		fw.close();		
	}		
}