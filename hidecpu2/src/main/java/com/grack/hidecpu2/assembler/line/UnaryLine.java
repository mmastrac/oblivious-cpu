package com.grack.hidecpu2.assembler.line;

import com.grack.hidecpu2.assembler.OpTarget;
import com.grack.hidecpu2.assembler.Opcode;
import com.grack.hidecpu2.assembler.UnaryOpcode;

public class UnaryLine extends Line {
	private OpTarget target;
	private UnaryOpcode subop;

	public UnaryLine(String comment, String label, Opcode opcode, OpTarget target, UnaryOpcode subop) {
		super(comment, label, opcode, null);
		this.target = target;
		this.subop = subop;
	}

	@Override
	public int[] assemble() {
		throw new RuntimeException("Unhandled");
	}

	@Override
	protected String toStringInternal() {
		return subop.name().toLowerCase() + "\t" + target.name().toLowerCase();
	}

	@Override
	public int size() {
		return 2;
	}
}
