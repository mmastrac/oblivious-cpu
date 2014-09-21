package com.grack.homomorphic.graph;

public class OutputNode extends Node {
	private String name;

	public OutputNode(String name, Node from) {
		this.name = name;
		addInput(from);
	}
	
	public OutputNode(String name) {
		this.name = name;
	}

	public String name() {
		return name;
	}
	
	@Override
	public Node duplicate() {
		return new OutputNode(name);
	}
}
