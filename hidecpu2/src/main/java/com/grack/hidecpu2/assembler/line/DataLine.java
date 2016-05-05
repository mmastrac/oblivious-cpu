package com.grack.hidecpu2.assembler.line;

import com.google.common.base.Joiner;
import com.grack.hidecpu2.assembler.Opcode;
import com.grack.hidecpu2.assembler.Value;

public class DataLine extends Line {
	public DataLine(String comment, String label, Opcode opcode, Value[] data) {
		super(comment, label, opcode, data);
	}
	
	@Override
	public int[] assemble() {
		int[] output = new int[values.length];
		for (int i = 0; i < values.length; i++)
			output[i] = (Integer)values[i].getValue();
		return output;
	}

	@Override
	protected String toStringInternal() {
		return "data" + "\t" + Joiner.on(", ").join(values);
	}

	@Override
	public int size() {
		return values.length;
	}
}
