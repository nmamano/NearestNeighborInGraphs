/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphnearestneighbor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * naive implementation of the NN structure
 * based on running Dijkstra for queries
 * @author Nil
 */
class DijkstraStruct extends NNStruct {
	final private Graph graph;
	final private ArrayGraph arrayGraph;
	boolean[] isSite;
	
	DijkstraStruct(Graph graph, HashSet<Node> sites) {
		this.graph = graph;
		this.arrayGraph = new ArrayGraph(graph);
		isSite = new boolean[graph.numNodes()];
		for (int i = 0; i < graph.numNodes(); i++) isSite[i] = false;
		for (Node node : sites) isSite[node.label] = true;
	}
	
	DijkstraStruct(Graph graph) {
		this(graph, new HashSet<Node>());
	}

	@Override
	Node query(Node queryNode) {
		return nearestTarget(arrayGraph, queryNode, isSite);
	}

	@Override
	void remove(Node node) {
		isSite[node.label] = false;
	}

	@Override
	void add(Node node) {
		isSite[node.label] = true;
	}

	@Override
	void setSites(HashSet<Node> sites) {
		for (int i = 0; i < arrayGraph.numNodes(); i++) isSite[i] = false;
		for (Node node : sites) isSite[node.label] = true;
	}

	@Override
	String name() {
		return "dijkstra";
	}
	
	@Override
	String acronym() {
		return "D";
	}
	
	@Override
	Graph getGraph() {
		return graph;
	}

	//experiments showed that it is generally faster to use a hashmap for dists
	//than an array because of the initialization time
	private static Node nearestTarget(ArrayGraph G, Node s, boolean[] isTarget) {
        HashMap<Node,Double> dists = new HashMap<>();

        BinaryHeap<Node> Q = new BinaryHeap();
        dists.put(s, 0.0);
		Q.add(s, 0.0);
        
        while (!Q.isEmpty()) {
            Node node = Q.extractMin();
			if (isTarget[node.label]) {
				return node;
			}
            for (Edge e : G.adjList.get(node.label)) {
                Node neighbor = e.getOther(node);
                double newDist = dists.get(node) + e.length;
                if (!dists.containsKey(neighbor)) {
					dists.put(neighbor, newDist);
					Q.add(neighbor, newDist);
				} else if (newDist < dists.get(neighbor)) {
                    dists.put(neighbor, newDist);
                    Q.decreaseKey(neighbor, newDist);
                }
            }
        }
		//no target is reachable
		return null;
	}
	
/**
 * alternative implementation of graph using an array list instead of a hash map
 * to make Dijkstra faster it assumes that node labels go from 0 to n-1
 * @author Nil
 */
	static class ArrayGraph {
		ArrayList<Node> nodes;
		ArrayList<ArrayList<Edge>> adjList;

		ArrayGraph(Graph G) {
			this.nodes = G.nodes;
			adjList = new ArrayList<>(G.numNodes());
			for (Node node : nodes) adjList.add(new ArrayList<Edge>());
			for (Node node : nodes) {
				adjList.set(node.label, G.getNeighbors(node));
			}
		}

		int numNodes() {
			return nodes.size();
		}

	}

}

