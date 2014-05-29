package com.grack.shapecpu.assembler;

public class Line {
	private String label;
	private Opcode opcode;
	private Value value;

	public Line(String label, Opcode opcode, Value value) {
		this.label = label;
		this.opcode = opcode;
		this.value = value;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Opcode getOpcode() {
		return opcode;
	}

	public void setOpcode(Opcode opcode) {
		this.opcode = opcode;
	}

	public Value getValue() {
		return value;
	}

	public void setValue(Value value) {
		this.value = value;
	}

	public boolean isEmpty() {
		return label == null && opcode == null;
	}

	public int assemble() {
		return opcode.getValue() << 8 | (int) (value == null ? 0 : value.getValue());
	}

	@Override
	public String toString() {
		// Empty line
		if (isEmpty())
			return "";

		return (label == null ? "" : label) + "\t" + opcode + "\t"
				+ (value == null ? "" : value);
	}

}
