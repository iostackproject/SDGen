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

import java.io.Serializable;
import java.io.Writer;
import java.security.SecureRandom;
import java.util.List;

/**
 * Any user-defined user characterization should inherit from this class
 * in order to be part of the framework life-cycle. This class includes
 * the basic attributes to hold information about a chunk.  
 * 
 * @author Raul Gracia-Tinedo (raulgraciatinedo@gmail.com)
 *
 */
public abstract class AbstractChunkCharacterization implements Serializable, Cloneable{
	
	private static final long serialVersionUID = 1L;
	/*Size of the chunk during the scan process*/
	protected int size = 0; 
	/*Amount of non unique data of this chunk across a dataset*/
	protected int deduplicatedData = 0;
	/*Storing the seed is necessary to reproduce the "same" chunk. This
	 * is needed to produce deduplicated data*/
	protected long seed = 0;
	
	public AbstractChunkCharacterization() {
		this.seed = new SecureRandom().nextLong();
	}
	
	/**
	 * Write the information of a chunk in the writer passed by parameter.
	 * If the user provides a specific format to persist the state of a chunk,
	 * he should also provide the load mechanism. We encourage to provide
	 * tailored save/load methods in case of including complex data structures
	 * inside a chunk, since the default serialization may occupy more space.
	 * 
	 * @param outputFile
	 */
	public abstract void save (Writer outputFile);
	/**
	 * Load the state of a chunk object from a group of strings that represent
	 * lines of a characterization file. These string are only related with the
	 * chunk, so the user should only take care of parsing the information
	 * related with a chunk. The DatasetCharacterization class does the rest.  
	 */
	public abstract void load (List<String> chunkInfo);
	
	@Override
	public abstract AbstractChunkCharacterization clone() throws CloneNotSupportedException;

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
}