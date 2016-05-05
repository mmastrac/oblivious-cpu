package com.grack.hidecpu2.assembler;

/**
 * Program opcodes.
 */
public enum Opcode {
	LOAD,
	STORE,
	SWAP,
	
	ADD,
	SUB,
	XOR,
	AND,
	OR,

	/**
	 * See {@link UnaryOpcode}.
	 */
	UNARY,
	
	CMP,
	SETFLAGS,
	BRA,
	LOOP,
	JUMP,
}
