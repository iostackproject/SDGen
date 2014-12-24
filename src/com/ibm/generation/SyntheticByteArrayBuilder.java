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

import java.util.Random;

/**
 * Utilities to generate synthetic data.
 * 
 * @author Raul Gracia-Tinedo (raulgraciatinedo@gmail.com)
 *
 */
public class SyntheticByteArrayBuilder {

	/**
	 * Return a random array of bytes of 
	 * @param length
	 * @return
	 */
	public byte[] getRandomByteArray(int length){
		return getRandomByteArray(new Random(), length, -1);
	}
	public byte[] getRandomByteArray(int length, int range){
		return getRandomByteArray(new Random(), length, range);
	}
	/**
	 * Return a random array of bytes of 
	 * @param length
	 * @return
	 */
	public byte[] getRandomByteArray(Random random, int length, int range){
		byte[] randomData = new byte[length];
		int i = 0;
		while (i < length){
			randomData[i] = (range!=-1) ? (byte) random.nextInt(): (byte) random.nextInt(range);
			i++;
		}
		return randomData;
	}
	public byte[] getRandomByteArray(Random random, int length){
		return getRandomByteArray(random, length, -1);
	}
	
	/**
	 * 
	 * @param length
	 * @return
	 */
	public byte[] getZeroData(int length) {
		//By default arrays of bytes are initialized to 0
		return new byte[length];
	}
}