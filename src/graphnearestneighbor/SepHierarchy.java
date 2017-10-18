
package graphnearestneighbor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

/**
 * Graph separator hierarchy enhanced with the extra information needed for the
 * separator-based NN data structure
 * @author Nil
 */
class SepHierarchy {

	static class Separator {
		Node node;		
		//maps the nodes to their distances from the separator
		//Double.MAX_VALUE indicates that the node is not reachable
		HashMap<Node,Double> dists; 
		//sites prioritized by their distance to this separator
		BinaryHeap<Node> siteHeap; 		

		Separator(Node node, Graph graph, HashSet<Node> sites) {
			this.node = node;
			dists = Dijkstra.getDistances(graph, node);
			siteHeap = initHeap(sites);
		}
		
		void setSites(HashSet<Node> newSites) {
			siteHeap = initHeap(newSites);
		}
				
		//assumes newSite is not already a site
		private void add(Node newSite) {
			siteHeap.add(newSite, dists.get(newSite));
		}

		//assumes remSite is a site
		private void remove(Node remSite) {
			siteHeap.remove(remSite);
		}
		
		final BinaryHeap<Node> initHeap(HashSet<Node> sites) {
			ArrayList<Node> qNodes = new ArrayList<>();
			ArrayList<Double> qDists = new ArrayList<>();
			for (Node u : sites) {
				if (dists.get(u) < Double.MAX_VALUE) {
					qNodes.add(u);
					qDists.add(dists.get(u));
				}
			}
			return new BinaryHeap<>(qNodes, qDists);
		}
	}
	
	static int BASE_CASE_SIZE = 20;
	ArrayList<Separator> separators;
	ArrayList<SepHierarchy> children;
	
	//mapping that allows to find in which child graph a node is
	HashMap<Node,SepHierarchy> nodeToChild;

	//separators sorted by distance to each node, which is needed for the query
	//prunning optimization
	HashMap<Node,ArrayList<Separator>> nodeToSortedSeparators;
	//activate or deactivate the optimization
	boolean useSortOptimization;
	
	boolean isBaseCase;
	Graph baseCaseGraph; //only used in base case
	HashSet<Node> baseCaseSites; //only used in base case
	
	int numNodes() {
		if (isBaseCase) return baseCaseGraph.numNodes();
		return nodeToChild.size();
	}
	
	//assumes that the graph is connected
	SepHierarchy(Graph graph, HashSet<Node> sites, boolean useSortOptimization) {
		this.useSortOptimization = useSortOptimization;
		isBaseCase = graph.numNodes() <= BASE_CASE_SIZE;
		if (isBaseCase) {
			baseCaseGraph = graph;
			baseCaseSites = sites;
			return;
		}
		
		Partition part = Partition.straightLineMethod(graph);
		separators = initSeparators(graph, part, sites);
		nodeToSortedSeparators = sortSeparatorsFromNodes(graph, separators);
		ArrayList<Graph> childrenGraphs = initChildrenGraphs(graph, part);
		ArrayList<HashSet<Node>> childrenSites = getChildrenSites(part, sites);
		children = new ArrayList();
		for (int i = 0; i < childrenGraphs.size(); i++) {
			children.add(new SepHierarchy(childrenGraphs.get(i), childrenSites.get(i), useSortOptimization));
		}
		nodeToChild = getNodeToChild(childrenGraphs, children, part.sepNodes);
	}
	
	private static ArrayList<Separator> initSeparators(Graph graph,
			Partition part, HashSet<Node> sites) {
		ArrayList<Separator> separators = new ArrayList<>(part.sepNodes.size());
		for (Node node : part.sepNodes) {
			separators.add(new Separator(node, graph, sites));
		}
		return separators;
	}
	
	private static HashMap<Node,ArrayList<Separator>> sortSeparatorsFromNodes(
			Graph graph, ArrayList<Separator> separators) {
		HashMap<Node,ArrayList<Separator>> res = new HashMap<>();
		for (final Node node : graph.nodes) {
			ArrayList<Separator> sortedSeps = new ArrayList<>(separators);
			Collections.sort(sortedSeps, new Comparator<Separator>(){
				@Override
				public int compare(Separator s1, Separator s2){
					if(Objects.equals(s1.dists.get(node), s2.dists.get(node))) return 0;
					return s1.dists.get(node) < s2.dists.get(node) ? -1 : 1;
				}
			});
			res.put(node, sortedSeps);
		}
		return res;
	}
	
	static private ArrayList<Graph> initChildrenGraphs(Graph graph, Partition part) {
		ArrayList<Graph> res = new ArrayList<>();
		for (HashSet<Node> nodeSet : part.parts) {
			res.add(Graph.subgraph(graph, nodeSet));
		}
		return res;
	}
	
	static private ArrayList<HashSet<Node>> getChildrenSites(Partition part,
			HashSet<Node> sites) {
		ArrayList<HashSet<Node>> res = new ArrayList<>();
		for (HashSet<Node> nodeSet : part.parts) {
			HashSet<Node> childSites = new HashSet<>();
			for (Node site : sites) {
				if (nodeSet.contains(site)) {
					childSites.add(site);
				}
			}
			res.add(childSites);
		}
		return res;
	}
	
