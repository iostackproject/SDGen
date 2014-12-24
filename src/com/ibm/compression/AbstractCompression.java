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

/**
 * Class to standardize the access methods of different compression libraries.
 * Any other compression library added to the project should extend this class.
 * 
 * @author Raul Gracia-Tinedo (raulgraciatinedo@gmail.com)
 *
 */
public abstract class AbstractCompression {
	
	//We measure bits of entropy in a byte basis
	public static final int MIN_ENTROPY_BITS = 0;
	public static final int MAX_ENTROPY_BITS = 8; 	

	public static final String compressionPackagePath = "com.ibm.compression.";
	
	public static final int readBufferSize = 32*1024;
	
	// Buffer-level compression methods	
	public abstract byte [] compress(byte [] toCompress);
	public abstract byte [] decompress(byte [] toDecompress);

}