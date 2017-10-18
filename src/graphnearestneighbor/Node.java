/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphnearestneighbor;

/**
 * for simplicity in constructing the separator hierarchy,
 * the same node can appear in several graphs
 * @author Nil
 */
class Node {
		int label; //the label is not the index in the adjacency list;
				   //it is just used to identify nodes
		Point coords;

		Node(int label, Point coords) {
			this.label = label;
			this.coords = coords;
		}
		
		@Override
		public String toString() {
			return String.valueOf(label);
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj == null) return false;
			return label == ((Node) obj).label;
		}

		@Override
		public int hashCode() {
			return 67 * label;
		}

	}