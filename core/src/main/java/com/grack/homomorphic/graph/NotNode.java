package com.grack.homomorphic.graph;

public class NotNode extends Node {
	public NotNode() {
	}

	public NotNode(Node in1) {
		addInput(in1);
	}

	@Override
	public Node duplicate() {
		return new NotNode();
	}
}
