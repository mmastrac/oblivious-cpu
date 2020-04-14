package com.grack.hidecpu2.assembler.line;

public class EmptyLine extends Line {
	public EmptyLine(String comment, String label) {
		super(comment, label, null, null);
	}

	@Override
	public int[] assemble() {
		return null;
	}

	@Override
	protected String toStringInternal() {
		return "";
	}
	
	@Override
	public int size() {
		return 0;
	}
}
