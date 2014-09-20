package com.grack.homomorphic.ops;

/**
 * Inter-tick state.
 */
public interface State {
	public abstract Word getWordRegister(String name);
	
	public abstract Bit getBitRegister(String name);
	
	public abstract Word[] getWordArrayRegister(String name);
	
	public abstract void setBitRegister(String name, Bit value);
	
	public abstract void setWordRegister(String name, Word value);
	
	public abstract void setWordArrayRegister(String name, Word[] value);

	public abstract Bit one();

	public abstract Bit zero();
}
