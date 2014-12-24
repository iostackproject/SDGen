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
package com.ibm.utils;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import com.ibm.generation.SyntheticByteArrayBuilder;

/**
 * @author Raul Gracia-Tinedo (raulgraciatinedo@gmail.com)
 *
 */
public class TestSytheticFileGenerator {

	public void generatePatternedFile(String fileName, long fileSize, double deduplicationRatio) throws IOException {
		long written = 0;
		int patternSize = 64*1024;
		int interPatternSpace = 64*1024;
		
		SyntheticByteArrayBuilder builder = new SyntheticByteArrayBuilder();
		
		byte [] pattern = builder.getRandomByteArray(patternSize);
		BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(fileName));

		Random random = new Random();
		
		while (written < fileSize){
			if (random.nextDouble() < deduplicationRatio){
				outputStream.write(pattern);
				written += patternSize;
			}else{
				byte[] syntheticData = builder.getRandomByteArray(interPatternSpace);
				Utils.shuffle(syntheticData);
				outputStream.write(syntheticData);
				written += interPatternSpace;
			}
		}
		outputStream.close();
	}
}