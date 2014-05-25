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
		throw new RuntimeException("Not implemented");
	}

	public Word add(int n) {
		throw new RuntimeException("Not implemented");
	}

	public Word and(Word n) {
		int size = Math.min(size(), n.size());

		Bit[] bits = new Bit[size];
		for (int i = 0; i < size; i++) {
			bits[i] = bit(i).and(n.bit(i));
		}

		return new Word(bits);
	}

	public Bit bit(int n) {
		return bits[n];
	}

	public Word bits(int topBit, int bottomBit) {
		Bit[] bits = new Bit[size()];

		throw new RuntimeException("Not implemented");
	}

	public Bit bitsEq(int topBit, int bottomBit, int value) {
		throw new RuntimeException("Not implemented");
	}

	public Bit eq(int n) {
		Bit result = ((n & 1) == 1) ? bit(0) : bit(0).not();

		for (int i = 1; i < size(); i++) {
			if ((n & (1 << i)) != 0) {
				result = result.and(bit(i));
			} else {
				result = result.and(bit(i).not());
			}
		}

		return result;
	}

	public Word not() {
		Bit[] bits = new Bit[size()];
		for (int i = 0; i < size(); i++) {
			bits[i] = bit(i).not();
		}

		return new Word(bits);
	}

	public Word or(Word n) {
		int size = Math.max(size(), n.size());
		Bit[] bits = new Bit[size];
		for (int i = 0; i < size; i++) {
			if (i >= size()) {
				bits[i] = n.bit(i);
			} else if (i >= n.size()) {
				bits[i] = bit(i);
			} else {
				bits[i] = bit(i).or(n.bit(i));
			}
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
		int size = Math.max(size(), n.size());
		Bit[] bits = new Bit[size];
		for (int i = 0; i < size; i++) {
			if (i >= size()) {
				bits[i] = n.bit(i);
			} else if (i >= n.size()) {
				bits[i] = bit(i);
			} else {
				bits[i] = bit(i).xor(n.bit(i));
			}
		}

		return new Word(bits);
	}

	public int size() {
		return bits.length;
	}

}
