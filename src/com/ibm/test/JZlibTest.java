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
package com.ibm.test;

import com.ibm.compression.JZlibCompression;
import com.ibm.compression.ZlibCompression;
import com.ibm.utils.Utils;

/**
 * @author Raul Gracia-Tinedo (raulgraciatinedo@gmail.com)
 *
 */
public class JZlibTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/*String test = "hello hello hello hello hello hello hello hello hello"+
				"hello hello hello hello hello hello hello hello hello"+
				"hello hello hello hello hello hello hello hello hello"+
				"hello hello hello hello hello hello hello hello hello";*/
		String test = "12233344445555566666122333444455555666661223334444555556666612233344445555566666";
		JZlibCompression compression = new JZlibCompression();
		ZlibCompression compression2 = new ZlibCompression();
		byte[] compressedData = compression.compress(test.getBytes());
		byte[] compressedData2 = compression2.compress(test.getBytes());
		System.out.println(compressedData.length + " - " + Utils.getCompressionRatio(
				test.getBytes(), compressedData));
		System.out.println(compressedData2.length + " - " + Utils.getCompressionRatio(
				test.getBytes(), compressedData2));
		String testRecovered = new String(compression.decompress(compressedData));
		assert testRecovered.equals(test);
		System.out.println(testRecovered.length() + "-" + testRecovered);
		System.exit(0);
	}
}