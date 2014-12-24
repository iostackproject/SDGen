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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.ibm.characterization.AbstractChunkCharacterization;
import com.ibm.characterization.DatasetCharacterization;
import com.ibm.characterization.user.MotifChunkCharacterization;
import com.ibm.config.PropertiesStore;
import com.ibm.config.PropertyNames;
import com.ibm.generation.DataProducer;
import com.ibm.scan.CompressionTimeScanner;
import com.ibm.scan.DataScanner;
import com.ibm.utils.Utils;

/**
 * @author Raul Gracia-Tinedo (raulgraciatinedo@gmail.com)
 *
 */
public class CompressionAndPerformanceMotifGeneratorTest {

	private static String testPath = "compressionAndPerformanceMotifTest/";
	
	private static int chunkSize = PropertiesStore.getInt(PropertyNames.GENERATION_CHUNK_SIZE);
	
	private static String dataType = "text";

	private static String datasetPath = "/home/raul/Desktop/";
	 
	private static String syntheticDatasetPath = "/home/raul/Desktop/syntheticFile";
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		String[] datasets = {
				//"sensor_network" ,
				//"os_images"
				//"pdf",  
				//"media",
				//"allVideo.tar",
				//"silesia",
				"text"
		};
		Utils.createDirectory(testPath);
		for (String dataset: datasets){
			System.out.println("Working with dataset: " + dataset);
			dataType = dataset;
			doTest(datasetPath + dataType + ".tar", syntheticDatasetPath + dataType + ".tar");
			System.gc();
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.exit(0);
	}
	
	public static void doTest(String originalDataset, String syntheticDataset) throws IOException {
		//Results and output	
		DataScanner scanner = new DataScanner();
		long time = System.currentTimeMillis();
		scanner.scan(originalDataset, chunkSize);
		DatasetCharacterization characterization = scanner.finishScanAndBuildCharacterization();
		characterization.save(datasetPath + "test_char.ser");
		//DatasetCharacterization newCharacterization = new DatasetCharacterization();
		//newCharacterization.load(datasetPath + "test_char.ser");
		System.out.println("Scanning time: " + (System.currentTimeMillis()-time)/1000.0);
		//Generate synthetic data
		DataScanner syntheticScanner = new DataScanner();		
		int syntheticDataSize = 0; 
		DataProducer generator = new DataProducer(characterization);
		generator.startProducing();
		BufferedOutputStream syntheticFileWriter = new BufferedOutputStream(new FileOutputStream(syntheticDataset));
		long fileSize = Utils.getFileSize(originalDataset);
		long samples = (long) (fileSize/chunkSize);
		time = System.currentTimeMillis();
		ByteArrayOutputStream bOutputStream = new ByteArrayOutputStream();
		for (int i=0; i<samples; i++) {
			byte[] chunk = generator.getSyntheticData();
			syntheticScanner.scan(chunk);
			if (i%100==0){
				syntheticFileWriter.write(bOutputStream.toByteArray());
				bOutputStream = new ByteArrayOutputStream();
			}else bOutputStream.write(chunk);
			syntheticDataSize+=chunkSize;
		}
		syntheticFileWriter.write(bOutputStream.toByteArray());
		bOutputStream.close();
		syntheticFileWriter.close();
		syntheticScanner.finishScan();
		generator.endProducing();
		System.out.println((syntheticDataSize/(1024*1024))/((System.currentTimeMillis()-time)/1000.0) + " MBps");
		System.out.println("Synthetic data size: " + syntheticDataSize);
		System.out.println("Synthetic Deduplication: " + syntheticScanner.getDeduplicationRatio());
		//Utils.writeDataToFile(syntheticCompression.toString().getBytes(), 
		//		testPath + dataType + "_expected_compression_" + dataType + ".dat");
		/*try {
			TestUtils.writeMapToFile(scanner.getGlobalRepetitionLengthHistogram(), 
					dataType + "_original_repetitions_histogram.dat");
			TestUtils.writeMapToFile(scanner.getGlobalCompressionHistogram(), 
					dataType + "_original_compression_histogram.dat");
			TestUtils.writeMapToFile(scanner.getGlobalSymbolHistogram(), 
					dataType + "_original_symbol_histogram.dat");
			TestUtils.writeMapToFile(syntheticScanner.getGlobalRepetitionLengthHistogram(), 
					dataType + "_synthetic_repetitions_histogram.dat");
			TestUtils.writeMapToFile(syntheticScanner.getGlobalCompressionHistogram(), 
					dataType + "_synthetic_compression_histogram.dat");
			TestUtils.writeMapToFile(syntheticScanner.getGlobalSymbolHistogram(), 
					dataType + "_synthetic_symbol_histogram.dat");
			
		} catch (IOException e) {
			e.printStackTrace();
		}

		writeChunkCharacterization(scanner.getChunkCharacterization(), 
				dataType + "_original_char.dat");
		writeChunkCharacterization(syntheticScanner.getChunkCharacterization(), 
				dataType + "_synthetic_char.dat");

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		CompressionTimeScanner cts = new CompressionTimeScanner(testPath + 
				dataType + "_original_data_performance.dat");
		cts.scan(originalDataset, chunkSize);
		cts.finishScan();
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		cts = new CompressionTimeScanner(testPath + 
				dataType + "_synthetic_data_performance.dat", 
					syntheticScanner.getChunkCharacterization());
		cts.scan(syntheticDataset, chunkSize);
		cts.finishScan();
		getRelativeSyntheticDataAccuracy(dataType, testPath + dataType + 
			"_original_data_performance.dat", testPath + dataType + 
				"_synthetic_data_performance.dat");*/
	}
	
