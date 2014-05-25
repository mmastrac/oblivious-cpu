package com.grack.shapecpu;

/**
 * Helper tuple for addition w/carry.
 */
public class BitAndBit {
	private Bit bit1;
	private Bit bit2;
	
	public BitAndBit(Bit bit1, Bit bit2) {
		this.bit1 = bit1;
		this.bit2 = bit2;
	}
	
	public Bit getBit1() {
		return bit1;
	}
	
	public Bit getBit2() {
		return bit2;
	}
}
