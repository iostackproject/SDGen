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
import com.ibm.scan.AbstractChunkScanner;

/**
 * Scanner to capture the frequencies of the bytes that appear within a
 * data chunk.
 * 
 * @author Raul Gracia-Tinedo (raulgraciatinedo@gmail.com)
 *
 */
public class AlphabetScanner extends AbstractChunkScanner {

	private static final long serialVersionUID = 1L;
	
	public AlphabetScanner(byte[] data) {
		histogram = new IntegersHistogram();
		this.toScan = data;
	}

	@Override
	public void scan(byte[] data) {     
        for (int b = 0; b < data.length; b++) {
        	histogram.add((int)data[b]);
        }
	}

	@Override
	public Histogram getHistogram() {
		return histogram;
	}

	@Override
	protected void compute() {
		scan(toScan);
	}

	@Override
	public void setInfo(AbstractChunkCharacterization chunk) {
		((MotifChunkCharacterization) chunk).setSymbolHistogram(getHistogram());		
	}
}