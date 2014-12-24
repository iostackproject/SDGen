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
import com.ibm.characterization.DoublesHistogram;
import com.ibm.characterization.Histogram;
import com.ibm.scan.AbstractChunkScanner;
import com.ibm.utils.Utils;

/**
 * This class generates entropy mappings of files.
 * 
 * @author Raul Gracia-Tinedo (raulgraciatinedo@gmail.com)
 *
 */
public class EntropyScanner extends AbstractChunkScanner {
	
	private static final long serialVersionUID = 1L;

	public EntropyScanner() {
		histogram = new DoublesHistogram();
	}
	
	public EntropyScanner(byte[] data) {
		histogram = new DoublesHistogram();
		this.toScan = data;
	}

	/**
	 * This method scans the entropy of every chunk of regoinSize bytes for the 
	 * given data.
	 *  
	 * @param data
	 * @param regionSize
	 * @return
	 */
	public void scan(byte [] data){
		double entropy = 0;
		entropy = Utils.getShannonEntropy(data);
		histogram.add(entropy);
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
		// TODO Auto-generated method stub		
	}
}