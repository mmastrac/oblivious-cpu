package com.grack.shapecpu;

public interface NativeBitFactory {
	NativeBit encodeNativeBit(int bit);

	default Bit encodeBit(int bit) {
		return new Bit(encodeNativeBit(bit));
	}
	
	default Word encodeWord(long word, int size) {
		Bit[] bits = new Bit[size];
		for (int i = 0; i < size; i++) {
			bits[i] = encodeBit((int) ((word & (1 << i)) >> i));
		}
		
		return new Word(bits);
	}
}
