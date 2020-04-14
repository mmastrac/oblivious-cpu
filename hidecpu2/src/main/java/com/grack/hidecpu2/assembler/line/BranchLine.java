package com.grack.hidecpu2.assembler.line;

import com.grack.hidecpu2.assembler.BranchType;
import com.grack.hidecpu2.assembler.Opcode;
import com.grack.hidecpu2.assembler.Value;

public class BranchLine extends Line {
	private BranchType branchType;

	public BranchLine(String comment, String label, Opcode opcode, BranchType branchType, Value value) {
		super(comment, label, opcode, new Value[] { value });
		this.branchType = branchType;
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
		return "(branch unhandled)";
	}

	@Override
	public int size() {
		return 2;
	}

}
