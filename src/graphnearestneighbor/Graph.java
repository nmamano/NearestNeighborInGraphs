/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphnearestneighbor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Nil
 */
class Graph {
	ArrayList<Node> nodes;
	HashMap<Node, ArrayList<Edge>> adjList;
	
	Graph(ArrayList<Node> nodes, HashMap<Node, ArrayList<Edge>> adjList) {
		this.nodes = nodes;
		this.adjList = adjList;
	}
		
	int numNodes() {
		return nodes.size();
	}
	
	int numEdges() {
		int res = 0;
		for (ArrayList<Edge> neighbors : adjList.values()) {
			res += neighbors.size();
		}
		return res/2;
	}

	ArrayList<Edge> getNeighbors(Node node) {
		return adjList.get(node);
	}
	
	//partition the nodes of the graph into connected subsets, 
	//excluding the nodes in 'excludedNodes'
	ArrayList<HashSet<Node>> connectedComps(Set<Node> excludedNodes) {
		ArrayList<HashSet<Node>> res = new ArrayList<>();
		HashSet<Node> visited = new HashSet<>(excludedNodes);
		for (Node node : nodes) {
			if (!visited.contains(node)) {
				HashSet<Node> reachedNodes = rechableNodesFrom(node, visited);
				res.add(reachedNodes);
			}
		}
		return res;
	}
	
	//returns the set of nodes reachable from s, excluding any node in 'excludedNodes'
	//as a side effect, it adds the result nodes to excludedNodes
	private HashSet<Node> rechableNodesFrom(Node s, HashSet<Node> excludedNodes) {
		ArrayList<Node> toVisit = new ArrayList<>();
		HashSet<Node> newlyVisited = new HashSet<>();
		toVisit.add(s);
		while (!toVisit.isEmpty()) {
			Node u = toVisit.get(toVisit.size()-1);
			toVisit.remove(toVisit.size()-1);
			excludedNodes.add(u);
			newlyVisited.add(u);
			for (Edge e : getNeighbors(u)) {
				Node v = e.getOther(u);
				if (!excludedNodes.contains(v)) {
					toVisit.add(v);
				}
			}
		}
		return newlyVisited;
	}

	static Graph subgraph(Graph graph, HashSet<Node> nodeSet) {
		ArrayList<Node> nodes = new ArrayList<>(nodeSet);
		HashMap<Node, ArrayList<Edge>> adjList = new HashMap<>();
		for (Node node : nodes) {
			ArrayList<Edge> neighbors = new ArrayList<>();
			for (Edge edge : graph.getNeighbors(node)) {
				Node neighbor = edge.getOther(node);
				if (nodeSet.contains(neighbor)) {
					neighbors.add(edge);
				}
			}
			adjList.put(node, neighbors);
		}
		return new Graph(nodes, adjList);
	}
	
	static Graph biggestConnectedComponent(Graph graph) {
		ArrayList<ArrayList<Node>> CCs = new ArrayList<>();
		HashSet<Node> visited = new HashSet<>();
		for (Node node : graph.nodes) {
			if (!visited.contains(node)) {
				HashSet<Node> reachedNodes = graph.rechableNodesFrom(node, visited);
				CCs.add(new ArrayList<>(reachedNodes));
			}
		}
		
		int maxSize = 0;
		ArrayList<Node> maxCC = null;
		for (ArrayList<Node> CC : CCs) {
			if (CC.size() > maxSize) {
				maxSize = CC.size();
				maxCC = CC;
			}
		}
		return subgraph(graph, new HashSet<>(maxCC));
	}


}
