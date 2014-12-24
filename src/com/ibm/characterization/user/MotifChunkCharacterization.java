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
package com.ibm.characterization.user;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.security.SecureRandom;
import java.util.List;

import com.ibm.characterization.AbstractChunkCharacterization;
import com.ibm.characterization.Histogram;
import com.ibm.characterization.IntegersHistogram;

/**
 * This class contains the necessary information to characterize
 * a original data chunk in terms of compression. With this information
 * we can produce synthetic data similar in terms of compression.
 * 
 * @author Raul Gracia-Tinedo (raulgraciatinedo@gmail.com)
 *
 */
public class MotifChunkCharacterization extends AbstractChunkCharacterization implements Serializable, Cloneable{
	
	private static final long serialVersionUID = 1L;
	
	/*As symbol is a byte within a chunk. In this histogram we capture
	 * the symbols that appear in a chunk and their frequency distribution.*/
	private Histogram symbolHistogram = new IntegersHistogram();
	/*Histogram of repetition lengths.*/
	private Histogram repetitionLengthHistogram = new IntegersHistogram();
	/*Other values that define the behavior of data*/
	private double compressionRatio = 0.0;	
	
	private static final String breakLine = "\n";
	private static final String histogramSeparator = " ";
	
	public MotifChunkCharacterization() {
		this.seed = new SecureRandom().nextLong();
	}
	
	@Override
	public void save(Writer outputFile) {
		try {
			outputFile.write(getCompressionRatio() + breakLine);
			outputFile.write(getDeduplicatedData() + breakLine);
			outputFile.write(getSeed() + breakLine);
			outputFile.write(getSize() + breakLine);				
			for (int key: getRepetitionLengthHistogram().getMap().keySet()){
				outputFile.write(key + histogramSeparator + 
					getRepetitionLengthHistogram().getMap().get(key) + breakLine);
			}
			outputFile.write(histogramSeparator + breakLine);
			for (int key: getSymbolHistogram().getMap().keySet()){
				outputFile.write(key + histogramSeparator + 
					getSymbolHistogram().getMap().get(key) + breakLine);
			}	
			//outputFile.write(histogramSeparator + breakLine);		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void load(List<String> chunkInfo) {
		int pointer = 0;
		String[] keyAndValue = null;
		for (String line: chunkInfo){
			///Create new instance and set deduplicated data
			if (line.equals(histogramSeparator)) {
				pointer++;
				//Skip the current line to parse symbols
				if (pointer==5) continue;
			}
			switch (pointer) {
				//Instantiate a new chunk characterization object and 
				//set the compression ratio
				case 0:
					setCompressionRatio(Double.valueOf(line));
					pointer++;
					break;
				//Set the deduplicated data of this chunk
				case 1:
					setDeduplicatedData(Integer.valueOf(line));
					pointer++;
					break;
				//Set the generation seed
				case 2:
					setSeed(Long.valueOf(line));
					pointer++;
					break;
				//Set the size of the chunk
				case 3:
					setSize(Integer.valueOf(line));
					pointer++;
					break;
				//Populate the repetition length histogram
				case 4:
					keyAndValue = line.split(histogramSeparator);
					getRepetitionLengthHistogram().getMap().put(
						Integer.valueOf(keyAndValue[0]), Long.valueOf(keyAndValue[1]));
					break;
				//Populate the symbol histogram
				case 5:
					keyAndValue = line.split(histogramSeparator);
					getSymbolHistogram().getMap().put(
						Integer.valueOf(keyAndValue[0]), Long.valueOf(keyAndValue[1]));
					break;
			}
		} 				
	}
	
	/**
	 * @return the symbolHistogram
	 */
	public Histogram getSymbolHistogram() {
		return symbolHistogram;
	}
	/**
	 * @param symbolHistogram the symbolHistogram to set
	 */
	public void setSymbolHistogram(Histogram symbolHistogram) {
		this.symbolHistogram = symbolHistogram;
	}
	/**
	 * @return the repetitionLengthHistogram
	 */
	public Histogram getRepetitionLengthHistogram() {
		return repetitionLengthHistogram;
	}
	/**
	 * @param repetitionLengthHistogram the repetitionLengthHistogram to set
	 */
	public void setRepetitionLengthHistogram(Histogram repetitionLengthHistogram) {
		this.repetitionLengthHistogram = repetitionLengthHistogram;
	}
	/**
	 * @return the compressionRatio
	 */
	public double getCompressionRatio() {
		return compressionRatio;
	}
	/**
	 * @param compressionRatio the compressionRatio to set
	 */
	public void setCompressionRatio(double compressionRatio) {
		this.compressionRatio = compressionRatio;
	}
	/**
	 * @return the seed
	 */
	public long getSeed() {
		return seed;
	}
	/**
	 * @param valueOf
	 */
	public void setSeed(Long seed) {
		this.seed = seed;
	}
	/**
	 * @return the size
	 */
	public int getSize() {
		return size;
	}

	/**
	 * @param size the size to set
	 */
	public void setSize(int size) {
		this.size = size;
	}

	/**
	 * @param length
	 */
	public void incrementDeduplicatedData(int length) {
		this.deduplicatedData+=length;
	}

	/**
	 * @return the deduplicatedData
	 */
	public int getDeduplicatedData() {
		return deduplicatedData;
	}

	/**
	 * @param deduplicatedData the deduplicatedData to set
	 */
	public void setDeduplicatedData(int deduplicatedData) {
		this.deduplicatedData = deduplicatedData;
	}

	@Override
	public MotifChunkCharacterization clone() throws CloneNotSupportedException {
		MotifChunkCharacterization characterization = new MotifChunkCharacterization();
		characterization.seed = seed;
		characterization.setDeduplicatedData(deduplicatedData);
		characterization.setCompressionRatio(compressionRatio);
		characterization.setSize(size);
		characterization.setRepetitionLengthHistogram((Histogram) repetitionLengthHistogram.clone());
		characterization.setSymbolHistogram((Histogram) symbolHistogram.clone());
		return characterization;
	}
}