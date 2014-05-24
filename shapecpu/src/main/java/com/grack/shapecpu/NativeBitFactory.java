package com.grack.shapecpu;

public interface NativeBitFactory {
	NativeBit encodeNativeBit(int bit);

	default Bit encodeBit(int bit) {
		return new Bit(encodeNativeBit(bit));
	}
	
	default Word encodeWord(int word) {
		return null;
	}
}
