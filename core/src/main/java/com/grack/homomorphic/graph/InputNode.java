package com.grack.homomorphic.graph;

public class InputNode extends Node {
	private String name;

	public InputNode(String name) {
		this.name = name;
	}

	public String name() {
		return name;
	}
	
	@Override
	public Node duplicate() {
		return new InputNode(name);
	}
}
