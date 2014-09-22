package com.grack.homomorphic.ops;

/**
 * Helper tuple for addition w/carry.
 */
public class WordAndBit {
	private Word word;
	private Bit bit;
	
	public WordAndBit(Word word, Bit bit) {
		this.word = word;
		this.bit = bit;
	}
	
	public Bit getBit() {
		return bit;
	}
	
	public Word getWord() {
		return word;
	}
}
