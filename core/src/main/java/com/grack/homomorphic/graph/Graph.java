package com.grack.homomorphic.graph;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents a graph of dataflow in a HME system.
 */
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
		Set<Node> nodesIn = new HashSet<>();
		visitIn((node) -> {
			return nodesIn.add(node);
		}, null);
		Set<Node> nodesOut = new HashSet<>();
		visitOut((node) -> {
			return nodesOut.add(node);
		}, null);

		nodesIn.addAll(nodesOut);
		return nodesIn.size();
	}

	public void optimize() {
		int before = nodeCount();

		// phase 1: remove nodes with no output
		removeDeadEndNodes();

		// phase 2: fold constants
		foldConstants();

		// phase 3: remove duplicate paths
		removeDuplicatePaths();

		Set<Node> visited = new HashSet<>();
		visitIn((node) -> {
			if (!visited.add(node))
				return false;

			if (node instanceof XorNode) {
				if (node.input(0) instanceof NotNode
						&& node.input(1) instanceof NotNode) {
					System.out.println(node);
				}
			}

			if (node instanceof AndNode || node instanceof XorNode) {
				if (node.input(0) == node.input(1)) {
					System.out.println(node);
				}
			}

			if (node instanceof NotNode) {
				if (node.input(0) instanceof NotNode) {
					System.out.println(node);
				}
			}
			return true;
		}, null);

		int after = nodeCount();

		System.out.println(before + " -> " + after);
	}

	private void removeDeadEndNodes() {
		Set<Node> reachable = new HashSet<>();
		visitIn((node) -> {
			return reachable.add(node);
		}, null);

		Set<Node> visited = new HashSet<>();
		Set<Node> toRemove = new LinkedHashSet<>();
		visitOut((node) -> {
			// Already been here
				if (!visited.add(node)) {
					return false;
				}

				if (reachable.contains(node)) {
					return true;
				}

				toRemove.add(node);
				return true;
			}, null);

		// Do this repeatedly -- it's fast enough and we don't have to figure
		// out the safe order to unwind them
		while (toRemove.size() > 0) {
			Iterator<Node> it = toRemove.iterator();
			while (it.hasNext()) {
				Node n = it.next();
				if (n.outputCount() == 0) {
					n.remove();
					it.remove();
				}
			}
		}
	}

	private void foldConstants() {
		Set<ConstantNode> inputsToRemove = new HashSet<>();

		top: while (true) {
			for (InputNode inputNode : inputs) {
				if (inputNode instanceof ConstantNode) {
					ConstantNode constant = (ConstantNode) inputNode;
					inputsToRemove.add(constant);
					int value = constant.value();

					for (Node output : inputNode.outputs()) {
						Node otherInput;
						if (output.input(0) == inputNode) {
							otherInput = output.input(1);
						} else {
							otherInput = output.input(0);
						}

						if (output instanceof XorNode) {
							if (value == 0) {
								// XOR 0 means no-op
								output.replaceWith(otherInput);
							} else {
								// XOR 1 means not
								output.replaceWith(new NotNode(otherInput));
							}
						} else if (output instanceof AndNode) {
							if (value == 0) {
								// AND 0 means zero (we will re-attach the zero
								// higher up the tree)
								output.replaceWith(constant);
							} else {
								// AND 1 means no-op
								output.replaceWith(otherInput);
							}
						} else {
							throw new IllegalStateException(
									"Unable to fold constant into "
											+ output.getClass());
						}

						continue top;
					}
				}
			}

			// No modifications
			break;
		}

		inputs.removeAll(inputsToRemove);
	}

	private void removeDuplicatePaths() {
		Set<Node> processed = new HashSet<>();
		for (InputNode inputNode : inputs) {
			removeDuplicatePaths(processed, inputNode);
		}
	}

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

	public void toGraphviz(Writer w) throws IOException {
		w.write("digraph G {\n");

		Map<Node, Integer> nodes = gatherNodes();

		// Recursively write
		w.write("subgraph input {\n");
		w.write("  rank = source;\n");
		toGraphviz(w, nodes, 0);
		w.write("}\n");
		toGraphviz(w, nodes, 1);
		w.write("subgraph output {\n");
		w.write("  rank = sink;\n");
		toGraphviz(w, nodes, 2);
		w.write("}\n");

		w.write("}\n");
	}

	private void toGraphviz(Writer w, Map<Node, Integer> nodes, int phase) {
		Set<Node> visited = new HashSet<>();
		visitIn((node) -> {
			return visited.add(node);
		}, (node) -> {
			try {
				toGraphviz(w, nodes, node, phase);
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

	private void toGraphviz(Writer w, Map<Node, Integer> nodes, Node node,
			int phase) throws IOException {
		if (phase == 0) {
			if (node instanceof InputNode) {
				w.write("  ");
				w.write(getGraphvizName(nodes, node)
						+ " [shape=rectangle label="
						+ ((InputNode) node).name() + "];\n");
			}
		}

		if (phase == 1) {
			if (node instanceof OutputNode) {
				w.write("  ");
				w.write(getGraphvizName(nodes, node.input(0)) + " -> "
						+ getGraphvizName(nodes, node) + ";\n");
			}
			if (node instanceof AndNode) {
				w.write("  ");
				w.write(getGraphvizName(nodes, node.input(0)) + " -> "
						+ getGraphvizName(nodes, node) + ";\n");
				w.write("  ");
				w.write(getGraphvizName(nodes, node.input(1)) + " -> "
						+ getGraphvizName(nodes, node) + ";\n");
				w.write("  ");
				w.write(getGraphvizName(nodes, node)
						+ " [shape=parallelogram label=and]\n");
			}

			if (node instanceof XorNode) {
				w.write("  ");
				w.write(getGraphvizName(nodes, node.input(0)) + " -> "
						+ getGraphvizName(nodes, node) + ";\n");
				w.write("  ");
				w.write(getGraphvizName(nodes, node.input(1)) + " -> "
						+ getGraphvizName(nodes, node) + ";\n");
				w.write("  ");
				w.write(getGraphvizName(nodes, node)
						+ " [shape=ellipse label=xor]\n");
			}

			if (node instanceof NotNode) {
				w.write("  ");
				w.write(getGraphvizName(nodes, node.input(0)) + " -> "
						+ getGraphvizName(nodes, node) + " [shape=triangle label=not]\n");
			}
		}

		if (phase == 2) {
			if (node instanceof OutputNode) {
				w.write("  ");
				w.write(getGraphvizName(nodes, node) + " [label="
						+ ((OutputNode) node).name() + "];\n");
			}
		}
	}

	private void toC(Writer w, Map<Node, Integer> nodes, Node node)
			throws IOException {
		if (node instanceof InputNode) {

		}

		if (node instanceof OutputNode) {
			w.write(getCName(nodes, node) + " = "
					+ getCName(nodes, node.input(0)) + ";\n");
		}

		if (node instanceof AndNode) {
			w.write(getCName(nodes, node) + " = "
					+ getCName(nodes, node.input(0)) + " & "
					+ getCName(nodes, node.input(1)) + "\n");
		}

		if (node instanceof XorNode) {
			w.write(getCName(nodes, node) + " = "
					+ getCName(nodes, node.input(0)) + " ^ "
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

	private String getGraphvizName(Map<Node, Integer> nodes, Node node) {
		if (node instanceof InputNode)
			return "in_" + inputs.indexOf(node) + "";
		if (node instanceof OutputNode)
			return "out_" + outputs.indexOf(node) + "";
		return "temp_" + nodes.get(node);
	}
}
