package com.grack.homomorphic.ops;

/**
 * Creates the inter-tick {@link State} environment.
 */
public interface StateFactory {
	void allocateWordRegister(String name, int width);

	void allocateBitRegister(String name);

	void allocateWordArrayRegister(String name, int width, int[] contents);

	State createState();
}
