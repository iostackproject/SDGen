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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.ibm.characterization.Histogram;
import com.ibm.characterization.IntegersHistogram;
import com.ibm.utils.Tuple;

/**
 * @author Raul Gracia-Tinedo (raulgraciatinedo@gmail.com)
 *
 */
public class RepetitionTracker {
	
	//private transient Set<String> uniqueRepetitions = new HashSet<String>();
	
	private Map<Integer, Tuple<Long, Long>> repetitionsAndRenewalRate = new HashMap<>();
	
	private Histogram histogram = new IntegersHistogram();
	
	private Histogram noRepetitionsHistogram = new IntegersHistogram();
	
	private int lastRepetitionPoint = 0;
	
	private long repeatedData = 0;
	
	private long nonRepeatedData = 0;
	
	/*public void addRepetition(byte [] repetitionBytes) {
		addRepetition(new String(repetitionBytes));		
	}*/
	
	/*public void addRepetition(String repetition) {
		addRepetition(repetition, repetition.length());		
	}*/
	
	/**
	 * @param prev_length
	 */
	public void addRepetition(int length) {
		histogram.add(length);		
	}
	
	/*private void addRepetition(String repetition, int length) {
		//If the map does not contain this length, create the tuple
		if (!repetitionsAndRenewalRate.containsKey(length)) {
			repetitionsAndRenewalRate.put(length, new Tuple<Long, Long>(2L, 1L));
		}
		Tuple<Long, Long> lengthRepetitions = repetitionsAndRenewalRate.get(length);
		//If the sequence is new, increment the unique sequences counter
		if (!uniqueRepetitions.contains(repetition)){
			uniqueRepetitions.add(repetition); 
			lengthRepetitions.y+=1;
			lengthRepetitions.x+=2;
		}else {
			//Increment the all repetition length counter
			lengthRepetitions.x+=1;
		}
		addRepetition(length);
		repetitionsAndRenewalRate.put(length,lengthRepetitions);
	}*/

	/**
	 * @return the repetitionsAndRenewalRate
	 */
	public Map<Integer, Tuple<Long, Long>> getRepetitionsAndRenewalRate() {
		return repetitionsAndRenewalRate;
	}

	//public void finish() {
	//	uniqueRepetitions.clear();
	//}
	/**
	 * @return
	 */
	public Histogram getRepetitionDistribution() {
		return histogram;
	}

	/**
	 * @param chunk
	 * @return
	 */
	public long getRenewalRate(int chunk) {
		Tuple<Long, Long> lengthRepetitions = repetitionsAndRenewalRate.get(chunk);
		return lengthRepetitions.x/lengthRepetitions.y+1;
	}
	
	/**
	 * @param strstart
	 */
	public void addRepetition(int strstart, int length) {
		/*System.out.println("Repetition start point: " + strstart);
		System.out.println("Repetition length: " + length);
		System.out.println("No Repetition length: " + (strstart-lastRepetitionPoint));
		System.out.println("-----------------------");*/
		if (strstart >= lastRepetitionPoint){
			repeatedData += length;
			if (strstart-lastRepetitionPoint>=0) {
				noRepetitionsHistogram.add(strstart-lastRepetitionPoint);
				nonRepeatedData += strstart-lastRepetitionPoint;
			}//else System.err.println("Problem with no rep length!");
			lastRepetitionPoint = strstart+length;
			//if (strstart-lastRepetitionPoint < 0) 
				//System.err.print("Bad repetition tracking!!");
		}
		addRepetition(length);
	}

	/**
	 * @return the noRepetitionsHistogram
	 */
	public Histogram getNoRepetitionsHistogram() {
		return noRepetitionsHistogram;
	}

	/**
	 * @param noRepetitionsHistogram the noRepetitionsHistogram to set
	 */
	public void setNoRepetitionsHistogram(Histogram noRepetitionsHistogram) {
		this.noRepetitionsHistogram = noRepetitionsHistogram;
	}
	
	public double getUniquenessRatio() {
		return nonRepeatedData/((double)repeatedData);
	}
}