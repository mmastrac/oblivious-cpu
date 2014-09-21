package com.grack.homomorphic.graph;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Graph {
	private ArrayList<InputNode> inputs = new ArrayList<>();
	private ArrayList<OutputNode> outputs = new ArrayList<>();

	public Graph() {
	}

	public void addInput(InputNode node) {
		inputs.add(node);
	}

	public void addOutput(OutputNode node) {
		outputs.add(node);
	}

	public void visitOut(NodeVisitor pre, NodeVisitor post) {
		for (InputNode node : inputs) {
			node.visitOut(pre, post);
		}
	}

	public void visitIn(NodeVisitor pre, NodeVisitor post) {
		for (OutputNode node : outputs) {
			node.visitIn(pre, post);
		}
	}

//	public Graph optimize() {
//		// phase 1: remove duplicate paths
//
//		Graph out = new Graph();
//		visitOut((node) -> {
//			
//			return true;
//		}, null);
//	}

	public void toC(Writer w) throws IOException {
		w.write("node_t input[" + inputs.size() + "]\n");
		w.write("node_t output[" + outputs.size() + "]\n");

		Map<Node, Integer> nodes = gatherNodes();
		w.write("node_t temp[" + nodes.size() + "]\n");

		w.write("\n");

		// Recursively write
		Set<Node> visited = new HashSet<>();
		visitIn((node) -> {
			return visited.add(node);
		}, (node) -> {
			try {
				toC(w, nodes, node);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			return true;
		});
	}

	private Map<Node, Integer> gatherNodes() {
		HashMap<Node, Integer> nodes = new HashMap<>();
		visitIn((node) -> {
			if (nodes.containsKey(node))
				return false;

			if (!(node instanceof InputNode) && !(node instanceof OutputNode))
				nodes.put(node, nodes.size());

			return true;
		}, null);

		return nodes;
	}

	private void toC(Writer w, Map<Node, Integer> nodes, Node node)
			throws IOException {
		if (node instanceof InputNode) {

		}

		if (node instanceof OutputNode) {
			w.write(getCName(nodes, node) + " = "
					+ getCName(nodes, node.input(0)) + "\n");
		}

		if (node instanceof AndNode) {
			w.write(getCName(nodes, node) + " = "
					+ getCName(nodes, node.input(0)) + " & "
					+ getCName(nodes, node.input(1)) + "\n");
		}

		if (node instanceof XorNode) {
			w.write(getCName(nodes, node) + " = "
					+ getCName(nodes, node.input(0)) + " & "
					+ getCName(nodes, node.input(1)) + "\n");
		}

		if (node instanceof NotNode) {
			w.write(getCName(nodes, node) + " = !"
					+ getCName(nodes, node.input(0)) + "\n");
		}
	}

	private String getCName(Map<Node, Integer> nodes, Node node) {
		if (node instanceof InputNode)
			return "input[" + inputs.indexOf(node) + "]";
		if (node instanceof OutputNode)
			return "output[" + outputs.indexOf(node) + "]";
		return "temp[" + nodes.get(node) + "]";
	}
}
