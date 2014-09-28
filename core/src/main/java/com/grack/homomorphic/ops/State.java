package com.grack.homomorphic.ops;

/**
 * Inter-tick state.
 */
public interface State {
	Word getWordRegister(String name);
	
	Bit getBitRegister(String name);
	
	Word[] getWordArrayRegister(String name);
	
	void setBitRegister(String name, Bit value);
	
	void setWordRegister(String name, Word value);
	
	void setWordArrayRegister(String name, Word[] value);

	Bit one();

	Bit zero();
	
	Word negativeOne(int width);

	void debug(Object... things);
}
