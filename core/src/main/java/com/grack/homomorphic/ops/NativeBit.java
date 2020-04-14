package com.grack.homomorphic.ops;

/**
 * The native homomorphic system.
 */
public interface NativeBit {
	/**
	 * Performs an xor operation with another bit.
	 */
	NativeBit xor(NativeBit n);
	
	/**
	 * Performs an and operation with another bit.
	 */
	NativeBit and(NativeBit n);

	/**
	 * Inverts a bit.
	 */
	NativeBit not();
	
	/**
	 * Returns a one from the same factory that produced this bit.
	 */
	NativeBit one();
	
	/**
	 * Returns a zero from the same factory that produced this bit.
	 */
	NativeBit zero();
}
