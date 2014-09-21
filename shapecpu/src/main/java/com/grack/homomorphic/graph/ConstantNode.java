package com.grack.homomorphic.graph;

public class ConstantNode extends InputNode {
	private int value;

	public ConstantNode(String name, int value) {
		super(name);
		this.value = value;
		
		if (value != 0 && value != 1) {
			throw new IllegalArgumentException("Value must be zero or one");
		}		
	}
	
	@Override
	public Node duplicate() {
		return new ConstantNode(name(), value);
	}
	
	public int value() {
		return value;
	}
}
