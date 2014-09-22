package com.grack.homomorphic.graph;

import java.util.ArrayList;

public abstract class Node {
	protected ArrayList<Node> in = new ArrayList<>();
	protected ArrayList<Node> out = new ArrayList<>();

	protected void addInput(Node node) {
		in.add(node);
		node.out.add(this);
	}

	protected void removeOutput(Node node) {
		out.remove(node);
	}

	protected void swapInput(Node from, Node to) {
		for (int i = 0; i < in.size(); i++) {
			if (in.get(i) == from) {
				in.set(i, to);
				to.out.add(this);
			}
		}
	}

	public boolean sameInputsAs(Node other) {
		ArrayList<Node> otherIn = other.in;
		
		if (in.size() == 1 && otherIn.size() == 1) {
			return in.get(0) == otherIn.get(0);
		}

		if (in.size() == 2 && otherIn.size() == 2) {
			return (in.get(0) == otherIn.get(0) && in.get(1) == otherIn.get(1))
					|| (in.get(0) == otherIn.get(1) && in.get(1) == otherIn
							.get(0));
		}

		throw new IllegalStateException("Unable to compare > 2 right now");
	}

	/**
	 * Replace this node with another.
	 */
	public void replaceWith(Node other) {
		for (Node toRedirect : this.outputs()) {
			toRedirect.swapInput(this, other);
		}
		
		for (Node toRemoveFrom : this.inputs()) {
			toRemoveFrom.removeOutput(this);
		}
	}

	public void visitIn(NodeVisitor pre, NodeVisitor post) {
		boolean visitChildren = true;
		if (pre != null)
			visitChildren = pre.visit(this);

		if (visitChildren) {
			for (Node node : in) {
				node.visitIn(pre, post);
			}

			if (post != null)
				post.visit(this);
		}
	}

	public void visitOut(NodeVisitor pre, NodeVisitor post) {
		boolean visitChildren = true;
		if (pre != null)
			visitChildren = pre.visit(this);

		if (visitChildren) {
			for (Node node : out) {
				node.visitOut(pre, post);
			}

			if (post != null)
				post.visit(this);
		}
	}

	public Node output(int n) {
		return out.get(n);
	}

	public Iterable<Node> outputs() {
		return out;
	}

	public Node input(int n) {
		return in.get(n);
	}

	public Iterable<Node> inputs() {
		return in;
	}

	/**
	 * Creates a duplicate of this node, but without any links.
	 */
	public abstract Node duplicate();
}
