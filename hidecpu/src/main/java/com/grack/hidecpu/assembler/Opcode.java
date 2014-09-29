package com.grack.hidecpu.assembler;

/**
 * Program opcodes.
 */
public enum Opcode {
	LOAD,
	STORE,
	ROR,
	ROL,
	NOT,
	
	ADD,
	SUB,
	XOR,
	AND,
	OR,
	
	CMP,
	CARRY,
	BRA,
	LOOP,
	JUMP,
	
	DATA
}
