package com.grack.shapecpu;

import com.google.common.base.Preconditions;

public class Word {
	private Bit[] bits;

	public Word(Bit[] bits) {
		this.bits = bits;
	}

	public Word add(Bit b) {
		Preconditions.checkNotNull(b);

		return add(new Word(new Bit[] { b }));
	}

	public WordAndBit addWithCarry(Word n, Bit carry) {
		Preconditions.checkNotNull(n);
		Preconditions.checkNotNull(carry);
		
		int size = Math.max(size(), n.size());
		Bit[] bits = new Bit[size];

		for (int i = 0; i < size; i++) {
			BitAndBit result;

			if (i >= size())
				result = n.bit(i).halfAdd(carry);
			else if (i >= n.size())
				result = bit(i).halfAdd(carry);
			else
				result = bit(i).fullAdd(n.bit(i), carry);

			carry = result.getBit2();
			bits[i] = result.getBit1();
		}
		
		return new WordAndBit(new Word(bits), carry);
	}

	public Word add(Word n) {
		Preconditions.checkNotNull(n);
		
		int size = Math.max(size(), n.size());
		Bit[] bits = new Bit[size];

		Bit carry = null;
		for (int i = 0; i < size; i++) {
			BitAndBit result;

			if (carry == null) {
				result = bit(i).halfAdd(n.bit(i));
			} else {
				if (i >= size())
					result = n.bit(i).halfAdd(carry);
				else if (i >= n.size())
					result = bit(i).halfAdd(carry);
				else
					result = bit(i).fullAdd(n.bit(i), carry);
			}

			carry = result.getBit2();
			bits[i] = result.getBit1();
		}
		
		return new Word(bits);
	}

	public Word and(Word n) {
		Preconditions.checkNotNull(n);
		
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

	/**
	 * Slices off a segment of this word (inclusive).
	 */
	public Word bits(int topBit, int bottomBit) {
		Bit[] bits = new Bit[topBit - bottomBit + 1];

		for (int i = bottomBit; i <= topBit; i++) {
			bits[i - bottomBit] = bit(i);
		}
		
		return new Word(bits);
	}

	/**
	 * Tests that a segment of bits (inclusive) is equal to the given constant.
	 */
	public Bit bitsEq(int topBit, int bottomBit, int n) {
		Bit result = ((n & 1) != 0) ? bit(bottomBit) : bit(bottomBit).not();

		for (int i = bottomBit + 1; i <= topBit; i++) {
			if ((n & (1 << (i - bottomBit))) != 0) {
				result = result.and(bit(i));
			} else {
				result = result.and(bit(i).not());
			}
		}
		
		return result;
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
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < bits.length; i++) {
			builder.append(bit(bits.length - i - 1));
		}		
		return builder.toString();
	}

	public Word reverse() {
		Bit[] bits = new Bit[size()];
		for (int i = 0; i < bits.length; i++) {
			bits[i] = bit(bits.length - i - 1);
		}
		
		return new Word(bits);
	}

}
