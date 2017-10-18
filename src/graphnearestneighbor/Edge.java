/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphnearestneighbor;

/**
 *
 * @author Nil
 */
class Edge {
	//stores the nodes in lexicographical order
	Node first, second;
	double length;

	Edge(Node first, Node second) {
		if (first.label <= second.label) { //put them in order
			this.first = first;
			this.second = second;
		} else {
			this.first = second;
			this.second = first;
		}
		length = Point.dist(first.coords, second.coords);
	}

	Node getOther(Node node) {
		assert node.equals(first) || node.equals(second);
		if (node.equals(first)) return second;
		return first;
	}
	
}
