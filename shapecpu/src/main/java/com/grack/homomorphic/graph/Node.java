package com.grack.homomorphic.graph;

import java.util.ArrayList;

public abstract class Node {
	protected ArrayList<Node> in = new ArrayList<>();
	protected ArrayList<Node> out = new ArrayList<>();
	
	protected void addInput(Node node) {
		in.add(node);
		node.out.add(this);
	}
}
