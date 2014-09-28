package com.grack.hidecpu.assembler;

public class Value {
	private Object value;

	public Value(int i) {
		value = i;
	}

	public Value(String s) {
		value = s;
	}

	public Object getValue() {
		return value;
	}
	
	public void setValue(Object value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return "" + value;
	}
}
