/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphnearestneighbor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

/**
 * Represents a division of the nodes of a graph into
 * the connected components 'parts' after removing a special separator set
 * @author Nil
 */
class Partition {
	HashSet<Node> sepNodes;
	ArrayList<HashSet<Node>> parts;

	Partition(HashSet<Node> sepNodes, ArrayList<HashSet<Node>> parts) {
		this.sepNodes = sepNodes;
		this.parts = parts;
	}
	
	static Partition straightLineMethod(Graph graph) {
		int n = graph.numNodes();

		//consider a vertical separator
		ArrayList<Node> nodesX = nodesSortedBy(graph, 'X');
		HashSet<Node> nodesLeft = new HashSet<>(nodesX.subList(0, n/2));
		HashSet<Node> nodesRight = new HashSet<>(nodesX.subList(n/2, n));
		ArrayList<Edge> crossEdgesX = getCrossEdges(graph, nodesLeft, nodesRight);
		//and a horizontal separator
		ArrayList<Node> nodesY = nodesSortedBy(graph, 'Y');
		HashSet<Node> nodesDown = new HashSet<>(nodesY.subList(0, n/2));
		HashSet<Node> nodesUp = new HashSet<>(nodesY.subList(n/2, n));
		ArrayList<Edge> crossEdgesY = getCrossEdges(graph, nodesDown, nodesUp);

		//take the one with the smallest number of crossing edges
		if (crossEdgesX.size() < crossEdgesY.size()) {
			return partitionFromCrossEdges(graph, nodesLeft, crossEdgesX);
		} else {
			return partitionFromCrossEdges(graph, nodesDown, crossEdgesY);
		}
	}
    
	//dim can be 'X' or 'Y'
    static private ArrayList<Node> nodesSortedBy(Graph graph, char dim) {
		ArrayList<Node> sortedNodes = new ArrayList<> (graph.nodes);
		if (dim == 'X') {
			Collections.sort(sortedNodes, new Comparator<Node>(){
				@Override
				public int compare(Node n1, Node n2){
					if(n1.coords.x == n2.coords.x) return 0;
					return n1.coords.x < n2.coords.x ? -1 : 1;
				}
			});
		} else {
			assert dim == 'Y';
			Collections.sort(sortedNodes, new Comparator<Node>(){
				@Override
				public int compare(Node n1, Node n2){
					if(n1.coords.y == n2.coords.y) return 0;
					return n1.coords.y < n2.coords.y ? -1 : 1;
				}
			});
		}
		return sortedNodes;
    }

    static private ArrayList<Edge> getCrossEdges(Graph graph,
			HashSet<Node> left, HashSet<Node> right) {
		ArrayList<Edge> crossEdges = new ArrayList<>();
		for (Node u : graph.nodes) {
			for (Edge e : graph.adjList.get(u)) {
				Node v = e.getOther(u);
				if (v.label < u.label) continue; //to avoid repeated edges
				if ((left.contains(u) && right.contains(v)) ||
					(left.contains(v) && right.contains(u))) {
					crossEdges.add(e);
				}
			}
		}
		return crossEdges;
    }
	
	//receives a subset of nodes 'nodes', and the set of edges crossing between
	//a node in 'nodes' and a node outside 'nodes'.
	//creates a separator as a subset of 'nodes' as follows: for each cross edge,
	//make the endpoint that is in 'nodes' a separator
	static Partition partitionFromCrossEdges(Graph graph, HashSet<Node> nodes,
			ArrayList<Edge> crossEdges) {
		
		HashSet<Node> sepNodes = new HashSet<>();
		for (Edge e : crossEdges) {
			Node u = e.first, v = e.second;
			if (!sepNodes.contains(u) && !sepNodes.contains(v)) {
				if (nodes.contains(u)) {
					sepNodes.add(u);
				} else {
					sepNodes.add(v);
				}
			}
		}
		//partition the nodes of the graph into reachable subsets 
		//excluding separator nodes
		ArrayList<HashSet<Node>> parts = graph.connectedComps(sepNodes);
		return new Partition(sepNodes, parts);
	}
}
