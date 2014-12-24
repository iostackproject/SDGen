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

import com.ibm.config.PropertiesStore;
import com.ibm.config.PropertyNames;

/**
 * This class defines the number of data samples to be scanned
 * from a dataset. The model comes from the paper:
 * "To Zip or not to Zip: Effective Resource Usage for Real-Time 
 * Compression" (USENIX FAST'13) 
 * 
 * @author Raul Gracia-Tinedo (raulgraciatinedo@gmail.com)
 *
 */
public class SampleEstimator {
	
	private double accuracy = Double.valueOf(
			PropertiesStore.getAppProperties().getProperty(PropertyNames.ACCURACY));
	
	private double confidence = Double.valueOf(
			PropertiesStore.getAppProperties().getProperty(PropertyNames.CONFIDENCE));
	
	public long getNumberOfSamples() {
		return Math.round((1.0/(2*Math.pow(accuracy,2))) * Math.log(2/confidence));
	}
}