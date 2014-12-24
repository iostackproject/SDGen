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

/**
 * @author Raul Gracia-Tinedo (raulgraciatinedo@gmail.com)
 *
 */

public class Finger {

	private Long hash;
	private int length;
	private long position;
	
	public Finger(Long hash, int length) {
		super();
		this.hash = hash;
		this.length = length;
	}
	
	public Finger(Long hash, int length, long position) {
		super();
		this.hash = hash;
		this.length = length;
		this.position = position;
	}
	
	@Override
	public int hashCode() {
		return hash.hashCode();//(int) (hash ^ (hash >>> 32));
	}
	
	@Override
	public boolean equals(Object obj) {
		Finger toCompare = (Finger) obj;
		return this.hash.hashCode() == toCompare.hash.hashCode();
	}

	/**
	 * @return the hash
	 */
	public Long getHash() {
		return hash;
	}
	/**
	 * @param hash the hash to set
	 */
	public void setHash(Long hash) {
		this.hash = hash;
	}
	/**
	 * @return the length
	 */
	public int getLength() {
		return length;
	}
	/**
	 * @param length the length to set
	 */
	public void setLength(int length) {
		this.length = length;
	}
	/**
	 * @return the position
	 */
	public long getPosition() {
		return position;
	}
	/**
	 * @param position the position to set
	 */
	public void setPosition(long position) {
		this.position = position;
	}
}