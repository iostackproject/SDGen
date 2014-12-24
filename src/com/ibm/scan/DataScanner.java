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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

import com.ibm.characterization.AbstractChunkCharacterization;
import com.ibm.characterization.DatasetCharacterization;
import com.ibm.characterization.DoublesHistogram;
import com.ibm.characterization.Histogram;
import com.ibm.characterization.IntegersHistogram;
import com.ibm.characterization.user.MotifChunkCharacterization;
import com.ibm.config.PropertiesStore;
import com.ibm.config.PropertyNames;
import com.ibm.deduplication.ContentBasedChunking;
import com.ibm.deduplication.Finger;

/**
 * This class is intended to coordinate the process of scanning datasets.
 * It works as follows: i) It loads the user defined chunk scanners specified
 * in the configuration file, ii) then, it digests every chunk of the dataset
 * feeding in parallel the available chunk scanners, iii) for each chunk of data,
 * it creates a chunk characterization object that is inserted into a dataset
 * characterization object when the scan finished and the buildCharacterization
 * method is invoked. Apart from chunk scanners, there are also global scanners
 * that can digest data; in this case, we mainly use this to detect the deduplication
 * ratio of a dataset.
 * 
 * @author Raul Gracia-Tinedo (raulgraciatinedo@gmail.com)
 *
 */
public class DataScanner extends AbstractScanner{
	
	private static final String scannersPackage = "com.ibm.scan.user."; 
	private static final String characterizationPackage = "com.ibm.characterization.user."; 
	
	/*Chunk granularity of the scan process*/
	private int chunkSize = PropertiesStore.getInt(PropertyNames.GENERATION_CHUNK_SIZE);
	private boolean enableDedupGeneration = PropertiesStore.getBoolean(PropertyNames.ENABLE_DEDUP);
			
	/*Scanners at chunk and dataset levels*/
	private List<Class<? extends AbstractChunkScanner>> chunkScannersClasses = 
			new ArrayList<Class<? extends AbstractChunkScanner>>();
	private Class<? extends AbstractChunkCharacterization> characterizationClass = null;
	
	/*Scanners and global dataset info*/
	private ContentBasedChunking globalDeduplicationScanner = new ContentBasedChunking();
	
	private final ForkJoinPool scannerWorkerPool = new ForkJoinPool();
	
	/*Scan information goes to these fields*/
	private List<AbstractChunkCharacterization> chunkCharacterization = new ArrayList<>();
	private double deduplicationRatio = 0.0;
	
	//For debug purposes
	private Histogram globalCompressionHistogram = new DoublesHistogram();
	private Histogram globalRepetitionLengthHistogram = new IntegersHistogram();
	private Histogram globalSymbolHistogram = new IntegersHistogram();	
	
