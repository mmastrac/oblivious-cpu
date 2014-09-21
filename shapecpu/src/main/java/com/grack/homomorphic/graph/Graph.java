package com.grack.homomorphic.graph;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;

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

	public int nodeCount() {
		HashSet<Node> nodes = new HashSet<>();
		visitIn((node) -> { return nodes.add(node); }, null);
		return nodes.size();
	}
	
	public void optimize() {
		int before = nodeCount();
		
		// phase 1: remove duplicate paths
		removeDuplicatePaths();
		
		int after = nodeCount();
		
		System.out.println(before + " -> " + after);
	}

	private void removeDuplicatePaths() {
		Set<Node> processed = new HashSet<>();
		for (InputNode inputNode : inputs) {
			removeDuplicatePaths(processed, inputNode);
		}
	}

	static int nodes;
	
	private void removeDuplicatePaths(Set<Node> processed, Node node) {
		if (!processed.add(node))
			return;
		
		// Let's just repeat this until we find no more dupes
		top: while (true) {
			Iterable<Node> candidates = node.outputs();
			for (Node candidate : candidates) {
				// Don't remove these
				if (candidate instanceof OutputNode)
					continue;
				
				for (Node other : candidates) {
					// Obviously don't compare against yourself
					if (candidate == other)
						continue;

					// Must be the same type
					if (candidate.getClass() != other.getClass())
						continue;

					if (candidate.sameInputsAs(other)) {
						// Found a dupe, so let's remove "other" from the tree
						// and replace references to it with candidate
						other.replaceWith(candidate);
						
						// Now we let the GC just reclaim it
						continue top;
					}
				}
			}
			
			// No dupes found
			break;
		}

		// Recurse
		for (Node output : node.outputs()) {
			removeDuplicatePaths(processed, output);
		}
	}

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
