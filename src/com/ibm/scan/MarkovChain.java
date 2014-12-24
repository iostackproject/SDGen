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
package com.ibm.scan;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.ibm.exception.DataGenerationException;
import com.ibm.generation.FitnessProportionateSelection;

/**
 * @author Raul Gracia-Tinedo (raulgraciatinedo@gmail.com)
 *
 */
public class MarkovChain {
	
	private Map<Integer, GraphNode> nodes = new HashMap<Integer, GraphNode>();
	
	private GraphNode currentElement;
	
	public void addElement(int  element) {
		GraphNode node = null;
		if (!nodes.containsKey(element)) {
			node = new GraphNode(element);
			nodes.put(element, node);
		}else{
			node = nodes.get(element);
		}
		if (currentElement!=null) {
			currentElement.updateLinks(node);
			node.updateLinks(currentElement);
		}
		currentElement=node;
	}
	
	public int getNextChainElement() {
		Iterator<Integer> it = null;
		do{
			if (currentElement==null) {
				it = nodes.keySet().iterator();
				currentElement = nodes.get(it.next());
			}
			GraphNode node = currentElement.getNextChainElement();
			currentElement = node;
		}while(currentElement==null);
		return currentElement.element;
	}
	
	public int getNextElementFor(int element) {
		GraphNode node = nodes.get(element);
		return node.getNextChainElement().element;
	}
	
	private class GraphNode {
		
		int element;
		
		Map<GraphNode, Long> links;
		
		FitnessProportionateSelection<GraphNode> selector;

		GraphNode(int element) {
			this.element = element;
			links = new HashMap<GraphNode, Long>();
			selector = new FitnessProportionateSelection<>(links);
		}

		/**
		 * @return
		 */
		public GraphNode getNextChainElement() {
			try {
				return selector.generateProportionalKeys();
			} catch (DataGenerationException e) {
				e.printStackTrace();
			}
			return null;
		}

		/**
		 * @param node
		 */
		public void updateLinks(GraphNode node) {
			long occurrences = 1;
			if (links.containsKey(node)){
				occurrences = links.get(node);
			}
			links.put(node, occurrences);
		}

		@Override
		public boolean equals(Object obj) {
			GraphNode toCompare = (GraphNode) obj;
			return this.element == toCompare.element;
		}

		@Override
		public int hashCode() {
			return element;
		}		
	}
}