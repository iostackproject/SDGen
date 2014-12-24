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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.ibm.exception.DataGenerationException;
import com.ibm.utils.Tuple;

/**
 * This class is implements the Fitness Proportional Selection algorithm. In 
 * summary, given a map of elements and their frequencies, this class computes
 * a vector of ranges according to the relative weight of each element's 
 * frequency compared with the total frequencies. Furthermore, on this vector 
 * is computed, this class also retrieves elements of the map based on the 
 * distribution of their weights.
 * 
 * @author Raul Gracia-Tinedo (raulgraciatinedo@gmail.com)
 *
 */
public class FitnessProportionateSelection <K> implements Serializable{

	private static final long serialVersionUID = 1L;

	private Map<K, Long> histogram = null;
	
	private BiMap<K, Tuple<Double, Double>> proportionalityMap;
	private List<Tuple<Double, Double>> sortedProbabilityBins;
	
	private boolean canGenerate;
	
	public FitnessProportionateSelection(Map<K, Long> histogram) {
		this.histogram = histogram;
	}
	/**
	 * This function returns random values whose distribution is based 
	 * on the histogram of entropies that characterizes a file. Therefore, 
	 * values are generated taking into account the frequency of histogram
	 * values of file chunks (Fitness Proportional selection).
	 *  
	 * @return element selected in a fitness proportional way
	 */
	public K generateProportionalKeys(Random seed) throws DataGenerationException {
		if (!canGenerate){
			computeFrequencyBasedProbability();
		}
		//Generate random trial
		double random = seed.nextDouble();
		//Binary search for the correct bin
		Tuple<Double, Double> proportion = binBinarySearch(sortedProbabilityBins, random);
		//Get the key associated to that bin
		K key = proportionalityMap.inverse().get(proportion);
		if (key != null) return key;
		throw new DataGenerationException();
	}
	public K generateProportionalKeys() throws DataGenerationException {
		return generateProportionalKeys(new Random());
	}
	/**
	 * This method builds an internal map that computes each entropy category
	 * with ranges between [0,1]. The width of a range represents the probability 
	 * of retrieving a chunk of a certain entropy category. Ranges are computed
	 * taking into account the frequency of a category compared with the total 
	 * frequencies. 
	 */
	public synchronized void computeFrequencyBasedProbability(Map<K, Long> histogram){
    	//Calculate the global number of frequencies
		proportionalityMap = HashBiMap.create();
		sortedProbabilityBins = new ArrayList<Tuple<Double, Double>>();
		Long totalFrequencies = 0L;
		for (Long frequency: histogram.values()) 
			totalFrequencies += frequency;
		double proportion, proportionAccum = 0;
		Long frequency = 0L;
		//Calculate the proportion that represents the frequencies
		//of each element compared with the total
		for (K category: histogram.keySet()) {
			frequency = histogram.get(category); 
			if (frequency > 0){
				proportion = ((double)frequency)/totalFrequencies;
				Tuple<Double, Double> probabilityBin = new Tuple<Double, Double>(
						proportionAccum, proportionAccum+proportion);
				proportionalityMap.put(category, probabilityBin);
				sortedProbabilityBins.add(probabilityBin);
				proportionAccum+=proportion;
			}
		}
		//The sum of all probabilities must be 1
		if (proportionAccum>1.0001 || proportionAccum<0.999) {
			System.err.println("Sum of probabilities not equal to 1: " + proportionAccum);
			//System.exit(1);
		}
		canGenerate = true;
	}
	
	public void computeFrequencyBasedProbability(){
		if (histogram!=null){
			computeFrequencyBasedProbability(histogram);
		}
	}

	/**
	 * Performs a binary search to find the Tuple that the element key
	 * belongs to.
	 *  
	 * @param elements
	 * @param key
	 * @return
	 * @throws DataGenerationException
	 */
	private Tuple<Double, Double> binBinarySearch(List<Tuple<Double, Double>> elements, double key) 
										throws DataGenerationException {
		if (elements==null || elements.size()==0) throw new DataGenerationException();
		int midPoint = elements.size()/2, minPoint = 0, maxPoint = elements.size();
		while (minPoint < maxPoint) {
			Tuple<Double, Double> elem = elements.get(midPoint);	
			if (key > elem.x && key <= elem.y) return elem;
			if (key < elem.x) maxPoint = midPoint;
			if (key > elem.y) minPoint = midPoint;
			midPoint = minPoint + (maxPoint-minPoint)/2;
		}
		throw new DataGenerationException();
	}
	
	//	ACCESS METHODS --
	
	/**
	 * @return the histogram
	 */
	public Map<K, Long> getHistogram() {
		return histogram;
	}

	/**
	 * @param histogram the histogram to set
	 */
	public void setHistogram(Map<K, Long> histogram) {
		this.histogram = histogram;
	}
}