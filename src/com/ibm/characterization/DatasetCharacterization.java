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
package com.ibm.characterization;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.ibm.characterization.user.MotifChunkCharacterization;

/**
 * This class contains the characterization information of a single dataset.
 * Making use of objects of this type we can generate synthetic data
 * similar in terms of compression and deduplication to the original dataset.
 * 
 * @author Raul Gracia-Tinedo (raulgraciatinedo@gmail.com)
 *
 */
public class DatasetCharacterization implements Serializable, Cloneable{

	private static final long serialVersionUID = 1L;

	/*List of chunk characterization and deduplication ratio of this dataset*/
	private List<AbstractChunkCharacterization> chunkCharacterization = new ArrayList<>();
	private double deduplicationRatio = 0.0;
	
	/*Used to generate synthetic chunks in the same order that they appear in the file*/
	private transient volatile int circularIndex = 0;
	
	/*Tokens used to format the persistent form of this object*/
	private static final String chunkSeparator = "?";
	private static final String lineBreak = "\n";
	
	/**
	 * Persistently write the information of a {@link DatasetCharacterization}
	 * object at disk.
	 * 
	 * @param filePath
	 */
	public void save(String filePath) {
		BufferedWriter fileOut;
		try {
			fileOut = new BufferedWriter(new FileWriter(filePath));
			//Write the deduplication ratio
			fileOut.write(deduplicationRatio + lineBreak);
			for (AbstractChunkCharacterization c: chunkCharacterization){
				c.save(fileOut);
				fileOut.write(chunkSeparator + lineBreak);
			}
			fileOut.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Load on this object the characterization stored in the file
	 * passed by parameter.
	 * 
	 * @param filePath
	 */
	public void load(String filePath) {
		BufferedReader fileIn = null;
		try {
			fileIn = new BufferedReader(new FileReader(filePath));
			String line = "";
			//Get the file deduplication ratio
			deduplicationRatio = Double.valueOf(fileIn.readLine());
			//Load every chunk characterization object
			MotifChunkCharacterization chunk = new MotifChunkCharacterization();
			List<String> chunkInfo = new ArrayList<String>(); 
			while ((line = fileIn.readLine())!=null){
				if (line.startsWith(chunkSeparator)){
					chunk.load(chunkInfo);
					chunkCharacterization.add(chunk);
					chunk = new MotifChunkCharacterization();
					chunkInfo.clear();
				}else chunkInfo.add(line);				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
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
	 * @return the deduplicationRatio
	 */
	public double getDeduplicationRatio() {
		return deduplicationRatio;
	}
	/**
	 * @param deduplicationRatio the deduplicationRatio to set
	 */
	public void setDeduplicationRatio(double deduplicationRatio) {
		this.deduplicationRatio = deduplicationRatio;
	}
	
	public synchronized AbstractChunkCharacterization getNextChunkInCircularOrder() {
		return chunkCharacterization.get(circularIndex++%chunkCharacterization.size());
	}

	@Override
	protected DatasetCharacterization clone() throws CloneNotSupportedException {
		DatasetCharacterization clone = new DatasetCharacterization();
		List<AbstractChunkCharacterization> cloneChunkCharacterization = new ArrayList<>();
		clone.setDeduplicationRatio(deduplicationRatio);
		for (AbstractChunkCharacterization chunk: chunkCharacterization)
			cloneChunkCharacterization.add(chunk.clone());
		clone.setChunkCharacterization(cloneChunkCharacterization);
		return clone;
	}
}