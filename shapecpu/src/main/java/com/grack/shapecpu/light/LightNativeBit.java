package com.grack.shapecpu.light;

import com.grack.shapecpu.NativeBit;

/**
 * Uses the bit 0 for the actual value of the native bit 
 */
public class LightNativeBit implements NativeBit {
	private int value;
	private LightNativeBitFactory factory;
	
	public LightNativeBit(LightNativeBitFactory factory, int value) {
		this.factory = factory;
		this.value = value;
	}

	@Override
	public NativeBit xor(NativeBit n) {
		factory.xor++;
		LightNativeBit other = (LightNativeBit)n;
		return new LightNativeBit(factory, scramble(value ^ other.value));
	}

	@Override
	public NativeBit not() {
		return xor(new LightNativeBit(factory, 1));
	}
	
	@Override
	public NativeBit and(NativeBit n) {
		factory.and++;
		LightNativeBit other = (LightNativeBit)n;
		return new LightNativeBit(factory, scramble(value & other.value));
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
