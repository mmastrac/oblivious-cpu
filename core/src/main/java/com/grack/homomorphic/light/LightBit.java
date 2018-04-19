package com.grack.homomorphic.light;

import com.grack.homomorphic.ops.NativeBit;

/**
 * Uses the bit 0 for the actual value of the native bit 
 */
public class LightBit implements NativeBit {
	private int value;
	private LightBitFactory factory;
	
	public LightBit(LightBitFactory factory, int value) {
		this.factory = factory;
		this.value = value;
	}

	@Override
	public NativeBit xor(NativeBit n) {
		factory.xor++;
		LightBit other = (LightBit)n;
		return new LightBit(factory, scramble(value ^ other.value));
	}

	@Override
	public NativeBit not() {
		return xor(new LightBit(factory, 1));
	}
	
	@Override
	public NativeBit and(NativeBit n) {
		factory.and++;
		LightBit other = (LightBit)n;
		return new LightBit(factory, scramble(value & other.value));
	}
	
	@Override
	public NativeBit one() {
		return factory.encodeNativeBit(1);
	}
	
	@Override
	public NativeBit zero() {
		return factory.encodeNativeBit(0);
	}
	
	private int scramble(int value) {
		return factory.scramble(value);
	}

	public int value() {
		return value;
	}
	
	@Override
	public String toString() {
		return Integer.toString(factory.extract(this));
	}
}
