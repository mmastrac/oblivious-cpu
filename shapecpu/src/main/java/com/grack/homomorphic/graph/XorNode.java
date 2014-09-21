package com.grack.homomorphic.graph;

public class XorNode extends Node {
	public XorNode() {
	}

	public XorNode(Node in1, Node in2) {
		addInput(in1);
		addInput(in2);
	}
	
	@Override
	public Node duplicate() {
		return new XorNode();
	}
}
