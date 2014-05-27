package com.grack.shapecpu.light;

import java.util.Random;

import com.grack.shapecpu.Bit;
import com.grack.shapecpu.NativeBit;
import com.grack.shapecpu.NativeBitFactory;
import com.grack.shapecpu.Word;

public class LightNativeBitFactory implements NativeBitFactory {
	private final Random r = new Random();
	int xor, and;

	@Override
	public NativeBit encodeNativeBit(int bit) {
		if (bit != 0 && bit != 1)
			throw new IllegalArgumentException("bit must be zero or one (not "
					+ bit + ")");

		int v = 0;
		if (bit == 1)
			v = v | 1;
		return new LightNativeBit(this, scramble(v));
	}

	public int extract(Bit bit) {
		LightNativeBit nativeBit = (LightNativeBit) bit.nativeBit();
		return extract(nativeBit);
	}

	public int extract(LightNativeBit nativeBit) {
		return nativeBit.value() & 1;
	}

	public long extract(Word word) {
		long value = 0;

		for (int i = 0; i < word.size(); i++) {
			value |= ((long) extract(word.bit(i))) << i;
		}

		return value;
	}

	public int scramble(int value) {
		return (value + (r.nextInt(256) & ~1)) % 256 | 0xf000;
	}
	
	public int getAndCount() {
		return and;
	}
	
	public int getXorCount() {
		return xor;
	}
}
