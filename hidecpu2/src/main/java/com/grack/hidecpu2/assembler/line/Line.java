package com.grack.hidecpu2.assembler.line;

import com.grack.hidecpu2.assembler.Opcode;
import com.grack.hidecpu2.assembler.Value;

public abstract class Line {
	protected String label;
	protected Opcode opcode;
	protected Value[] values;
	private String comment;

	public Line(String comment, String label, Opcode opcode, Value[] value) {
		this.comment = comment;
		this.label = label;
		this.opcode = opcode;
		this.values = value;
	}

	public String getComment() {
		return comment;
	}
	
	public void setComment(String comment) {
		this.comment = comment;
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
	
	public Value[] getValues() {
		return values;
	}
	
	public void setValues(Value[] value) {
		this.values = value;
	}

	abstract public int[] assemble();

	@Override
	public String toString() {
		return toString(-1);
	}

	public String toString(int line) {
		String s = "";
		if (line != -1) {
			if (opcode != null)
				s += line;
		}
		
		String str = toStringInternal();
		if (getComment() != null) {
			if (!str.isEmpty())
				str += "\t";
			str += "#" + getComment();
		}
		
		if (label != null) {
			if (line != -1 && opcode != null && !str.isEmpty()) {
				s += "\t" + label + ":\n" + line + "\t";
			} else {
				if (line != -1)
					s += "\t";
				s += label + ":";
			}
		} else {
			if (line != -1)
				s += "\t";
		}
		
		return s + str;
	}

	abstract protected String toStringInternal();

	abstract public int size();
}
