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
package com.ibm.deduplication;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.rabinfingerprint.fingerprint.RabinFingerprintLong;
import org.rabinfingerprint.fingerprint.RabinFingerprintLongWindowed;
import org.rabinfingerprint.handprint.BoundaryDetectors;
import org.rabinfingerprint.handprint.FingerFactory.ChunkBoundaryDetector;
import org.rabinfingerprint.polynomial.Polynomial;

import com.ibm.config.PropertiesStore;
import com.ibm.config.PropertyNames;

/**
 * Content-based chunking mechanisms based on Rabin fingerprint to detect the
 * deduplication ratio of data. 
 * 
 * @author Raul Gracia-Tinedo (raulgraciatinedo@gmail.com)
 *
 */
public class ContentBasedChunking {

	static long bytesPerWindow = PropertiesStore.getInt(PropertyNames.RABIN_WINDOW_SIZE);
	static Polynomial p = Polynomial.createFromLong(10923124345206883L);
	private RabinFingerprintLong fingerHash = new RabinFingerprintLong(p);
	private RabinFingerprintLongWindowed fingerWindow = new RabinFingerprintLongWindowed(p, bytesPerWindow);
	private ChunkBoundaryDetector boundaryDetector = BoundaryDetectors.DEFAULT_BOUNDARY_DETECTOR;
	private final static int MIN_CHUNK_SIZE = PropertiesStore.getInt(PropertyNames.T_MIN);
	private final static int MAX_CHUNK_SIZE = PropertiesStore.getInt(PropertyNames.T_MAX);
	
	// windowing fingerprinter for finding chunk boundaries. this is only
	// reset at the beginning of the file
	private final RabinFingerprintLong window = newWindowedFingerprint();
	// fingerprinter for chunks. this is reset after each chunk
	//private final RabinFingerprintLong fingerHash = newFingerprint();

	// counters
	long chunkStart = 0;
	long chunkEnd = 0;
	int chunkLength = 0;
	
	private List<Finger> breakpoints = new ArrayList<Finger>();
	private Map<Finger, Long> fingerprints = new LinkedHashMap<Finger, Long>();
	
	/**
	 * Fingerprint the file into chunks called "Fingers". The chunk boundaries
	 * are determined using a windowed fingerprinter
	 * {@link RabinFingerprintLongWindowed}.
	 * 
	 * The chunk detector is position independent. Therefore, even if a file is
	 * rearranged or partially corrupted, the untouched chunks can be
	 * efficiently discovered.
	 */
	public void digest (byte[] barray) {
		//If we dont want to scan for deduplication, just skip this		
		ByteBuffer buf = ByteBuffer.allocateDirect(MAX_CHUNK_SIZE);
		buf.clear();
		/*
		 * fingerprint one byte at a time. we have to use this granularity to
		 * ensure that, for example, a one byte offset at the beginning of the
		 * file won't effect the chunk boundaries
		 */
		for (byte b : barray) {
			// push byte into fingerprints
			window.pushByte(b);
			fingerHash.pushByte(b);
			chunkEnd++;
			chunkLength++;
			buf.put(b);
			/*
			 * if we've reached a boundary (which we will at some probability
			 * based on the boundary pattern and the size of the fingerprint
			 * window), we store the current chunk fingerprint and reset the
			 * chunk fingerprinter.
			 */
			if (boundaryDetector.isBoundary(window)	&& chunkLength > MIN_CHUNK_SIZE) {
				byte[] c = new byte[chunkLength];
				buf.position(0);
				buf.get(c);
				// store last chunk offset
				Finger finger = new Finger(fingerHash.getFingerprintLong(), chunkLength, chunkStart);
				breakpoints.add(finger);
				addFingerprint(finger); 
					//deduplicatedData.put(c);
				chunkStart = chunkEnd;
				chunkLength = 0;
				fingerHash.reset();
				buf.clear();
			} else if (chunkLength >= MAX_CHUNK_SIZE) {
				byte[] c = new byte[chunkLength];
				buf.position(0);
				buf.get(c);
				Finger finger = new Finger(fingerHash.getFingerprintLong(), chunkLength, chunkStart);
				breakpoints.add(finger);
				addFingerprint(finger);
				//	deduplicatedData.put(c);
				fingerHash.reset();
				buf.clear();
				// store last chunk offset
				chunkStart = chunkEnd;
				chunkLength = 0;
			}
		}
		byte[] c = new byte[chunkLength];
		buf.position(0);
		buf.get(c);
		Finger finger = new Finger(fingerHash.getFingerprintLong(), chunkLength, chunkStart);
		breakpoints.add(finger);
		addFingerprint(finger);
			//deduplicatedData.put(c);
		fingerHash.reset();
		buf.clear();
	}
	/**
	 * Ratio that represents Repeated data/all data. 
	 */
	public double calculateDeduplicationRatio(){
		long deduplicatedData = 0, allData = 0;
		for (Finger fingerprint: fingerprints.keySet()){
			long count = fingerprints.get(fingerprint);
			if (count > 1){
				deduplicatedData+=(count-1)*fingerprint.getLength();
			}
			allData+=count*fingerprint.getLength();
		}
		return ((double) deduplicatedData)/allData;
	}

	private RabinFingerprintLongWindowed newWindowedFingerprint() {
		return new RabinFingerprintLongWindowed(fingerWindow);
	}
	
	private boolean addFingerprint(Finger finger){
		boolean alreadyExists = true;
		if (!fingerprints.containsKey(finger)){
			fingerprints.put(finger, 0L);
			alreadyExists = false;
		}
		fingerprints.put(finger, fingerprints.get(finger)+1);
		return alreadyExists;
	}
	/**
	 * @return the fingerprints
	 */
	public Map<Finger, Long> getFingerprints() {
		return fingerprints;
	}
	/**
	 * @return the breakpoints
	 */
	public List<Finger> getBreakpoints() {
		return breakpoints;
	}

	public void reset() {
		breakpoints.clear();
		fingerprints.clear();
	}
}