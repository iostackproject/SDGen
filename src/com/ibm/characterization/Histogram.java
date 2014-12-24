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
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.ibm.generation.FitnessProportionateSelection;

/**
 * This class represents the histogram of characterization values
 * across a file. This class is not thread-safe since values 
 * should be generated when the histogram is completely built.
 * 
 * @author Raul Gracia-Tinedo (raulgraciatinedo@gmail.com)
 *
 */
public abstract class Histogram implements Serializable, Cloneable{

	private static final long serialVersionUID = 1L;
	
	protected Map<Integer, Long> map = new HashMap<Integer, Long>();
	
	protected transient FitnessProportionateSelection<Integer> generator = 
			new FitnessProportionateSelection<Integer>(map); 
	
	protected long accumulatedValues = 0;
	protected long accumulatedFrequencies = 0;
	
	public abstract void add(Number value);
	
	public abstract double getHistogramBasedValue();
	
	public abstract double getHistogramBasedValue(Random seed);
	
	/**
	 * Add the frequencies of the histogram passed by parameter to the
	 * current one. This method is useful to build in parallel several
	 * entropy histograms of a file and then merging them together.  
	 * 
	 * @param generateEntropyHistogram
	 */
	public void mergeHistograms(Histogram toMerge) {
		for (Integer category: toMerge.getMap().keySet()){
			long currentValue = 0L;
			if (map.containsKey(category)) 
				currentValue = map.get(category);
			map.put(category,  currentValue + toMerge.getMap().get(category));
		}
	}
	/**
	 * This call is necessary to compute the proportionality vector
	 * in the Fitness Proportional generation. We just acknowledge that
	 * we have finished scanning the file.
	 */
	public void finishScan(){
		generator.computeFrequencyBasedProbability(map);
	}
	
	//	ACCESS METHODS --
	
	/**
	 * @return the histogram
	 */
	public Map<Integer, Long> getMap() {
		return map;
	}

	/**
	 * @param histogram the histogram to set
	 */
	public void setMap(Map<Integer, Long> map) {
		this.map = map;
	}
	/**
	 * @return
	 */
	public boolean isEmpty() {
		return map.isEmpty();
	}

	/**
	 * @return the accumulatedValues
	 */
	public long getAccumulatedValues() {
		return accumulatedValues;
	}

	/**
	 * @param accumulatedValues the accumulatedValues to set
	 */
	public void setAccumulatedValues(long accumulatedValues) {
		this.accumulatedValues = accumulatedValues;
	}

	/**
	 * @return the accumulatedFrequencies
	 */
	public long getAccumulatedFrequencies() {
		return accumulatedFrequencies;
	}

	/**
	 * @param accumulatedFrequencies the accumulatedFrequencies to set
	 */
	public void setAccumulatedFrequencies(long accumulatedFrequencies) {
		this.accumulatedFrequencies = accumulatedFrequencies;
	}
	
	public double getHistogramMeanValue(){
		return ((double) accumulatedValues)/accumulatedFrequencies;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		Histogram histogram = null;
		try {
			histogram = this.getClass().newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		histogram.setAccumulatedFrequencies(accumulatedFrequencies);
		histogram.setAccumulatedValues(accumulatedValues);
		for (int key: map.keySet())
			histogram.getMap().put(key, map.get(key));
		return histogram;
	}
}