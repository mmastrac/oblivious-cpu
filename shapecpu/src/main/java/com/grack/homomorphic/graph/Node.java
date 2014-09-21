package com.grack.homomorphic.graph;

import java.util.ArrayList;

public abstract class Node {
	protected ArrayList<Node> in = new ArrayList<>();
	protected ArrayList<Node> out = new ArrayList<>();

	protected void addInput(Node node) {
		in.add(node);
		node.out.add(this);
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

	public Node input(int n) {
		return in.get(n);
	}
	
	/**
	 * Creates a duplicate of this node, but without any links.
	 */
	public abstract Node duplicate();
}
