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
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.ibm.compression.jcraft.jzlib.JZlib;
import com.ibm.compression.jcraft.jzlib.ZInputStream;
import com.ibm.compression.jcraft.jzlib.ZOutputStream;
import com.ibm.compression.jcraft.jzlib.ZStream;
import com.ibm.scan.RepetitionTracker;

/**
 * @author Raul Gracia-Tinedo (raulgraciatinedo@gmail.com)
 *
 */
public class JZlibCompression extends AbstractCompression{

	private RepetitionTracker repetitions = new RepetitionTracker();
	
	@Override
	public byte[] compress(byte[] toCompress) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			ZOutputStream zOut = new ZOutputStream(out, JZlib.Z_BEST_COMPRESSION);
			ObjectOutputStream objOut = new ObjectOutputStream(zOut);
			objOut.writeObject(toCompress);
			zOut.close();
			//Get the repetitions found from this data block
			repetitions = zOut.getState().getRepetitions();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return out.toByteArray();
	}

	@Override
	public byte[] decompress(byte[] toDecompress) {
		ByteArrayInputStream in = new ByteArrayInputStream(toDecompress);
		ZInputStream zIn;
		try {
			zIn = new ZInputStream(in);
			ObjectInputStream objIn = new ObjectInputStream(zIn);
			return (byte[]) objIn.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	
	/**
	 * @return the repetitionDistribution
	 */
	public RepetitionTracker getRepetitions() {
		return repetitions;
	}
	
}