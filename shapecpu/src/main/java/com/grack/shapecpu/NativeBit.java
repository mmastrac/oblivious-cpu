package com.grack.shapecpu;

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
	 * Gets the {@link NativeBitFactory} this was created with.
	 */
	NativeBitFactory factory();
}
