/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphnearestneighbor;

import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author Nil
 */
class Dijkstra {

	static HashMap<Node,Double> getDistances(Graph G, Node s) {
        HashMap<Node,Double> dists = new HashMap<>();
		for (Node node : G.nodes) {
			dists.put(node, Double.MAX_VALUE);
        }

		BinaryHeap<Node> Q = new BinaryHeap(G.nodes, Double.MAX_VALUE);
        dists.put(s, 0.0);
		Q.decreaseKey(s, 0.0);
        
        while (!Q.isEmpty()) {
            Node node = Q.extractMin();
            for (Edge e : G.getNeighbors(node)) {
                Node neighbor = e.getOther(node);
                double newDist = dists.get(node) + e.length;
                if (newDist < dists.get(neighbor)) {
                    dists.put(neighbor, newDist);
                    Q.decreaseKey(neighbor, newDist);
                }
            }
        }
		return dists;
    } 

	/*
	Returns the closest node in 'targets' from 's', paired with the distance
	returns (null, Double.MAX) if no target is reachable
	*/
	static utils.Pair<Node, Double> nearestTarget(Graph G, Node s, HashSet<Node> targets) {
        HashMap<Node,Double> dists = new HashMap<>();
		for (Node node : G.nodes) {
			dists.put(node, Double.MAX_VALUE);
        }

		
        BinaryHeap<Node> Q = new BinaryHeap(G.nodes, Double.MAX_VALUE);
        dists.put(s, 0.0);
		Q.decreaseKey(s, 0.0);
        
        while (!Q.isEmpty()) {
            Node node = Q.extractMin();
			if (targets.contains(node)) {
				return new utils.Pair<>(node, dists.get(node));
			}
            for (Edge e : G.getNeighbors(node)) {
                Node neighbor = e.getOther(node);
                double newDist = dists.get(node) + e.length;
                if (newDist < dists.get(neighbor)) {
                    dists.put(neighbor, newDist);
                    Q.decreaseKey(neighbor, newDist);
                }
            }
        }
		//no target is reachable
		return new utils.Pair<>(null, Double.MAX_VALUE);
	}

}
