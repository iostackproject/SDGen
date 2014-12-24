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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.ibm.compression.AbstractCompression;
import com.ibm.compression.AbstractCompressionFactory;
import com.ibm.config.PropertiesStore;
import com.ibm.config.PropertyNames;
import com.ibm.deduplication.Finger;

/**
 * Set of useful methods.
 * 
 * @author Raul Gracia-Tinedo (raulgraciatinedo@gmail.com)
 *
 */
public class Utils {
	
	public static AbstractCompressionFactory compressorFactory = null;

	public static final String APPLICATION_PATH = 
			System.getProperty("user.dir") + File.separator;
	
	static {
		//Initialize the factory with the compression algorithm configured
		try {
			compressorFactory = (AbstractCompressionFactory) Class.forName(
					AbstractCompression.compressionPackagePath + 
						PropertiesStore.getAppProperties().get(
							PropertyNames.SCAN_COMP_ALG) + 
								"CompressionFactory").newInstance();
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException e) {
			e.printStackTrace();
		}		
	}
	
	/**
	 * Return a new array from joining two arrays given by parameter.
	 * @param firstArray
	 * @param secondArray
	 * @return
	 */
	public static byte[] joinByteArrays(byte[] firstArray, byte[] secondArray){
		byte[] jointArray = new byte[firstArray.length+secondArray.length];
		System.arraycopy(firstArray, 0, jointArray, 0, firstArray.length);
		System.arraycopy(secondArray, 0, jointArray, firstArray.length, secondArray.length);
		return jointArray;
	}
	
	/**
	 * Randomly shuffle elements within an array.
	 * @param array
	 */
	public static void shuffle(byte [] array) {
        shuffle(new Random(), array);
    }
	
	/**
	 * Randomly shuffle elements within an array.
	 * @param array
	 */
	public static void shuffle(Random random, byte [] array) {
        int count = array.length;
        for (int i = count; i > 1; i--) {
            swap(array, i - 1, random.nextInt(i));
        }
    }
	
	/**
	 * Get the size of a file.
	 * @param fileName
	 * @return
	 */
	public static long getFileSize(String fileName) {
		File file = new File(fileName);
		return file.length();
	}
	
	public static String getApplicationPath() {
		return System.getProperty("user.dir") + File.separator;
	}
	/**
	 * This function returns the compression ration of two files: the first one
	 * the original (F_or) and the second one the compressed file (F_com). The 
	 * ratio is defined by F_or/F_com.
	 * 
	 * @param originalFile
	 * @param compressedFile
	 * @return
	 */
	public static double getCompressionRatio(String originalFile, String compressedFile){
		float lengthOriginal = (float) Utils.getFileSize(originalFile);
		float lengthCompressed = (float) Utils.getFileSize(compressedFile);
		if (lengthCompressed > 0.0)
			return lengthOriginal/lengthCompressed;
		return Double.MAX_VALUE;
	}
	
