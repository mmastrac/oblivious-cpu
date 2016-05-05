package com.grack.hidecpu2.assembler.line;

import com.grack.hidecpu2.assembler.BranchType;
import com.grack.hidecpu2.assembler.OpSource;
import com.grack.hidecpu2.assembler.OpTarget;
import com.grack.hidecpu2.assembler.Opcode;
import com.grack.hidecpu2.assembler.UnaryOpcode;
import com.grack.hidecpu2.assembler.Value;

public class LineBuilder {
	private String label;
	private Opcode opcode;
	private String comment;

	public LineBuilder() {
	}

	public LineBuilder withLabel(String label) {
		this.label = label;
		return this;
	}

	public LineBuilder withOpcode(Opcode opcode) {
		this.opcode = opcode;
		return this;
	}

	public LineBuilder withComment(String comment) {
		this.comment = comment;
		return this;
	}
	
	public EmptyLine createEmpty() {
		return new EmptyLine(comment, label);
	}

	public SegmentLine createSegment(String segment) {
		if (label != null)
			throw new IllegalArgumentException();
		if (opcode != null)
			throw new IllegalArgumentException();
		return new SegmentLine(comment, segment);
	}
	
	public DataLine createData(Value[] data) {
		return new DataLine(comment, label, opcode, data);
	}

	public StandardLine createStandard(OpTarget target, OpSource source, Value value) {
		return new StandardLine(comment, label, opcode, target, source, value);
	}

	public UnaryLine createUnary(OpTarget target, UnaryOpcode subop) {
		return new UnaryLine(comment, label, opcode, target, subop);
	}

	public MaskValueLine createMaskValue(int mask, int value) {
		return new MaskValueLine(comment, label, opcode, mask, value);
	}

	public BranchLine createBranch(BranchType branchType, Value value) {
		return new BranchLine(comment, label, opcode, branchType, value);
	}
}
