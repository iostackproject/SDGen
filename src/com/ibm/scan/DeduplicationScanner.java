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

import com.ibm.characterization.AbstractChunkCharacterization;
import com.ibm.characterization.Histogram;
import com.ibm.characterization.IntegersHistogram;
import com.ibm.deduplication.ContentBasedChunking;
import com.ibm.deduplication.Finger;

/**
 * @author Raul Gracia-Tinedo (raulgraciatinedo@gmail.com)
 *
 */
public class DeduplicationScanner extends AbstractChunkScanner {

	private static final long serialVersionUID = 1L;
	
	private ContentBasedChunking chunking = new ContentBasedChunking();

	public DeduplicationScanner() {
		histogram = new IntegersHistogram();
	}
	
	/**
	 * @param data
	 */
	public DeduplicationScanner(byte[] data) {
		this.toScan = data;
	}

	@Override
	protected void compute() {
		scan(toScan);		
	}

	@Override
	public void scan(byte[] data) {
		chunking.digest(data);
	}
	
	@Override
	public Histogram getHistogram() {
		for (Finger finger: chunking.getFingerprints().keySet()){
			histogram.add(finger.hashCode());
		}
		return histogram;
	}

	@Override
	public void setInfo(AbstractChunkCharacterization chunk) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @return the chunking
	 */
	public ContentBasedChunking getChunking() {
		return chunking;
	}

	/**
	 * @param chunking the chunking to set
	 */
	public void setChunking(ContentBasedChunking chunking) {
		this.chunking = chunking;
	}

	/**
	 * @return
	 */
	public double getDeduplicationRatio() {
		return chunking.calculateDeduplicationRatio();
	}
	
	public void reset() {
		chunking.reset();
	}
}