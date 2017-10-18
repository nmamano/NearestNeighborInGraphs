
package graphnearestneighbor;

import java.util.HashSet;

/**
 * reactive nearest neighbor data structure in graphs
 * it can answer queries asking for the closest site to a query node
 * it can also add or remove sites
 * or take an entirely new set of sites
 */
abstract class NNStruct {
	
	//returns null if there are no reachable sites from the query node
	abstract Node query(Node queryNode);
	
	//does nothing if node is not a site
	abstract void remove(Node node);
	
	//does nothing is node is not a site
	abstract void add(Node node);
	
	//discards current set of sites
	abstract void setSites(HashSet<Node> newSites);

	abstract String name();
	abstract String acronym();
	
	abstract Graph getGraph();
	
}
