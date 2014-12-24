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

import java.util.Arrays;
import java.util.Random;

import com.ibm.exception.DataGenerationException;

/**
 * @author Raul Gracia-Tinedo (raulgraciatinedo@gmail.com)
 *
 */
public class DoublesHistogram extends Histogram {
	
	private static final long serialVersionUID = 1L;
	
	public static final double BIN_SIZE = 0.2;
	
	/**
	 * This function adds one to the frequency of the entropy category which
	 * the given entropy belongs to.
	 * 
	 * @param value
	 */
	public void add(Number number){
		Double value = (Double) number;
		int bin = (int) Math.round(value/BIN_SIZE);
		if (!map.containsKey(bin)){
			map.put(bin, 0L);
		}
		long frequency = map.get(bin);
		map.put(bin, frequency+1);
	}
	
	public double getHistogramBasedValue(Random seed){
		double result = -1;
		try {
			result = ((double)generator.generateProportionalKeys(seed))*BIN_SIZE;
		} catch (DataGenerationException exception){
			System.err.println("Doubles Histogram: Problems generating values from histogram " + toString());
		}
		return result;		
	}
	
	public double getHistogramBasedValue(){
		return getHistogramBasedValue(new Random());
	}
	
	@Override
	public String toString() {
		StringBuffer stringBuffer = new StringBuffer();
		Object [] sortedKeys = (Object[]) map.keySet().toArray();
		Arrays.sort(sortedKeys);
		for (Object category: sortedKeys) {
			stringBuffer.append("Value: " + 
				Math.round(((int)category)*BIN_SIZE*100)/100d + " ");
			for (int i = 0; i < map.get(category); i++) {
				stringBuffer.append("*");
			} 
			stringBuffer.append("\t (" + map.get(category) + ")\n");
		}
		return stringBuffer.toString();
	}
}