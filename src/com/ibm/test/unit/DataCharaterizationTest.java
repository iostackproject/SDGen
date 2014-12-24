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
package com.ibm.test.unit;

import java.util.Random;

import org.junit.Test;

import com.ibm.characterization.DoublesHistogram;
import com.ibm.characterization.Histogram;
import com.ibm.scan.user.EntropyScanner;

/**
 * @author Raul Gracia-Tinedo (raulgraciatinedo@gmail.com)
 *
 */
public class DataCharaterizationTest {
	
	@Test
	public void entropyHistogramSerializationTest () {
		byte [] testData = new byte [1024];
		Random random = new Random();
		for (int i = 0; i < testData.length; i++){
			testData[i] = (byte) random.nextInt();
		}
		EntropyScanner entropyScanner = new EntropyScanner();
		entropyScanner.scan(testData);
		Histogram dataCharaterization = entropyScanner.getHistogram();
		String outputFile = "serializedHistogram.ser";
		//Histogram.exportCharacterization(outputFile);
		Histogram Histogram2 = new DoublesHistogram();
		//Histogram2.importCharacterization(outputFile);
		assert dataCharaterization.equals(Histogram2);
	}

}
