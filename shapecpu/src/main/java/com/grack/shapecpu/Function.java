package com.grack.shapecpu;

/**
 * The native homomorphic system.
 */
public interface Function {
	Bit xor(Bit a, Bit b);
	
	Bit and(Bit a, Bit b);

	Bit encodeBit(int bit);
	
	default Word encodeWord(int word) {
		return null;
	}
}
