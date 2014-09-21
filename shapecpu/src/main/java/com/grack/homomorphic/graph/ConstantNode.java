package com.grack.homomorphic.graph;

public class ConstantNode extends InputNode {
	private int value;

	public ConstantNode(String name, int value) {
		super(name);
		this.value = value;
	}
	
	@Override
	public Node duplicate() {
		return new ConstantNode(name(), value);
	}
}