	public static void writeDataToFile(byte[] data, String fileName) {
		// Write random data to a file
		FileOutputStream fileOutputStream;
		try {
			fileOutputStream = new FileOutputStream(new File(fileName));
			int from = 0, to = 0;
			int writeSize = 32*1024;
			while (from < data.length){
				if ((to + writeSize) > data.length){
					to = data.length;
				}else to += writeSize;
				fileOutputStream.write(Arrays.copyOfRange(data, from, to));
				from += writeSize;
			}
			fileOutputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static double getCompressionRatio(byte[] original, byte[] compressed) {
		return ((double)original.length)/compressed.length;
	}
	public static double compressAndCompare(byte[] data){
		//Compress File
		AbstractCompression compressor = compressorFactory.create();
		return compressAndCompare(compressor, data);
	}
	
	public static void deleteFile(String fileName) {
		File file = new File(fileName);
		file.delete();
	}
	
	public static File[] getDirectoryFiles(String path) {
		File folder = new File(path);
		return folder.listFiles(); 
	}
	
    /**
     * Shovels all data from an input stream to an output stream.
     */
    public static void shovelInToOut(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1000];
        int len;
        while((len = in.read(buffer)) > 0) {
            out.write(buffer, 0, len);
        }
    }
	
	/**
     * Returns bits of Shanon entropy in a given array of bytes
     * 
     * @param data
     * @return bits of entropy
     */
	public static double getShannonEntropy(byte[] data) {
        Map<Byte, Integer> occ = new HashMap<Byte, Integer>();
     
        for (int b = 0; b < data.length; ++b) {
        	byte currentByte = data[b];
        	if (occ.containsKey(currentByte)) {
        		occ.put(currentByte, occ.get(currentByte) + 1);
        	} else {
        		occ.put(currentByte, 1);
        	}
        }
     
        double entropy = 0.0;
        for (Map.Entry<Byte, Integer> entry : occ.entrySet()) {
        	double p = (double) entry.getValue() / data.length;
        	entropy += p * log2(p);
        }
        return (entropy==0.0) ? 0.0 : -entropy;
    }
	
	/**
	 * Save the serialized version of the object in a file.
	 * 
	 * @param outputFileName
	 */
	public static void serializeObject(Object object, String outputFileName){
		try {
			FileOutputStream fileOut = new FileOutputStream(outputFileName);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(object);
			out.close();
			fileOut.close();
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}
	/**
	 * Load the characterization from the serialized version stored in a file.
	 * 
	 * @param inputFileName
	 * @return
	 */
	public static Object deserializeObject(String inputFileName) {
		Object toLoad = null;
		try {
			FileInputStream fileIn = new FileInputStream(inputFileName);
	        ObjectInputStream in = new ObjectInputStream(fileIn);
	        toLoad = in.readObject();
	        in.close();
	        fileIn.close();
		}catch(IOException ioException) {
	        ioException.printStackTrace();
	    }catch(ClassNotFoundException classNotFoundException) {
	        classNotFoundException.printStackTrace();
	    }
		return toLoad;
	}

	public static double getDeduplicationRatio(Map<Finger, Long> chunksAndRepetitions) {
		long deduplicatedData = 0, allData = 0, uniqueChunks = 0, repeatedChunks = 0, allChunks = 0;
		for (Finger fingerprint: chunksAndRepetitions.keySet()){
			long count = chunksAndRepetitions.get(fingerprint);
			if (count > 1){
				deduplicatedData+=(count-1)*fingerprint.getLength();
				repeatedChunks+=count-1;
			}
			uniqueChunks+=1;
			allData+=count*fingerprint.getLength();
			allChunks+=count;
		}
		double dedupRatio = ((double) deduplicatedData)/allData;
		System.out.println("Deduplication ratio: " + dedupRatio);
		System.out.println("Unique chunks: " + uniqueChunks);
		System.out.println("Repeated chunks: " + repeatedChunks);
		System.out.println("Deduplicated data: " + deduplicatedData);
		System.out.println("Total chunks: " + allChunks);
		System.out.println("All data: " + allData);
		return dedupRatio;
	}
	
	/**
	 * @param profilespath
	 */
	public static void createDirectory(String directoryName) {
		File theDir = new File(directoryName);
		// if the directory does not exist, create it
		if (!theDir.exists()) {
			System.out.println("creating directory: " + directoryName);
			theDir.mkdirs();  
		}		
	}
	
    //	PRIVATE FUNCTIONS --
	
	private static double log2(double a) {
		return Math.log(a) / Math.log(2);
	}
	
    private static void swap(byte[] array, int i, int j) {
        byte temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }
    private static double compressAndCompare(AbstractCompression compressor, byte[] data) {
    	byte [] compressedData = compressor.compress(data);
		return getCompressionRatio(data, compressedData);
    }

    //ACCESS METHODS --
    
	/**
	 * @return the characterizationCompressorFactory
	 */
	public static AbstractCompressionFactory getCharacterizationCompressorFactory() {
		return compressorFactory;
	}
	/**
	 * @param characterizationCompressorFactory the characterizationCompressorFactory to set
	 */
	public static void setCharacterizationCompressorFactory(
			AbstractCompressionFactory characterizationCompressorFactory) {
		Utils.compressorFactory = characterizationCompressorFactory;
	}
}