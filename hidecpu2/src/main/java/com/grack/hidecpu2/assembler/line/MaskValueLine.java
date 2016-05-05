package com.grack.hidecpu2.assembler.line;

import com.grack.hidecpu2.assembler.Opcode;

public class MaskValueLine extends Line {
	private int mask;
	private int value;

	public MaskValueLine(String comment, String label, Opcode opcode, int mask, int value) {
		super(comment, label, opcode, null);
		this.mask = mask;
		this.value = value;
	}

	@Override
	public int[] assemble() {
		int op = 0;
		op |= opcode.ordinal() << 4;
		int val = 0;
		return new int[]{ val, op };
	}

	@Override
	protected String toStringInternal() {
		if (mask == 1) {
			if (value == 1)
				return "setc";
			return "clrc";
		}
		
		return "(mask unhandled)";
	}

	@Override
	public int size() {
		return 2;
	}
}
