package com.grack.shapecpu.light;

import com.grack.shapecpu.NativeBit;
import com.grack.shapecpu.NativeBitFactory;

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
		LightNativeBit other = (LightNativeBit)n;
		return new LightNativeBit(factory, value ^ other.value);
	}

	@Override
	public NativeBit and(NativeBit n) {
		LightNativeBit other = (LightNativeBit)n;
		return new LightNativeBit(factory, value & other.value);
	}
	
	@Override
	public NativeBitFactory factory() {
		return factory;
	}
}
