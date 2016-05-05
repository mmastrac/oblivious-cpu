package com.grack.hidecpu2.assembler.line;

public class SegmentLine extends Line {
	private String segment;

	public SegmentLine(String comment, String segment) {
		super(comment, null, null, null);
		this.segment = segment;
	}

	public String getSegment() {
		return segment;
	}

	@Override
	public int[] assemble() {
		throw new IllegalStateException("This isn't valid for SegmentLine");
	}

	@Override
	protected String toStringInternal() {
		return "." + segment;
	}

	@Override
	public int size() {
		return 0;
	}
}
