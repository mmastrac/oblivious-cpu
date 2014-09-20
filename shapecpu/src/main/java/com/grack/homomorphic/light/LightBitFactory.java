package com.grack.homomorphic.light;

import java.util.Random;

import com.grack.homomorphic.ops.Bit;
import com.grack.homomorphic.ops.NativeBit;
import com.grack.homomorphic.ops.NativeBitFactory;
import com.grack.homomorphic.ops.Word;

/**
 * Implementation of homomorphic CPU, where the bits are encoded as the smallest
 * bit in a byte. No privacy is implemented, but this ensure that there is at
 * least some scrambling of the contents.
 */
public class LightBitFactory implements NativeBitFactory {
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
		return new LightBit(this, scramble(v));
	}

	public int extract(Bit bit) {
		LightBit nativeBit = (LightBit) bit.nativeBit();
		return extract(nativeBit);
	}

	public int extract(LightBit nativeBit) {
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