	/**
	 * @param chunkCharacterization
	 * @param writerOriginal 
	 * @throws IOException 
	 */
	private static void writeChunkCharacterization(List<AbstractChunkCharacterization> 
		chunkCharacterization, String fileName) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
		StringBuilder builder = new StringBuilder();
		for (AbstractChunkCharacterization chunk: chunkCharacterization){
			builder.append(((MotifChunkCharacterization) chunk).getCompressionRatio() + "\t");
			builder.append(((MotifChunkCharacterization) chunk).getRepetitionLengthHistogram().getHistogramBasedValue() + "\t");
			builder.append(((MotifChunkCharacterization) chunk).getSymbolHistogram().getMap().size() + "\n");
			writer.write(builder.toString());
			builder = new StringBuilder();
		}
		writer.close();
	}

	/**
	 * @param string
	 * @param string2
	 */
	private static void getRelativeSyntheticDataAccuracy(String id, 
			String originalResults, String syntheticResults) {
		BufferedReader readerOriginal = null;
		BufferedReader readerSynthetic = null;
		BufferedWriter resultsWritter = null;
		try {
			readerOriginal = new BufferedReader(new FileReader(originalResults));
			readerSynthetic = new BufferedReader(new FileReader(syntheticResults));
			resultsWritter = new BufferedWriter(new FileWriter(
					testPath + id + "_comparison.dat"));
			StringBuilder resultLine = new StringBuilder();
			while (true) {
				String originalLine = readerOriginal.readLine();
				String syntheticLine = readerSynthetic.readLine();
				if (originalLine==null || syntheticLine==null) break;
				String[] originalFields = originalLine.split("\t");
				String[] syntheticFileds = syntheticLine.split("\t");
				for (int i=0; i<originalFields.length; i++){
					/*System.out.println(Double.valueOf(originalFields[i])
							+ " / " + Double.valueOf(syntheticFileds[i]));*/
					resultLine.append(((Double.valueOf(syntheticFileds[i])-Double.valueOf(originalFields[i]))/
							Double.valueOf(originalFields[i]))*100);
					if (i<originalFields.length-1) resultLine.append("\t");
				}
				resultLine.append("\n");
				resultsWritter.write(resultLine.toString());
				resultLine = new StringBuilder();
			}
			readerOriginal.close();
			readerSynthetic.close();
			resultsWritter.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static byte[] getByteArrayOfAllFiles (String directory) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		for (File file: Utils.getDirectoryFiles(directory)){
			try {
				outputStream.write(TestUtils.getFileBytes(file.getAbsolutePath()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}	
		return outputStream.toByteArray();
	}
}