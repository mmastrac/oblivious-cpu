package com.grack.shapecpu;

public class Word {
	private Bit[] bits;
	
	public Word(Bit[] bits) {
		this.bits = bits;
	}
	
	public Word add(Bit b) {
		return null;
	}

	public Word add(Word n) {
		return null;
	}

	public Word and(Word n) {
		Bit[] bits = new Bit[size()];
		for (int i = 0; i < size(); i++) {
			bits[i] = bit(i).and(n.bit(i));
		}
		
		return new Word(bits);
	}

	public Bit bit(int n) {
		return bits[n];
	}

	public Word bits(int topBit, int bottomBit) {
		Bit[] bits = new Bit[size()];
		
		return null;
	}
	
	public Bit bitsEq(int topBit, int bottomBit, int value) {
		return null;
	}

	public Bit eq(int i) {
		return null;
	}

	public Word not() {
		Bit[] bits = new Bit[size()];
		for (int i = 0; i < size(); i++) {
			bits[i] = bit(i).not();
		}
		
		return new Word(bits);
	}

	public Word or(Word n) {
		Bit[] bits = new Bit[size()];
		for (int i = 0; i < size(); i++) {
			bits[i] = bit(i).or(n.bit(i));
		}
		
		return new Word(bits);
	}

	public Word setBit(int bit, Bit b) {
		Bit[] bits = new Bit[size()];
		for (int i = 0; i < size(); i++) {
			bits[i] = i == bit ? b : bit(i);
		}
		
		return new Word(bits);
	}

	public Word shl(int i) {
		return null;
	}

	public Word xor(Word n) {
		Bit[] bits = new Bit[size()];
		for (int i = 0; i < size(); i++) {
			bits[i] = bit(i).xor(n.bit(i));
		}
		
		return new Word(bits);
	}

	public int size() {
		return bits.length;
	}


}
