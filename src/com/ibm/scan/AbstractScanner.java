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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

import com.ibm.config.PropertiesStore;
import com.ibm.config.PropertyNames;
import com.ibm.generation.SampleEstimator;
import com.ibm.utils.Utils;

/**
 * @author Raul Gracia-Tinedo (raulgraciatinedo@gmail.com)
 *
 */
public abstract class AbstractScanner {
	
	/*Sampling configuration*/
	protected boolean doSampling = PropertiesStore.getBoolean(PropertyNames.SAMPLING);
	protected SampleEstimator sampleEstimator = new SampleEstimator();
	
	public abstract void scan(byte[] toScan);
	
	public abstract void finishScan();
	
	/**
	 * Scans the file or directory introduced in the first parameter at chunks of
	 * size defined by the second parameter.
	 * 
	 * @param toScan
	 * @param regionSize
	 */
	public void scan(String toScan, int regionSize){
		System.out.println("Scanning: " + toScan);
		boolean isDir = new File(toScan).isDirectory();	
		if (isDir) scanDirectory(toScan, regionSize);
		else {
			if (doSampling) samplingScan(toScan, regionSize);		
			else fullScan(toScan, regionSize);
		}
	}
	
	/**
	 * Scans the whole content of a file.
	 * 
	 * @param fileName
	 * @param regionSize
	 */
	public void fullScan(String fileName, int regionSize) {	    
		try {
			FileInputStream is = new FileInputStream(new File(fileName));
		    byte[] chunk = new byte[regionSize];
		    int bytesRead = 0;
		    while (true) {
		    	bytesRead = is.read(chunk);
		    	if (bytesRead == -1) break;
		    	if (bytesRead < chunk.length) 
		    		chunk = Arrays.copyOfRange(chunk, 0, bytesRead);
		    	if (bytesRead > 128) 
		    		scan(chunk);
		    }
		    is.close();
		} catch (FileNotFoundException fileNotFoundException) {
		    fileNotFoundException.printStackTrace();
		} catch (IOException ioException) {
		    ioException.printStackTrace();
		}
	}	
	/**
	 * Scans only a number of equally-spaced samples of the file passed in the
	 * first parameter. The samples are of the size defined in the second parameter.
	 * The sampling parameters (e.g. accuracy) are defined in the Properties file.
	 * 
	 * @param fileName
	 * @param regionSize
	 */
	public void samplingScan (String fileName, int regionSize) {
		long fileSize = Utils.getFileSize(fileName);
		long samples = sampleEstimator.getNumberOfSamples(); //(long) ((fileSize/regionSize) * 1.0);
		long interSampleSpace = (long) (fileSize-(samples*regionSize))/samples;
		if ((samples*regionSize)>fileSize) {
			System.out.println("Data to sample is larger than the file itself." +
					" Doing a full scan instead.");
			fullScan(fileName, regionSize);
		}else{
			System.out.println("Number of samples: " + samples);
			try {
				long actualSamples = -1;
				RandomAccessFile randomAccessFile = new RandomAccessFile(fileName, "r");
				byte[] chunk = new byte[regionSize];
				while (randomAccessFile.read(chunk) != -1) {
			    	scan(chunk);
			    	//Skip bytes between samples
			    	int skipped = randomAccessFile.skipBytes((int) interSampleSpace);
			    	if (skipped<interSampleSpace) 
			    		System.out.println("skipping less bytes than expected in sampling " + skipped);
			    	actualSamples++;
			    }
				randomAccessFile.close();
				if (actualSamples != samples) {
					System.err.println("Number of scanned samples " + actualSamples 
						+ " is different" + "from the expected (" + samples + ") one!");
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * Walks a entire directory scanning all the files and subdirectories.
	 * 
	 * @param directory
	 * @param regionSize
	 */
	public void scanDirectory (String directory, int regionSize) {
		for (File file: Utils.getDirectoryFiles(directory)){
			scan(file.getAbsolutePath(), regionSize);
		}	
	}
}