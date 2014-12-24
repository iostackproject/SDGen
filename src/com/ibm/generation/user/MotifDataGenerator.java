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
package com.ibm.generation.user;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.ibm.characterization.AbstractChunkCharacterization;
import com.ibm.characterization.Histogram;
import com.ibm.characterization.IntegersHistogram;
import com.ibm.characterization.user.MotifChunkCharacterization;
import com.ibm.generation.AbstractDataGenerator;

/**
 *
 */
public class MotifDataGenerator extends AbstractDataGenerator{

	public static int DEFAULT_MOTIF_BUFFER_SIZE = 268;
	
	private static int MAX_CHUNK_SIZE = DEFAULT_MOTIF_BUFFER_SIZE;
	
	private static final int MIN_NUM_SYMBOLS = 8;
	
	private static final int NUM_REPETITIONS_BUFFER = 1000;
	
	private double uniqueness;
	
	private MotifChunkCharacterization myChunk = null;
	
	//For debug purposes
	private Histogram myRepetitionDistribution = new IntegersHistogram();
	
	/**
	 * Buffer with a sequence of random bytes that are pasted into output.
	 * Starts off null, initialized on demand.
	 */
	private byte[] motif = new byte[MAX_CHUNK_SIZE];
	
	/** Size of motif buffer */
	private int motifBytes = MAX_CHUNK_SIZE;
	
	/**
	 * Constructor to use the motif generator with real data measurements.
	 * 
	 * @param rangeOfBytes
	 * @param repetitionDistribution
	 */
	public void initialize (AbstractChunkCharacterization chunk) {
		myChunk = (MotifChunkCharacterization) chunk;
	}	
	
	@Override
	public byte[] generate(boolean useSeed) {
		byte [] data = new byte[myChunk.getSize()];
		if (useSeed) {
			fill(new Random(myChunk.getSeed()), data);
		}else fill(new Random(), data);
		return data;
	}
	
	/**
	 * This method is used to generate synthetic data that meets compression
	 * ratio and times for a variety of compression engines. We  
	 * 
	 * @param rng
	 * @param data
	 */
	public void fill(Random rng, byte[] data) {
		//Set the desired compression ratio for the synthetic chunk
		uniqueness = 1.0/myChunk.getCompressionRatio();		
		//Chunk features
		int numSymbols = myChunk.getSymbolHistogram().getMap().keySet().size();
		//int meanSequenceLength = (int) repetitionDistribution.getHistogramMeanValue();		
		int renewalRate = Integer.MAX_VALUE; //(int) ((meanSequenceLength*numSymbols)/compression);		
		//Handle extreme data types	
		if (numSymbols < MIN_NUM_SYMBOLS) 
			renewalRate = numSymbols;	
		/*if (meanSequenceLength > MAX_MEAN_REPETITION_LENGTH) 
			renewalRate = (int) Math.sqrt(meanSequenceLength);*/
		int n = data.length, i=0, iterations=1;
		int chunk = -1;		
		//Initialize the first motif
		motif = initializeMotif(rng);	
		for (int j = 0; j < Math.min(motif.length, n-i); j++) {
			data[i] = motif[j];
			i+=1;					
		}
		//Flag to check if the previous sequence was a repetition
		boolean previousWasRepetition = false;
		//Repetitions will be added by groups and in decreasing order by length.
		//The reason is that algorithms like LZ4 do not account for the longest repetition 
		//but for the last one. Doing this we force the last repetition to be the longest.
		List<Integer> sequences = new ArrayList<Integer>();
		fillRepetitionLengthsInDescendingOrder(sequences, NUM_REPETITIONS_BUFFER, rng);
		while (i < n) {					
			//Refill the group of sequences
			if (sequences.isEmpty()) 
				fillRepetitionLengthsInDescendingOrder(sequences, NUM_REPETITIONS_BUFFER, rng);
			boolean createUniqueData = rng.nextDouble() < uniqueness;
			chunk = sequences.remove(0);//(createUniqueData) ? (int) getSequenceLength(rng): sequences.remove(0);
			//Check array boundaries
			if (i + chunk > n) chunk = n-i;
			if (createUniqueData) {
				System.arraycopy(getRandomData(chunk, rng), 0, data, i, chunk);
				previousWasRepetition = false;
			} else {					
				//We avoid the concatenation of large sequences together since
				//it overestimates the actual compression and biases the repetition
				//length distribution. For doing this we a a break byte.
				if (previousWasRepetition){// && chunk > meanSequenceLength) {
					data[i] = (byte) myChunk.getSymbolHistogram().getHistogramBasedValue(rng);
					i++;
					if (i + chunk > n) chunk = n-i;
				}
				previousWasRepetition = true;
				//Keep track of the repetitions distribution
				myRepetitionDistribution.add(chunk);
				//Introduce the already existing data 
				System.arraycopy(motif, 0, data, i, chunk);
				//For the aforementioned extreme data types, we renew the motif
				if (iterations%(renewalRate)==0){
					motif = initializeMotif(rng);
					for (int j = 0; j < Math.min(motif.length, n-i); j++) {
						data[i] = motif[j];
						i+=1;					
					}	
				}
				iterations++;
			}
			i+=chunk;
		}
	}

	//	PRIVATE METHODS --
	
	private int getSequenceLength(Random seed) {
		return (int) myChunk.getRepetitionLengthHistogram().getHistogramBasedValue(seed);
	}
	
	private byte[] initializeMotif(Random seed) {
		return getRandomData(motifBytes, seed); 
	}
	
	private byte[] getRandomData(int length, Random seed){
		byte[] data = new byte[length];
		for (int i = 0; i < length; i++) {
			data[i] = (byte) myChunk.getSymbolHistogram().getHistogramBasedValue(seed);
		}	
		return data;
	}

	private void fillRepetitionLengthsInDescendingOrder(List<Integer> sequences, 
			int numSequences, Random rng){
		for (int seqIndex = 0; seqIndex<numSequences; seqIndex++) 
			sequences.add((int) getSequenceLength(rng));
		Collections.sort(sequences, Collections.reverseOrder());
	}
	
	//	ACCESS METHODS --
	
	/**
	 * @return the uniqueness
	 */
	public double getUniqueness() {
		return uniqueness;
	}

	/**
	 * @param uniqueness
	 *            the uniqueness to set
	 */
	public void setUniqueness(double uniqueness) {
		this.uniqueness = uniqueness;
	}

	/**
	 * @return the motifBytes
	 */
	public int getMotifBytes() {
		return motifBytes;
	}

	/**
	 * @param motifBytes the motifBytes to set
	 */
	public void setMotifBytes(int motifBytes) {
		this.motifBytes = motifBytes;
	}
	/**
	 * @return the myRepetitionDistribution
	 */
	public Histogram getMyRepetitionDistribution() {
		return myRepetitionDistribution;
	}
	/**
	 * @param myRepetitionDistribution the myRepetitionDistribution to set
	 */
	public void setMyRepetitionDistribution(Histogram myRepetitionDistribution) {
		this.myRepetitionDistribution = myRepetitionDistribution;
	}
}