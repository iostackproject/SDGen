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
import java.io.InputStream;

import org.itadaki.bzip2.BZip2InputStream;
import org.itadaki.bzip2.BZip2OutputStream;

/**
 * @author Raul Gracia-Tinedo (raulgraciatinedo@gmail.com)
 *
 */
public class BzipCompression extends AbstractCompression {

	@Override
	public byte[] compress(byte[] toCompress) {
		InputStream fileInputStream = new ByteArrayInputStream(toCompress);
		ByteArrayOutputStream fileOutputStream = new ByteArrayOutputStream();
        BZip2OutputStream outputStream = null;
        byte[] buffer = new byte [readBufferSize];
        int bytesRead;
		try {
			outputStream = new BZip2OutputStream(fileOutputStream);
			while ((bytesRead = fileInputStream.read (buffer)) != -1) {
			        outputStream.write (buffer, 0, bytesRead);
			}
			outputStream.close();
			fileInputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileOutputStream.toByteArray();
	}

	@Override
	public byte[] decompress(byte[] toDecompress) {
		InputStream fileInputStream = new ByteArrayInputStream(toDecompress);
        BZip2InputStream inputStream = new BZip2InputStream(fileInputStream, false);
        ByteArrayOutputStream fileOutputStream = new ByteArrayOutputStream();
        byte[] decoded = new byte [readBufferSize];
        int bytesRead;
        try {
	        while ((bytesRead = inputStream.read (decoded)) != -1) {
	                fileOutputStream.write(decoded, 0, bytesRead) ;        
	        }
	        fileOutputStream.close();
	        inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileOutputStream.toByteArray();
	}
}