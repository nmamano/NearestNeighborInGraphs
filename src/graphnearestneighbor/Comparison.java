/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphnearestneighbor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Class to benchmark different NN data structures
 * @author Nil
 */
class Comparison {

	private static ArrayList<DataPoint> data;
	private static ArrayList<NNStruct> structs;
	
	//contains all the info related to one particular run
	static private class DataPoint {
		NNStruct struct; //'D' for Dijktsra, 'S' for Separator
		int siteNum;
		char opType; //'Q' for queries only, 'U' for updates only, 'M' for mixed
		int numOps;
		int runId;
		double time;
		ArrayList<Node> answers;
		DataPoint(NNStruct struct, int siteNum, char opType, int numOps,
				int runId, double time, ArrayList<Node> answers) {
			this.struct = struct; this.siteNum = siteNum; this.opType = opType;
			this.numOps = numOps; this.runId = runId; this.time = time;
			this.answers = answers;
		}
		@Override
		public String toString() {
			return "{"+struct.name()+", "+siteNum+", "+opType+", "+runId+", "+utils.prettyStr(time, 2)+"}";
		}
	}
	
	//represents an operation for a NN data structure
	static class Operation {
		char type; //'Q' for query, 'A' for add, and 'R' for remove
		Node node;
		Operation(char type, Node node) {
			this.type = type; this.node = node;
		}
		
		@Override
		public String toString() {
			return String.valueOf(type)+" "+node;
		}
	}
			
	static void experiment(String graphName) throws IOException {
		Graph G = GraphLoader.load(graphName);
		generateData(G);
		printDataCSV(G, true);
	}
	
	//executes all the runs and stores them in 'data'
	private static void generateData(Graph G) {
		structs = initStructs(G);
		ArrayList<Integer> siteNums = initSiteNums(G);
		ArrayList<Character> opTypes = initOpTypes();
		int numRuns = 5;
		int numOps = 1000;
		data = new ArrayList<>();
		generateDataPoints(G, structs, siteNums, opTypes, numRuns, numOps);
	}
	
	private static ArrayList<NNStruct> initStructs(Graph G) {
		ArrayList<NNStruct> res = new ArrayList<>();
		Timer T = new Timer();
		T.start();
		res.add(new DijkstraStruct(G));
		System.err.println("dijktsra init time: "+T.stop());
		T.start();
		//separator-based struct without optimization:
		res.add(new SeparatorStruct(G, false));
		System.err.println("separator init time: "+T.stop());
		//separator-based struct with optimization:
		res.add(new SeparatorStruct(G, true));
		return res;
	}

	private static ArrayList<Integer> initSiteNums(Graph G) {
		ArrayList<Integer> siteNums = new ArrayList<>();
		int i = 2;
		while (i < G.numNodes()/2) {
			siteNums.add(i);
			i *= 2;
		}
		return siteNums;
	}
	
	//represents the type of operations that will be executed in a run
	//'Q' for only query, 'U' for only updates, and 'M' for mixed
	private static ArrayList<Character> initOpTypes() {
		ArrayList<Character> opTypes = new ArrayList<>();
		opTypes.add('Q');
		opTypes.add('U'); 
		opTypes.add('M');
		return opTypes;
	}

	private static void generateDataPoints(Graph G, ArrayList<NNStruct> structs, ArrayList<Integer>
			siteNums, ArrayList<Character> opTypes, int numRuns, int numOps) {
		for (Integer siteNum : siteNums) {
			for (char opType : opTypes) {
				for (int i = 0; i < numRuns; i++) {
					//same sites and operations for every struct
					HashSet<Node> sites = generateSites(siteNum, G);
					ArrayList<Operation> ops = generateOps(numOps, opType, sites, G);
					for (NNStruct struct : structs) {
						data.add(generateDataPoint(struct, sites, opType, ops, i));
					}
				}
			}
		}
	}
	
	private static DataPoint generateDataPoint(NNStruct struct, HashSet<Node> sites, 
			char opType, ArrayList<Operation> ops, int runId) {
		ArrayList<Node> answers = new ArrayList<>();
		struct.setSites(sites);
		Timer T = new Timer();
		for (Operation op : ops) {
			if (op.type == 'Q') {
				Node answer = struct.query(op.node);
				answers.add(answer);
			}
			else if (op.type == 'A') {
				struct.add(op.node);
			} else {
				struct.remove(op.node);
			}
		}
		double time = T.stop();
		System.err.println("siteNum:"+sites.size()+" opType:"+opType+" numRun:"+
				runId+" struct:"+struct.acronym()+" time:"+time);
		DataPoint dp = new DataPoint(struct, sites.size(), opType,
				ops.size(), runId, time, answers);
		checkCorrecntness(dp, ops);
		return dp;
	}

	static private HashSet<Node> generateSites(int k, Graph G) {
		int n = G.numNodes();
		assert k <= n;
		
		HashSet<Node> sites = new HashSet<>();
		while (sites.size() < k) {
			int randIndex = utils.randInt(0, n-1);
			sites.add(G.nodes.get(randIndex)); //it will not add repeated nodes
		}
		return sites;
	}
	
	private static ArrayList<Operation> generateOps(int numOps, char opType, 
			HashSet<Node> startSites, Graph G) {
		if (opType == 'Q') return generateQueries(numOps, G);
		if (opType == 'U') return generateUpdates(numOps, startSites, G);
		return generateMixedOps(numOps, startSites, G);
	}
	
	static private ArrayList<Operation> generateQueries(int numOps, Graph G) {
		ArrayList<Operation> res = new ArrayList<>();
		for (int i = 0; i < numOps; i++) {
			res.add(new Operation('Q', randomNode(G)));
		}
		return res;
	}
	
