
package graphnearestneighbor;

import java.util.HashSet;

/**
 * separator-based NN data structure
 * @author Nil
 */
class SeparatorStruct extends NNStruct {

	Graph graph;
	HashSet<Node> sites;
	SepHierarchy hierarchy;
	
	//indicates whether the pruning optimization in queries should be used
	boolean useSortOptimization;

	SeparatorStruct(Graph graph, HashSet<Node> sites, boolean useSortOptimization) {
		this.graph = graph;
		this.sites = new HashSet<>(sites);
		this.useSortOptimization = useSortOptimization;
		hierarchy = new SepHierarchy(graph, sites, useSortOptimization);
	}
	
	SeparatorStruct(Graph graph, boolean useSortOptimization) {
		this(graph, new HashSet<Node>(), useSortOptimization);
	}
	
	@Override
	Node query(Node queryNode) {
		return hierarchy.query(queryNode).first;
	}

	@Override
	void remove(Node node) {
		if (!sites.contains(node)) return;
		sites.remove(node);
		hierarchy.remove(node);
	}

	@Override
	void add(Node node) {
		if (sites.contains(node)) return;
		sites.add(node);
		hierarchy.add(node);
	}
	
	@Override
	void setSites(HashSet<Node> newSites) {
		sites = new HashSet<>(newSites);
		hierarchy.setSites(newSites);
	}
	
	@Override
	String name() {
		return "separator";
	}
	
	@Override
	String acronym() {
		return "S" + (useSortOptimization ? "O" : "N");
	}
	
	@Override
	Graph getGraph() {
		return graph;
	}
	
	//prints some statistics about the data structure
	@Override
	public String toString() {
		int n = graph.numNodes();
		int BC = SepHierarchy.BASE_CASE_SIZE;
		String str = "nodes: "+n+", edges: "+graph.numEdges()+", "+
				"sites: "+sites.size()+", base case: "+BC+"\n";
		str += "depth: "+hierarchy.depth()+" (expected: "+
				expectedBestDepth(n, BC)+" - "+expectedWorstDepth(n, BC)+") "+
			   "size: "+hierarchy.size()+" (expected: "+
				expectedBestSize(n, BC)+" - "+expectedWorstSize(n, BC)+") "+
			   "num nodes: "+hierarchy.totalNumNodes()+" (n sqrt n: "+utils.prettyStr(n*Math.sqrt(n), 1)+")\n";
		//uncomment the following line to show the full tree of
		//graph/separator sizes of the hierarchy
		//str += "separator size tree:\n"+hierarchy.separatorSizeTree("");
		return str;
	}

	//worst expected depth of the hierarchy for a graph with 'numNodes' nodes.
	//it happens when one side of the separator always contains 2/3 of the nodes
	private static int expectedWorstDepth(int numNodes, int baseCaseSize) {
		if (numNodes <= baseCaseSize) return 1;
		return 1+expectedWorstDepth(numNodes*2/3, baseCaseSize);
	}
	
	//best expected depth of the hierarchy for a graph with 'numNodes' nodes.
	//it happens when separators always split the nodes evenly
	private static int expectedBestDepth(int numNodes, int baseCaseSize) {
		if (numNodes <= baseCaseSize) return 1;
		Double sq = Math.sqrt(numNodes);
		int subgraphSize = (numNodes-sq.intValue())/2;
		return 1+expectedBestDepth(subgraphSize, baseCaseSize);
	}
	
	//worst expected number of graphs in the hierarchy for a graph with 'numNodes' nodes.
	//it happens when one side of the separator always contains 2/3 of the nodes
	private static int expectedWorstSize(int numNodes, int baseCaseSize) {
		if (numNodes <= baseCaseSize) return 1;
		return 1+expectedWorstSize(numNodes*2/3, baseCaseSize)+expectedWorstSize(numNodes*1/3, baseCaseSize);
	}
	
	//best expected number of graphs in the hierarchy for a graph with 'numNodes' nodes.
	//it happens when separators always split the nodes evenly
	private static int expectedBestSize(int numNodes, int baseCaseSize) {
		if (numNodes <= baseCaseSize) return 1;
		Double sq = Math.sqrt(numNodes);
		int subgraphSize = (numNodes-sq.intValue())/2;
		return 1+2*expectedBestSize(subgraphSize, baseCaseSize);
	}
}
