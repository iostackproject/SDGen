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
import com.ibm.generation.FitnessProportionateSelection;

/**
 * @author Raul Gracia-Tinedo (raulgraciatinedo@gmail.com)
 *
 */
public class IntegersHistogram extends Histogram{
	
	private static final long serialVersionUID = 1L;

	public void add(Number number) {
		Integer value = (Integer) number;
		if (!map.containsKey(value)){
			map.put(value, 0L);
		}
		long frequency = map.get(value);
		map.put(value, frequency+1);
		accumulatedValues+=value;
		accumulatedFrequencies+=1;
	}	
	
	public double getHistogramBasedValue(Random seed){
		double result = -1;
		try {
			if (generator==null) 
				generator = new FitnessProportionateSelection<Integer>(map);
			result = ((double)generator.generateProportionalKeys(seed));
		} catch (DataGenerationException exception){
			System.err.println("Integers Hsstoram: Problems generating values from histogram: " + toString());
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
			stringBuffer.append("Value: " + category + " ");
			for (int i = 0; i < map.get(category); i++) {
				stringBuffer.append("*");
			} 
			stringBuffer.append("\t (" + map.get(category) + ")\n");
		}
		return stringBuffer.toString();
	}
}