	static private ArrayList<Operation> generateUpdates(int numOps,
			HashSet<Node> startSites, Graph G) {
		ArrayList<Operation> res = new ArrayList<>();
		HashSet<Node> sites = new HashSet<>(startSites);
		for (int i = 0; i < numOps; i++) {
			if (i%2 == 0) {
				Operation addOp = new Operation('A', randomNodeExcept(G, sites));
				res.add(addOp);
				sites.add(addOp.node);
			} else {
				Operation remOp = new Operation('R', randomNodeFrom(sites));
				res.add(remOp);
				sites.remove(remOp.node);
			}
		}
		return res;
	}
	
	static private ArrayList<Operation> generateMixedOps(int numOps, 
			HashSet<Node> startSites, Graph G) {
		ArrayList<Operation> res = new ArrayList<>();
		HashSet<Node> sites = new HashSet<>(startSites);
		for (int i = 0; i < numOps; i++) {
			if (i%2 == 0) {
				res.add(new Operation('Q', randomNode(G)));
			} else if (i%4 == 1) {
				Operation addOp = new Operation('A', randomNodeExcept(G, sites));
				res.add(addOp);
				sites.add(addOp.node);
			} else {
				Operation remOp = new Operation('R', randomNodeFrom(sites));
				res.add(remOp);
				sites.remove(remOp.node);
			}
		}
		return res;
	}

	static private Node randomNode(Graph G) {
		int randIndex = utils.randInt(0, G.numNodes()-1);
		return G.nodes.get(randIndex);
	}
	
	static private Node randomNodeExcept(Graph G, HashSet<Node> exceptions) {
		int randIndex;
		do {
			randIndex = utils.randInt(0, G.numNodes()-1);
		} while(exceptions.contains(G.nodes.get(randIndex)));
		return G.nodes.get(randIndex);
	}
	
	static private Node randomNodeFrom(HashSet<Node> nodes) {
		ArrayList<Node> list = new ArrayList<>(nodes);
		int randIndex = utils.randInt(0, list.size()-1);
		return list.get(randIndex);
	}

	//this checks that all the data structures give the same output to the same input
	//the answers of this particular data point 'dp1' are compared against the
	//answers of the other data structures with the same input
	//this suffices as long as there is at least one correct method
	private static void checkCorrecntness(DataPoint dp1, ArrayList<Operation> ops) {
		for (DataPoint dp2 : data) {
			if (dp1.struct.acronym().equals(dp2.struct.acronym())) continue;
			boolean sameOps = dp1.siteNum == dp2.siteNum &&
					dp1.opType == dp2.opType &&
					dp1.runId == dp2.runId;
			if (sameOps && !dp1.answers.equals(dp2.answers)) {
				reportMismatch(dp1, dp2, ops);
			}
		}
	}
	
	//in case two methods gave different answers to the same input, we check if
	//both answers are at the same distance from the query node. If this is true,
	//there is a tie and both answers are correct. Otherwise, we print the discrepancy
	private static void reportMismatch(DataPoint dp1, DataPoint dp2, ArrayList<Operation> ops) {
		int i = 0;
		for (Operation op : ops) {
			if (op.type == 'Q') {
				Node ans1 = dp1.answers.get(i), ans2 = dp2.answers.get(i);
				if (!ans1.equals(ans2)) {
					HashMap<Node,Double> dists = Dijkstra.getDistances(dp1.struct.getGraph(), op.node);
					double dist1 = dists.get(ans1), dist2 = dists.get(ans2);
					if (dist1 != dist2) {
						System.err.println("answers differ for the same input\n"+dp1+" "+dp2);
						System.err.println(ans1+" (dist "+utils.prettyStr(dist1, 6)+") vs "+ans2+" (dist "+utils.prettyStr(dist2, 6));
					}
				}
				i++;
			}
		}
	}

	//parses the list 'data' of data points in order to report average, min, and
	//max times
	private static void printDataCSV(Graph G, boolean includeMinMax) {
		System.out.print("numSites");
		for (NNStruct struct : structs) {
			for (char opType : initOpTypes()) {
				if (includeMinMax) {
					System.out.print(","+struct.acronym()+"avg"+opType);
					System.out.print(","+struct.acronym()+"min"+opType);
					System.out.print(","+struct.acronym()+"max"+opType);
				} else {
					System.out.print(","+struct.acronym()+"avg"+opType);
				}
			}
		}
		System.out.println();
		
		ArrayList<Integer> siteNums = initSiteNums(G);
		for (int siteNum : siteNums) {
			ArrayList<Stats> stats = new ArrayList<>();
			for (NNStruct struct : structs) {
				for (char opType : initOpTypes()) {
					stats.add(getStats(siteNum, struct, opType));
				}
			}
			System.out.print(siteNum);
			for (Stats st : stats) {
				if (includeMinMax) {
					System.out.print(","+st.avg+","+st.min+","+st.max);
				} else {
					System.out.print(","+st.avg);
				}
			}
			System.out.println();
		}
	}
	
	static private class Stats {
		double avg, min, max;
		Stats(double avg, double min, double max) {
			this.avg = avg; this.min = min; this.max = max;
		}
	}
	
	static private Stats getStats(int siteNum, NNStruct struct, char opType) {
		double total = 0;
		int count = 0;
		double max = -1;
		double min = Double.MAX_VALUE;
		for (DataPoint dp : data) {
			if (dp.siteNum == siteNum && dp.struct.acronym().equals(struct.acronym()) && dp.opType == opType) {
				total += dp.time;
				count += 1;
				max = Math.max(max, dp.time);
				min = Math.min(min, dp.time);
			}
		}
		return new Stats(total/count, min, max);
	}
	

}
	

	