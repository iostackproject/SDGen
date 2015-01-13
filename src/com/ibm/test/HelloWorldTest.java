package com.ibm.test;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.ibm.characterization.DatasetCharacterization;
import com.ibm.config.PropertiesStore;
import com.ibm.config.PropertyNames;
import com.ibm.generation.DataProducer;
import com.ibm.scan.CompressionTimeScanner;
import com.ibm.scan.DataScanner;
import com.ibm.utils.Utils;

public class HelloWorldTest {
	
	private static String datasetPath = "path_to_your_dataset";
	private static String originalDataset = datasetPath + "LargeCalgaryCorpus.tar";
	private static String syntheticDataset = datasetPath + "syntheticDataset";
	private static int chunkSize = PropertiesStore.getInt(PropertyNames.GENERATION_CHUNK_SIZE);
	
	public static void main(String[] args) throws IOException {
		//1) Scan a dataset. If the dataset is a collection of files, to run this test is better
		//to pack them into a .tar fall to do a fair comparison with the synthetic file generated.
		DataScanner scanner = new DataScanner();
		long time = System.currentTimeMillis();
		scanner.scan(originalDataset, chunkSize);
		
		//2) Here the scan process finishes. In this point we want to persist the characterization of this
		//dataset in a sharable data structure.
		DatasetCharacterization characterization = scanner.finishScanAndBuildCharacterization();
		characterization.save(datasetPath + "characterization.ser");
		
		//3) In this point, we load the dataset characterization just created to show how characterization can be loaded
		//and shared among users.
		DatasetCharacterization newCharacterization = new DatasetCharacterization();
		newCharacterization.load(datasetPath + "characterization.ser");
		System.out.println("Scanning time: " + (System.currentTimeMillis()-time)/1000.0);
		//Generate synthetic data
		int syntheticDataSize = 0; 
		
		//4)Now, we want to generate a synthetic dataset of the same size than the original one with similar synthetic data.
		DataProducer generator = new DataProducer(characterization);
		generator.startProducing();
		BufferedOutputStream syntheticFileWriter = new BufferedOutputStream(new FileOutputStream(syntheticDataset));
		//We assume that the dataset is a file (e.g. a single fiel or a .tar) to get the size in this way
		long fileSize = Utils.getFileSize(originalDataset);
		time = System.currentTimeMillis();
		ByteArrayOutputStream bOutputStream = new ByteArrayOutputStream();
		for (int i=0; i<fileSize/chunkSize; i++) {
			byte[] chunk = generator.getSyntheticData();
			if (i%100==0){
				syntheticFileWriter.write(bOutputStream.toByteArray());
				bOutputStream = new ByteArrayOutputStream();
			}else bOutputStream.write(chunk);
			syntheticDataSize+=chunkSize;
		}
		syntheticFileWriter.write(bOutputStream.toByteArray());
		bOutputStream.close();
		syntheticFileWriter.close();
		generator.endProducing();
		System.out.println((syntheticDataSize/(1024*1024))/((System.currentTimeMillis()-time)/1000.0) + " MBps");
		System.out.println("Synthetic data size: " + syntheticDataSize);
		
		//5) Finally, we compare the compression ratio and time of these datasets for various compression engines
		//This test consists of taking every chunk of data and compressing it with different compressors to
		//get the compression ratio and time. It is quite slow, so try first with small datasets.
		CompressionTimeScanner cts = new CompressionTimeScanner(datasetPath + "_original_data_performance.dat");
		cts.scan(originalDataset, chunkSize);
		cts.finishScan();
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		cts = new CompressionTimeScanner(datasetPath + "_synthetic_data_performance.dat");
		cts.scan(syntheticDataset, chunkSize);
		cts.finishScan();
		System.exit(0);
	}
}
