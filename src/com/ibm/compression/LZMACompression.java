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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.UnsupportedOptionsException;
import org.tukaani.xz.XZInputStream;
import org.tukaani.xz.XZOutputStream;

/**
 * @author Raul Gracia-Tinedo (raulgraciatinedo@gmail.com)
 *
 */
public class LZMACompression extends AbstractCompression {

	@Override
	public byte[] compress(byte[] toCompress) {
		ByteArrayInputStream inputStream = new ByteArrayInputStream(toCompress);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		LZMA2Options options = new LZMA2Options();
		try {
			options.setPreset(6);	
			XZOutputStream out = new XZOutputStream(outputStream, options);
			//Chunked deflate process
		    byte [] partialOutput = new byte[readBufferSize];
			int size;
			while ((size = inputStream.read(partialOutput)) != -1)
			   out.write(partialOutput, 0, size);
			out.finish();
			out.close();
		} catch (UnsupportedOptionsException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return outputStream.toByteArray();
	}

	@Override
	public byte[] decompress(byte[] toDecompress) {
		ByteArrayInputStream inputStream = new ByteArrayInputStream(toDecompress);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			XZInputStream inXzInputStream = new XZInputStream(inputStream);
			//Chunked deflate process
			byte [] partialOutput = new byte[readBufferSize];
			while (inXzInputStream.read(partialOutput) != -1){
				outputStream.write(partialOutput);
			}
			inXzInputStream.close();
		} catch (UnsupportedOptionsException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return outputStream.toByteArray();
	}
}