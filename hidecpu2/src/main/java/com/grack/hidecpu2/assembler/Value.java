package com.grack.hidecpu2.assembler;

public class Value {
	private Object value;

	public Value(int i) {
		value = i;
	}

	public Value(String s) {
		value = s;
	}

	public Value(OpTarget t) {
		value = t;
	}

	public Object getValue() {
		return value;
	}
	
	public void setValue(Object value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		if (value instanceof OpTarget)
			return ((OpTarget) value).name().toLowerCase();
		return "" + value;
	}
}