	static private HashMap<Node,SepHierarchy> getNodeToChild(
			ArrayList<Graph> childrenGraphs, ArrayList<SepHierarchy> children,
			HashSet<Node> sepNodes) {
		HashMap<Node,SepHierarchy> res = new HashMap<>();
		for (Node sepNode : sepNodes) {
			res.put(sepNode, null);
		}
		for (int i = 0; i < children.size(); i++) {
			SepHierarchy child = children.get(i);
			Graph childGraph = childrenGraphs.get(i);
			for (Node node : childGraph.nodes) {
				res.put(node, child);
			}
		}
		return res;
	}
	
	void setSites(HashSet<Node> newSites) {
		if (isBaseCase) {
			baseCaseSites = newSites;
			return;
		}
		//update separators in this level
		for (Separator sep : separators) {
			sep.setSites(newSites);
		}
		//update separators in children
		HashMap<SepHierarchy,HashSet<Node>> childrenToNewSites = new HashMap<>();
		for (SepHierarchy child : children) childrenToNewSites.put(child, new HashSet<Node>());
		for (Node newSite : newSites) {
			SepHierarchy child = nodeToChild.get(newSite);
			if (child == null) continue;
			HashSet<Node> siteSet = childrenToNewSites.get(child);
			siteSet.add(newSite);
		}
		for (SepHierarchy child : children) {
			child.setSites(childrenToNewSites.get(child));
		}
	}

	utils.Pair<Node,Double> query(Node queryNode) {
		if (isBaseCase) {
			return Dijkstra.nearestTarget(baseCaseGraph, queryNode, baseCaseSites);
		}
		
		//look at an arbitrary separator to check if there are no sites:
		if (separators.get(0).siteHeap.isEmpty()) return new utils.Pair<>(null, Double.MAX_VALUE);


		//get a candidate solution recursively
		utils.Pair<Node,Double> bestCand = new utils.Pair<>(null, Double.MAX_VALUE);
		SepHierarchy child = nodeToChild.get(queryNode);
		if (child != null) {
			bestCand = child.query(queryNode);
		}
		
		//look through the separators to see if they improve
		for (Separator sep : nodeToSortedSeparators.get(queryNode)) {
			double qNodeToSepDist = sep.dists.get(queryNode);
			//we can stop looking once the separator is further than the closest
			//site found so far
			//(but we only do so if the optimization is enabled)
			if (useSortOptimization && qNodeToSepDist >= bestCand.second) break;
			
			double sepToSiteDist = sep.siteHeap.getMinKey();
			double qNodeToSiteDist = qNodeToSepDist + sepToSiteDist;
			if (qNodeToSiteDist < bestCand.second) {
				bestCand.first = sep.siteHeap.getMin();
				bestCand.second = qNodeToSiteDist;
			}
		}
		
		return bestCand;
	}

	//assumes that remSite is a site
	void remove(Node remSite) {
		if (isBaseCase) {
			baseCaseSites.remove(remSite);
			return;
		}
		for (Separator sep: separators) {
			sep.remove(remSite);
		}
		SepHierarchy child = nodeToChild.get(remSite);
		if (child != null) child.remove(remSite);
	}

	//assumes that newSite is not already a site
	void add(Node newsite) {
		if (isBaseCase) {
			baseCaseSites.add(newsite);
			return;
		}
		for (Separator sep : separators) {
			sep.add(newsite);
		}
		SepHierarchy child = nodeToChild.get(newsite);
		if (child != null) child.add(newsite);
	}

	//returns depth of the hierarchy
	int depth() {
		if (isBaseCase) return 1;
		int maxChildDepth = 0;
		for (SepHierarchy child : children) {
			maxChildDepth = Math.max(maxChildDepth, child.depth());
		}
		return maxChildDepth+1;
	}

	//returns the size (in number of graphs) of the hierarchy
	int size() {
		if (isBaseCase) return 1;
		int res = 1;
		for (SepHierarchy child : children) {
			res += child.size();
		}
		return res;
	}
	
	//returns the number of nodes across all the graphs in the hierarchy
	int totalNumNodes() {
		if (isBaseCase) return baseCaseGraph.numNodes();
		int numNodes = nodeToChild.size();
		for (SepHierarchy child : children) {
			numNodes += child.totalNumNodes();
		}
		return numNodes;
	}
	
	//returns a string showing the size of all the graphs and separators
	//in the hierarchy
	String separatorSizeTree(String prefix) {
		if (isBaseCase) return "";
		String res = prefix+separators.size()+
				" (n: "+numNodes()+", sqrt n: "+
				utils.prettyStr(Math.sqrt(numNodes()), 1)+") (children sizes:";
		for (SepHierarchy child : children) res += " "+child.numNodes();
		res += ")\n";
		for (SepHierarchy child : children) {
			res += child.separatorSizeTree(prefix+"    ");
		}
		return res;
	}
	

}
