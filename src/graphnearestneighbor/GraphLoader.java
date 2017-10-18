
package graphnearestneighbor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class encapsulates all the dealing with graph text files
 * See the instructions file in graphs/README.txt
 * 
 * @author Nil
 */
class GraphLoader {
	//edit this variable as needed so that the path can reach the graphs/ folder
	private static final String GRAPHS_FOLDER = "graphs/";

	private static final String DIMACS_EXT = "tmp";
	private static final String CLEANED_EXT = "txt";
	private static final String INFO_EXT = "info";


	static String getFile(String graphName, String ext) {
		return GRAPHS_FOLDER+graphName+"/"+graphName+"."+ext;
	}
	
	static Graph load(String graphName) throws IOException {
		if (!fileExists(getFile(graphName, CLEANED_EXT))) {
			createCleanedFile(graphName);
		}
		if (!fileExists(getFile(graphName, INFO_EXT))) {
			createInfoFile(graphName);
		}
		return loadGraph(getFile(graphName, CLEANED_EXT));
	}
	
	private static boolean fileExists(String fileName) {
		File file = new File(fileName);
		return file.exists() && file.isFile();
	}

	private static void createCleanedFile(String graphName) throws IOException {
		if (!fileExists(getFile(graphName, DIMACS_EXT))) {
			throw new RuntimeException("graph not found at:\n"
					+ System.getProperty("user.dir") + "/" +
					getFile(graphName, DIMACS_EXT));
		}
		Graph G = loadGraph(getFile(graphName, DIMACS_EXT));
		G = Graph.biggestConnectedComponent(G);
		normalizeCoordinates(G);
		storeGraph(G, getFile(graphName, CLEANED_EXT));
	}

	//since we are dealing with actual earth latitudes and longitudes in a small
	//region, we get big numbers in a small range. This is bad for floating arithmetic.
	//so it helps to subtract the same value from every coordinate
	static void normalizeCoordinates(Graph graph) {
		double smallestX = Double.MAX_VALUE, smallestY = Double.MAX_VALUE;
		for (Node node : graph.nodes) {
			if (node.coords.x < smallestX) smallestX = node.coords.x;
			if (node.coords.y < smallestY) smallestY = node.coords.y;
		}
		for (Node node : graph.nodes) {
			node.coords.x -= smallestX;
			node.coords.y -= smallestY;
		}
	}
	
	private static Graph loadGraph(String fileName) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(fileName));

		String currLine = reader.readLine();
		int numNodes = Integer.parseInt(currLine);
		ArrayList<Integer> labels = new ArrayList<>(numNodes);
		ArrayList<Point> coords = new ArrayList<>(numNodes);
		
		for (int i = 0; i < numNodes; i++) {
			currLine = reader.readLine();
			String[] strNums = currLine.split("\\s");
			Integer id = Integer.parseInt(strNums[0]);
			double x = Double.parseDouble(strNums[1]);
			double y = Double.parseDouble(strNums[2]);
			labels.add(id);
			coords.add(new Point(x, y));
		}
		
		currLine = reader.readLine();
		int numEdges = Integer.parseInt(currLine);
		ArrayList<utils.Pair<Integer,Integer>> edgeList = new ArrayList<>(numEdges);

		for (int i = 0; i < numEdges; i++) {
			currLine = reader.readLine();
			String[] strNums = currLine.split("\\s");
			int id1 = Integer.parseInt(strNums[0]);
			int id2 = Integer.parseInt(strNums[1]);
			edgeList.add(new utils.Pair<>(id1, id2));
			if (fileExtension(fileName).equals(DIMACS_EXT)) {
				reader.readLine(); //skip line with edge properties
			}
		}
		return initGraph(labels, coords, edgeList);
	}
	
	static String fileExtension(String fileName) {
		return fileName.substring(fileName.lastIndexOf('.') + 1, fileName.length());
	}
	
	private static Graph initGraph(ArrayList<Integer> labels, 
			ArrayList<Point> coords, ArrayList<utils.Pair<Integer,Integer>> edgeList) {
		int n = labels.size();
		ArrayList<Node> nodes = new ArrayList<>(n);
		HashMap<Node, ArrayList<Edge>> adjList = new HashMap<>();
		HashMap<Integer,Node> label2Node = new HashMap();		
		for (int i = 0; i < n; i++) {
			Node node = new Node(labels.get(i), coords.get(i));
			nodes.add(node);
			label2Node.put(labels.get(i), node);
		}
		for (Node node : nodes) {
			adjList.put(node, new ArrayList<Edge>());
		}
		for (utils.Pair<Integer, Integer> pair : edgeList) {
			int firstLabel = pair.first;
			int secondLabel = pair.second;
			Node firstNode = label2Node.get(firstLabel);
			Node secondNode = label2Node.get(secondLabel);
			Edge edge = new Edge(firstNode, secondNode);
			adjList.get(firstNode).add(edge);
			adjList.get(secondNode).add(edge);
		}
		return new Graph(nodes, adjList);
	}
	
	private static void storeGraph(Graph G, String fileName) throws IOException {
		File outFile = new File(fileName);
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(outFile))) {
			writer.write(String.valueOf(G.numNodes()));
			writer.newLine();
			for (Node n : G.nodes) {
				writer.write(n.label + " " + n.coords.x + " " + n.coords.y);
				writer.newLine();
			}
			writer.write(String.valueOf(G.numEdges()));
			writer.newLine();
			for (Node u : G.nodes) {
				for (Edge e : G.adjList.get(u)) {
					Node v = e.getOther(u);
					if (u.label < v.label) {
						writer.write(u.label + " " + v.label);
						writer.newLine();
					}
				}
			}
		}
	}

	private static void createInfoFile(String graphName) throws IOException {
		Graph G = loadGraph(getFile(graphName, CLEANED_EXT));
		String fileName = getFile(graphName, INFO_EXT);
		File outFile = new File(fileName);
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(outFile))) {
			writer.write(graphName);
			writer.newLine();
			writer.write(String.valueOf(G.numNodes())+" nodes");
			writer.newLine();
			writer.write(String.valueOf(G.numEdges())+" edges");
			writer.newLine();
		}
	}



}
