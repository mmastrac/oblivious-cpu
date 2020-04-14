package com.grack.hidecpu2.assembler.line;

import com.grack.hidecpu2.assembler.OpSource;
import com.grack.hidecpu2.assembler.OpTarget;
import com.grack.hidecpu2.assembler.Opcode;
import com.grack.hidecpu2.assembler.Value;

public class StandardLine extends Line {
	private OpTarget target;
	private OpSource source;

	public StandardLine(String comment, String label, Opcode opcode, OpTarget target, OpSource source, Value value) {
		super(comment, label, opcode, value == null ? null : new Value[] { value });
		this.target = target;
		this.source = source;
	}

	@Override
	public int[] assemble() {
		int op = 0;
		op |= opcode.ordinal() << 4;
		op |= (target == null) ? 0 : (target.ordinal() << 2);
		op |= (source == null) ? 0 : (source.ordinal() << 0);

		int val = 0;
		Object value = values[0].getValue();
		if (values != null) {
			if (value instanceof OpTarget)
				val = 255 - ((OpTarget)value).ordinal();
			else
				val = (int)value;
		}

		return new int[] { val, op };
	}

	@Override
	protected String toStringInternal() {
		String src = "<null>";
		if (source != null) {
			switch (source) {
			case CONSTANT:
				src = values[0].toString();
				break;
			case CONSTANT_LOAD:
				src = '[' + values[0].toString() + ']';
				break;
			case R0_RELATIVE_LOAD:
				src = "[r0 + " + values[0].toString() + ']';
				break;
			case R1_RELATIVE_LOAD:
				src = "[r1 + " + values[0].toString() + ']';
				break;
			}
		}

		String s;
		
		switch (opcode) {
		case LOAD:
			s = "mov\t" + target.name().toLowerCase() + ", " + src;
			break;
		case STORE:
			s = "mov\t" + src + ", " + target.name().toLowerCase();
			break;
		default:
			if (target == null)
				s = opcode.name().toLowerCase() + "\t" + src;
			else
				s = opcode.name().toLowerCase() + "\t" + target.name().toLowerCase() + ", " + src;
		}

		return s;
	}
	
	@Override
	public int size() {
		return 2;
	}
}
