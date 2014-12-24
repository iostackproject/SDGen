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

import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4SafeDecompressor;

/**
 * This class provides compression services based on LZ4 algorithm.
 * 
 * @author Raul Gracia-Tinedo (raulgraciatinedo@gmail.com)
 *
 */
public class LZ4Compression extends AbstractCompression{

	private LZ4Factory factory = LZ4Factory.fastestInstance();
	
    public byte [] compress(byte [] data){    	
         LZ4Compressor compressor = factory.fastCompressor(); //highCompressor();//
         return compressor.compress(data);
    }

	@Override
	public byte[] decompress(byte[] toDecompress) {
		LZ4SafeDecompressor decompressor = factory.safeDecompressor();
		return decompressor.decompress(toDecompress, readBufferSize);
	}
}