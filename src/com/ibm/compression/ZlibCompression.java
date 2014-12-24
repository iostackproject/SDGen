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
package com.ibm.compression;

import java.io.ByteArrayOutputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * This class provides compression services based on the Zlib library.
 * 
 * @author Raul Gracia-Tinedo (raulgraciatinedo@gmail.com)
 *
 */
public class ZlibCompression extends AbstractCompression{
	
	public static final int COMPRESSION_LEVEL = Deflater.DEFAULT_COMPRESSION;
	
	@Override
	public byte[] compress(byte[] toCompress) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		Deflater compressor = new Deflater();
		//Set the configured compression level
		compressor.setLevel(COMPRESSION_LEVEL);
	    compressor.setInput(toCompress);
	    compressor.finish();
	    //Chunked deflate process
	    byte [] partialOutput = new byte[readBufferSize];
	    while (!compressor.finished()){
	    	int count = compressor.deflate(partialOutput);
	    	output.write(partialOutput, 0, count);
	    }
	    //Close the compressor
	    compressor.end();
		return output.toByteArray();
	}

	@Override
	public byte[] decompress(byte[] toDecompress) {
		Inflater inflater = new Inflater(); 
		inflater.setInput(toDecompress); 
		ByteArrayOutputStream output = new ByteArrayOutputStream(); 
		byte[] buffer = new byte[readBufferSize];
		while (!inflater.finished()) { 
			int count = -1;
			//Just die if the input format is invalid
			try {
				count = inflater.inflate(buffer);
			} catch (DataFormatException e) {
				e.printStackTrace();
			} 	
			output.write(buffer, 0, count);
			
		}
		return output.toByteArray();
	}
}