	@SuppressWarnings("unchecked")
	public DataScanner() {
		String[] scanners = PropertiesStore.getStrings(PropertyNames.CHUNK_SCANNERS);
		try {
			characterizationClass = (Class<? extends AbstractChunkCharacterization>) 
				Class.forName(characterizationPackage + 
					PropertiesStore.getString(PropertyNames.CHUNK_CHARACTERIZATION));
			for (String scannerName: scanners){
				chunkScannersClasses.add((Class<? extends AbstractChunkScanner>) 
					Class.forName(scannersPackage + scannerName));
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This method takes as input a chunk of data of the original dataset
	 * to scan. We exploit parallelism by scanning the data with different
	 * scanners at the same time.
	 * 
	 * @param data
	 */
	@Override
	public void scan(byte[] data) {		
		//Instantiate a new chunk characterization loaded from the configuration
		AbstractChunkCharacterization chunk = null;
		try {
			chunk = (AbstractChunkCharacterization) characterizationClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		//Instantiate a list of chunk scanners
		List<AbstractChunkScanner> chunkScanners = initializeChunkScanners(data);
		//Execute chunk feature extracting in parallel
		for (AbstractChunkScanner scanner: chunkScanners) 
			scannerWorkerPool.execute(scanner);
		//Third, scan for deduplication if necessary
		if (enableDedupGeneration) 
			globalDeduplicationScanner.digest(data);
		//Wait for scan termination
		for (AbstractChunkScanner scanner: chunkScanners) 
			scanner.join();
		//Set the scan information into the characterization object		
		for (AbstractChunkScanner scanner: chunkScanners) 
			scanner.setInfo(chunk);		
		chunk.setSize(data.length);	
		//For debug purposes
		globalRepetitionLengthHistogram.mergeHistograms(
				((MotifChunkCharacterization)chunk).getRepetitionLengthHistogram());
		//Add chunk to characterization
		chunkCharacterization.add(chunk);
	}
	/**
	 * This function instantiates fresh scanner objects and feeds them with a 
	 * data chunk to be scanned.
	 * 
	 * @param data
	 * @return scanners
	 */
	private List<AbstractChunkScanner> initializeChunkScanners(byte[] data) {
		List<AbstractChunkScanner> chunkScanners = new ArrayList<AbstractChunkScanner>();
		for (Class<? extends AbstractChunkScanner> scannerClass: chunkScannersClasses){
			try {
				chunkScanners.add((AbstractChunkScanner) 
					scannerClass.getConstructor(byte[].class).newInstance(data));
			} catch (InstantiationException | IllegalAccessException 
				| IllegalArgumentException | InvocationTargetException | 
				NoSuchMethodException | SecurityException e) {
			}
		}
		return chunkScanners;
	}

	@Override
	public void finishScan() {
		scannerWorkerPool.shutdown();
		deduplicationRatio = globalDeduplicationScanner.calculateDeduplicationRatio();
		setDeduplicationInfoToChunks();
		//Delete memory objects related with this scan
		globalDeduplicationScanner.reset();
		System.out.println("Number of characterization chunks: " + 
				chunkCharacterization.size());
	}
	
	/**
	 * Create a {@link DatasetCharacterization} object representing the
	 * data scanned.
	 */
	public DatasetCharacterization buildCharacterization() {
		DatasetCharacterization characterization = new DatasetCharacterization();		
		List<AbstractChunkCharacterization> cloneChunkCharacterization = new ArrayList<>();
		for (AbstractChunkCharacterization chunk: chunkCharacterization){
			try {
				cloneChunkCharacterization.add((AbstractChunkCharacterization) chunk.clone());
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
		}
		characterization.setChunkCharacterization(cloneChunkCharacterization);
		characterization.setDeduplicationRatio(deduplicationRatio);
		return characterization;
	}
	
	public DatasetCharacterization finishScanAndBuildCharacterization() {
		finishScan();
		return buildCharacterization();
	}	
	
	private void setDeduplicationInfoToChunks() {
		Iterator<Finger> breakpoints = globalDeduplicationScanner.getBreakpoints().iterator();
		double deduplicatedData = 0.0;
		while (breakpoints.hasNext()){
			Finger finger = breakpoints.next();
			long fingerRepetitions = globalDeduplicationScanner.getFingerprints().get(finger);
			if (fingerRepetitions > 1){
				int chunkIndex = (int) (finger.getPosition()/chunkSize);
				long fingerDeduplication = finger.getLength()-(finger.getLength()/fingerRepetitions);
				deduplicatedData += fingerDeduplication;
				//Set the position in relative terms
				chunkCharacterization.get(chunkIndex).incrementDeduplicatedData((int)fingerDeduplication);
			}
		}		
		deduplicationRatio = globalDeduplicationScanner.calculateDeduplicationRatio();
		System.out.println("Deduplicated data: " + deduplicatedData/(1024.0*1024.0) + "MB" +
				" (dedup ratio=" + deduplicationRatio + ")");
	}
	/**
	 * @return the globalRepetitionLengthHistogram
	 */
	public Histogram getGlobalRepetitionLengthHistogram() {
		return globalRepetitionLengthHistogram;
	}

	/**
	 * @param globalRepetitionLengthHistogram the globalRepetitionLengthHistogram to set
	 */
	public void setGlobalRepetitionLengthHistogram(
			Histogram globalRepetitionLengthHistogram) {
		this.globalRepetitionLengthHistogram = globalRepetitionLengthHistogram;
	}

	/**
	 * @return the chunkCharacterization
	 */
	public List<AbstractChunkCharacterization> getChunkCharacterization() {
		return chunkCharacterization;
	}

	/**
	 * @param chunkCharacterization the chunkCharacterization to set
	 */
	public void setChunkCharacterization(
			List<AbstractChunkCharacterization> chunkCharacterization) {
		this.chunkCharacterization = chunkCharacterization;
	}

	/**
	 * @return the globalCompressionHistogram
	 */
	public Histogram getGlobalCompressionHistogram() {
		return globalCompressionHistogram;
	}

	/**
	 * @param globalCompressionHistogram the globalCompressionHistogram to set
	 */
	public void setGlobalCompressionHistogram(Histogram globalCompressionHistogram) {
		this.globalCompressionHistogram = globalCompressionHistogram;
	}

	/**
	 * @return the globalSymbolHistogram
	 */
	public Histogram getGlobalSymbolHistogram() {
		return globalSymbolHistogram;
	}

	/**
	 * @param globalSymbolHistogram the globalSymbolHistogram to set
	 */
	public void setGlobalSymbolHistogram(Histogram globalSymbolHistogram) {
		this.globalSymbolHistogram = globalSymbolHistogram;
	}	
	
	public double getDeduplicationRatio() {
		return deduplicationRatio;
	}
}