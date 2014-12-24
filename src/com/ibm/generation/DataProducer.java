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
package com.ibm.generation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.ibm.characterization.AbstractChunkCharacterization;
import com.ibm.characterization.DatasetCharacterization;
import com.ibm.config.PropertiesStore;
import com.ibm.config.PropertyNames;
import com.ibm.exception.DataGenerationException;

/**
 * This class coordinates the generation of synthetic data. Basically,
 * it works as follows: i) This class loads the characterization of a
 * dataset and stores it at the characterization field, ii) then, it
 * initializes the worker tasks that will generate data, iii) finally,
 * for each getSyntheticData call this class generates a new synthetic 
 * chunk resorting to a chunk characterization included in the 
 * DatasetCharacterization field. This class also provides some basic
 * form of deduplicated data generation.  
 * 
 * @author Raul Gracia-Tinedo (raulgraciatinedo@gmail.com)
 *
 */
public class DataProducer {
	
	private static final String generationPath = "com.ibm.generation.user.";
	
	/*Data structures needed to generate synthetic data*/
	private DatasetCharacterization characterization;
	private FitnessProportionateSelection<AbstractChunkCharacterization> dedupSelector = null;
	private Set<Long> alreadyGeneratedChunk = new HashSet<Long>();
	private boolean enableDedupGeneration = PropertiesStore.getBoolean(PropertyNames.ENABLE_DEDUP);
	
	/*Data structures and field for the production process*/
	private static final int parallelsm = 4;
	private static final int pollDataTimeoutInSecs = 10;
	private BlockingQueue<byte[]> dataQueue = new ArrayBlockingQueue<byte[]>(parallelsm);
	private List<DataProducerTask> producers = new ArrayList<DataProducerTask>();
	private ExecutorService threadPool = Executors.newFixedThreadPool(parallelsm);
	private ByteBuffer currentChunk = ByteBuffer.allocate(
			PropertiesStore.getInt(PropertyNames.SCAN_CHUNK_SIZE));
	
	public DataProducer(DatasetCharacterization characterization) {
		this.characterization = characterization;
		if (enableDedupGeneration) initializeDedupSelector();
	}

	public void startProducing () {
		for (int i=0; i<parallelsm; i++){
			DataProducerTask producer = new DataProducerTask();
			producers.add(producer);
			threadPool.execute(producer);
		}
		//Initialize first chunk
		try {
			currentChunk.put(dataQueue.poll(pollDataTimeoutInSecs, TimeUnit.SECONDS));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		currentChunk.position(0);
	}
	
	public void endProducing() {
		System.out.println("Finishing tasks...");
		for (DataProducerTask task: producers){
			task.finish = true;
		}
		System.out.println("Clearing queue...");
		dataQueue.clear();		
		System.out.println("Tearing down pool...");
		threadPool.shutdown();
		currentChunk.clear();
	}
	/**
	 * This method return a chunk of synthetic with the same size
	 * as the characterization specifies.
	 * 
	 * @return synthetic chunk
	 */
	public byte[] getSyntheticData() {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			out.write(currentChunk.array());
			currentChunk.rewind();
			currentChunk.put(dataQueue.poll(pollDataTimeoutInSecs, TimeUnit.SECONDS));
			currentChunk.position(0);
			return out.toByteArray();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.err.println("No production of data!!");
		return null;
	}
	/**
	 * This method produces an arbitrary amount of synthetic data.
	 * Its use recommended when the size of writes is smaller that the
	 * average chunk size in the characterization to avoid wasting
	 * already generated synthetic content. 
	 * 
	 * @param size
	 * @return synthetic data
	 */
	public synchronized byte[] getSyntheticData(int size) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] smallWrite = new byte[Math.min(size, currentChunk.remaining())];
		currentChunk.get(smallWrite);
		try {
			out.write(smallWrite);
			//Renew the current chunk
			if (!currentChunk.hasRemaining()){ 
				currentChunk.rewind();
				currentChunk.put(dataQueue.poll(pollDataTimeoutInSecs, TimeUnit.SECONDS));
				currentChunk.position(0);
			}
			//If there is more data to create, just write more
			while (out.size() < size)		
				out.write(dataQueue.poll(pollDataTimeoutInSecs, TimeUnit.SECONDS));
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		return (out.size()!=size)? Arrays.copyOf(out.toByteArray(), size): out.toByteArray();
	}

	private void initializeDedupSelector() {
		Map<AbstractChunkCharacterization, Long> chunkAndDedupData = new HashMap<>();
		for (AbstractChunkCharacterization chunk: characterization.getChunkCharacterization()){
			chunkAndDedupData.put(chunk, (long) chunk.getDeduplicatedData());
		}
		dedupSelector = new FitnessProportionateSelection<AbstractChunkCharacterization>(chunkAndDedupData);
	}
	
	private class DataProducerTask implements Runnable{
		
		boolean finish = false; 
		private long deduplicatedData = 0;
		private AbstractDataGeneratorFactory generatorFactory;
		
		public DataProducerTask() {
			try {
				String generator = PropertiesStore.getString(PropertyNames.DATA_GENERATION);
				generatorFactory = (AbstractDataGeneratorFactory) 
					Class.forName(generationPath + generator + "Factory").newInstance();
			} catch (InstantiationException | IllegalAccessException
					| ClassNotFoundException e) {
				e.printStackTrace();
			} 
		}

		@Override
		public void run() {
			/*Producer tasks will run until they are notified to end*/
			Random random = new Random();
			while (!finish) {								
				AbstractChunkCharacterization chunk = null;
				//Decide whether to introduce a deduplicated block or not
				boolean deduplicatedChunk = enableDedupGeneration &&
						(random.nextDouble() < characterization.getDeduplicationRatio());
				//At the moment, we opt to generate data by doing a circular
				//walk over the chunk characterization
				try {
					if (deduplicatedChunk) {					
						chunk = dedupSelector.generateProportionalKeys();
					}else {
						chunk = characterization.getNextChunkInCircularOrder();	
						if (!alreadyGeneratedChunk.contains(chunk.getSeed())){
							alreadyGeneratedChunk.add(chunk.getSeed());
							deduplicatedChunk = true;
						}
					}
					//Generate the synthetic data and wait until the queue is free again
					boolean result = dataQueue.offer(generateData(chunk, deduplicatedChunk), 
									Long.MAX_VALUE, TimeUnit.SECONDS);
					if (!result) System.err.println("Problems inserting data in queue!");
				} catch (InterruptedException | DataGenerationException e) {
					e.printStackTrace();
				}
			}
			System.out.println("PRODUCED DEDUPLICATED DATA: " + deduplicatedData);
			System.out.println("Finishing producer task...");
		}
		
		private byte[] generateData(AbstractChunkCharacterization chunk, boolean deduplicated) {
			//Instantiate the data generator with the appropriate information
			AbstractDataGenerator dataGenerator = generatorFactory.create();
			//Set the info to the data generator for building the synthetic chunk
			dataGenerator.initialize(chunk);
			//Discriminate between deduplicated block or not 
			return dataGenerator.generate(deduplicated);
		}		
	}
	
	/**
	 * @return the characterization
	 */
	public DatasetCharacterization getCharacterization() {
		return characterization;
	}

	/**
	 * @param characterization the characterization to set
	 */
	public void setCharacterization(DatasetCharacterization characterization) {
		this.characterization = characterization;
	}
}