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
package com.ibm.benchmarks.linkbench;

import java.util.Properties;
import java.util.Random;

import com.ibm.characterization.DatasetCharacterization;
import com.ibm.generation.DataProducer;
import com.ibm.utils.Utils;
/**
 * This class implements an adapter pattern to allow LinkBench
 * calls to interact with the SDGen library.
 * 
 * @author Raul Gracia-Tinedo (raulgraciatinedo@gmail.com)
 *
 */
public class SDGenAdapter {//implements DataGenerator {

    private static DataProducer generator = null;
    
    static {
        DatasetCharacterization characterization = (DatasetCharacterization) Utils.deserializeObject("?");
        generator = new DataProducer(characterization);
        generator.startProducing();
    }
    
    /**
     * To generate data we first need to build a histogram with
     * the data feature values in a histogram.
     * 
     */
    //@Override
    public void init(Properties props, String keyPrefix) {
        //Do nothing at the moment...
    }

    /**
     * Insert the synthetic data in the array passed by parameter
     * and returned.
     */
    //@Override
    public byte[] fill(Random rng, byte[] data) {
        System.arraycopy(generator.getSyntheticData(data.length), 0, data, 0, data.length);
        return data;
    }
}