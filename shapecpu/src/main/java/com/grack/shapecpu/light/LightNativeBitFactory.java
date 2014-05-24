package com.grack.shapecpu.light;

import java.util.Random;

import com.grack.shapecpu.NativeBit;
import com.grack.shapecpu.NativeBitFactory;

public class LightNativeBitFactory implements NativeBitFactory {
	private final Random r = new Random();
	
	@Override
	public NativeBit encodeNativeBit(int bit) {
		if (bit != 0 && bit != 1)
			throw new IllegalArgumentException("bit must be zero or one");
		
		int v = r.nextInt(256) & 254;
		if (bit == 1)
			v = v | 1;
		return new LightNativeBit(this, v);
	}
}
