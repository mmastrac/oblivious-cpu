package com.grack.shapecpu.assembler;

/**
 * Program opcodes.
 */
public enum Opcode {
	CMP(1),
	CMDa(1 + 16),
	BMI(2),
	La(3 + 16),
	J(4),
	BEQ(5),
	OR(6),
	ORa(6 + 16),
	AND(7),
	ANDa(7 + 16),
	XOR(8),
	XORa(8 + 16),
	SEC(9),
	CLC(10),
	ADD(11),
	ADDa(11 + 16),
	ROR(12),
	ROL(13),
	L(14),
	STa(15 + 16);
	
	private int value;

	private Opcode(int value) {
		this.value = value;
	}
	
	public int getValue() {
		return value;
	}
}
