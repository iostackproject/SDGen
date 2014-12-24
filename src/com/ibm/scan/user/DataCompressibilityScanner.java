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
package com.ibm.scan.user;

import com.ibm.characterization.AbstractChunkCharacterization;
import com.ibm.characterization.Histogram;
import com.ibm.characterization.IntegersHistogram;
import com.ibm.characterization.user.MotifChunkCharacterization;
import com.ibm.compression.JZlibCompression;
import com.ibm.scan.AbstractChunkScanner;
import com.ibm.scan.RepetitionTracker;

/**
 * This scanner provides the distribution of repetitions and
 * compression of data chunks. It can rely on any algorithm that
 * provides both sources of information. At the moment we rely on
 * a Java version of Zlib library.
 * 
 * @author Raul Gracia-Tinedo (raulgraciatinedo@gmail.com)
 *
 */
public class DataCompressibilityScanner extends AbstractChunkScanner{

	private static final long serialVersionUID = 1L;
	
	private Histogram noRepetitions = new IntegersHistogram();
	private double compressionRatio = 0.0;
	
	public DataCompressibilityScanner() {
		this.histogram = new IntegersHistogram();
	}
	
	public DataCompressibilityScanner(byte[] toScan) {
		this.histogram = new IntegersHistogram();
		this.toScan = toScan;
	}
	
	@Override
	protected void compute() {
		scan(toScan);
	}

	@Override
	public void scan(byte[] data) {
		JZlibCompression repetitionFinder = new JZlibCompression();
		byte[] compressedData = repetitionFinder.compress(data);
		compressionRatio = data.length/((double)compressedData.length);
		RepetitionTracker repetitionTracker = repetitionFinder.getRepetitions();
		histogram = repetitionTracker.getRepetitionDistribution();
		noRepetitions = repetitionTracker.getNoRepetitionsHistogram();
	}
	
	@Override
	public void setInfo(AbstractChunkCharacterization chunk) {
		((MotifChunkCharacterization) chunk).setRepetitionLengthHistogram(getHistogram());
		((MotifChunkCharacterization) chunk).setCompressionRatio(getCompressionRatio());
		this.toScan = null;
	}

	@Override
	public Histogram getHistogram() {
		return histogram;
	}
	/**
	 * @return the noRepetitions
	 */
	public Histogram getNoRepetitions() {
		return noRepetitions;
	}

	public double getCompressionRatio() {
		return compressionRatio;
	}
}