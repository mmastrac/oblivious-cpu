package com.grack.homomorphic.graph;

public class AndNode extends Node {
	public AndNode(Node in1, Node in2) {
		addInput(in1);
		addInput(in2);
	}
}
