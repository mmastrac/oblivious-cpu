package com.grack.homomorphic.graph;

public class XorNode extends Node {
	public XorNode(Node in1, Node in2) {
		addInput(in1);
		addInput(in2);
	}
}
