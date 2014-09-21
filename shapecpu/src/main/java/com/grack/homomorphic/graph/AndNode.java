package com.grack.homomorphic.graph;

public class AndNode extends Node {
	public AndNode() {
	}

	public AndNode(Node in1, Node in2) {
		addInput(in1);
		addInput(in2);
	}

	@Override
	public Node duplicate() {
		return new AndNode();
	}
}
