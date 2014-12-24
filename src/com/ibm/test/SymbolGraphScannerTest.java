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

import java.io.File;

import com.ibm.characterization.DoublesHistogram;
import com.ibm.characterization.Histogram;
import com.ibm.scan.user.EntropyScanner;
import com.ibm.utils.Utils;

/**
 * @author Raul Gracia-Tinedo (raulgraciatinedo@gmail.com)
 *
 */
public class SymbolGraphScannerTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		EntropyScanner entropyMapper = new EntropyScanner();
		String fileName = "/home/raul/workspace/ContentDefinableGenerator/" +
				"results_variable_size_chunks.dat";
		String fileName2 = "/home/raul/Desktop/cantrbry/grammar.lsp";//"/home/raul/Desktop/impressions-v1.tar.gz";
		Histogram entropyHistogram = new DoublesHistogram();
		for (File file: Utils.getDirectoryFiles("/home/raul/Desktop/cantrbry/")){
			//entropyHistogram.mergeHistograms((Histogram)entropyMapper.fullScan(file.getAbsolutePath(), 4*1024));
		}			
		System.out.println(entropyHistogram.toString());
		entropyHistogram.finishScan();
		for (int i = 0; i < 25; i++)
			System.out.println(entropyHistogram.getHistogramBasedValue());
	}
}