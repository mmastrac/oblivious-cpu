package com.grack.homomorphic.ops;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ObjectArrays;

public class Word {
	private Bit[] bits;

	public Word(Bit[] bits) {
		Preconditions.checkNotNull(bits);
		for (Bit bit : bits) {
			Preconditions.checkNotNull(bit);
		}
		this.bits = bits;
	}

	/**
	 * Optimization for adding a bit to a word.
	 */
	public Word add(Bit b) {
		Preconditions.checkNotNull(b);

		int size = Math.max(size(), 1);
		Bit[] bits = new Bit[size];

		Bit carry = null;
		for (int i = 0; i < size; i++) {
			BitAndBit result;

			if (i == 0) {
				result = bit(i).halfAdd(b);
			} else {
				result = bit(i).halfAdd(carry);
			}

			carry = result.getBit2();
			bits[i] = result.getBit1();
		}

		return new Word(bits);
	}

	public WordAndBit addWithCarry(Word n, Bit carry) {
		Preconditions.checkNotNull(n);
		Preconditions.checkNotNull(carry);

		return addWithCarry(this.bits, n.bits, carry);
	}

	/**
	 * Add, silently ignore carry.
	 */
	public Word add(Word n) {
		return addWithCarry(n).getWord();
	}

	public WordAndBit addWithCarry(Word n) {
		Preconditions.checkNotNull(n);
		return addWithCarry(this.bits, n.bits, null);
	}
	
	/**
	 * Internal method that supports nullable holes in {@link Bit} arrays. Input
	 * carry may also be null.
	 */
	private WordAndBit addWithCarry(Bit[] w1, Bit[] w2, Bit carry) {
		int size = Math.max(w1.length, w2.length);
		Bit[] bits = new Bit[size];

		for (int i = 0; i < size; i++) {
			BitAndBit result;

			Bit b1 = i >= w1.length ? null : w1[i];
			Bit b2 = i >= w2.length ? null : w2[i];
			
			result = countBits(b1, b2, carry);

			carry = result.getBit2();
			bits[i] = result.getBit1();
		}

		return new WordAndBit(new Word(bits), carry);		
	}
	
	private BitAndBit countBits(Bit b1, Bit b2, Bit b3) {
		List<Bit> bits = new ArrayList<>(3);
		if (b1 != null)
			bits.add(b1);
		if (b2 != null)
			bits.add(b2);
		if (b3 != null)
			bits.add(b3);
		
		switch (bits.size()) {
		case 0:
			return new BitAndBit(bits.get(0).zero(), null);
		case 1:
			return new BitAndBit(bits.get(0), null);
		case 2:
			return bits.get(0).halfAdd(bits.get(1));
		case 3:
			return bits.get(0).fullAdd(bits.get(1), bits.get(2));
		}
		
		throw new IllegalStateException("Unexpected");
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

	public Word setBits(int topBit, int bottomBit, Word with) {
		Bit[] bits = new Bit[size()];
		for (int i = 0; i < size(); i++) {
			if (i <= topBit && i >= bottomBit)
				bits[i] = with.bit(i - bottomBit);
			else
				bits[i] = bit(i);
		}

		return new Word(bits);
	}

	/**
	 * Shifts a word left, filling in the bottom bits with the fill param.
	 */
	public Word shl(int n, Bit fill) {
		Bit[] bits = new Bit[size() + n];
		System.arraycopy(this.bits, 0, bits, n, size());
		for (int i = 0; i < n; i++)
			bits[i] = fill;
		return new Word(bits);
	}

	/**
	 * Shifts a word right, reducing the size of the word and lopping off bits.
	 */
	public Word shr(int n) {
		Bit[] bits = new Bit[size() - n];
		System.arraycopy(this.bits, n, bits, 0, size() - n);
		return new Word(bits);
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

	/**
	 * Concatenates two words. The argument becomes the higher bits.
	 */
	public Word concat(Word other) {
		Bit[] bits = ObjectArrays.concat(this.bits, other.bits, Bit.class);
		return new Word(bits);
	}

	/**
	 * Concatenates a word and a bit. The argument becomes the higher bit.
	 */
	public Word concat(Bit other) {
		Bit[] bits = ObjectArrays.concat(this.bits, new Bit[] { other }, Bit.class);
		return new Word(bits);
	}
	
	public int size() {
		return bits.length;
	}

	public Word reverse() {
		Bit[] bits = new Bit[size()];
		for (int i = 0; i < bits.length; i++) {
			bits[i] = bit(bits.length - i - 1);
		}

		return new Word(bits);
	}

	/**
	 * Decodes the bits in this word into an array of words using
	 * {@link Bit#ifThen(Word, Word)} calls.
	 */
	public Word decode(Word... args) {
		int n = 1 << size();
		if (n != args.length)
			throw new IllegalArgumentException("Expected " + n + " argument(s) for decode");
		
		return decode(args, 0, size() - 1);
	}
	
	private Word decode(Word[] args, int start, int bit) {
		if (bit == 0) {
			return bit(0).ifThen(args[start + 1], args[start]);
		}
		
		return bit(bit).ifThen(decode(args, start + (1 << bit), bit - 1),
				decode(args, start, bit - 1));
	}

	/**
	 * Decodes the bits in this word into an array of words using
	 * {@link Bit#ifThen(Bit, Bit)} calls.
	 */
	public Bit decode(Bit... args) {
		int n = 1 << size();
		if (n != args.length)
			throw new IllegalArgumentException("Expected " + n + " argument(s) for decode");
		
		return decode(args, 0, size() - 1);
	}
	
	private Bit decode(Bit[] args, int start, int bit) {
		if (bit == 0) {
			return bit(0).ifThen(args[start + 1], args[start]);
		}
		
		return bit(bit).ifThen(decode(args, start + (1 << bit), bit - 1),
				decode(args, start, bit - 1));
	}
	
	/**
	 * Generates a Dadda multiplier (https://en.wikipedia.org/wiki/Dadda_multiplier) for
	 * this Word and the multiplier.
	 */
	public Word multiplyDadda(Word multiplier) {
		return multiplyDadda(multiplier, false);
	}
	
	/**
	 * Generates a Dadda multiplier (https://en.wikipedia.org/wiki/Dadda_multiplier) for
	 * this Word and the multiplier, optionally signed.
	 */
	public Word multiplyDadda(Word multiplier, boolean signed) {
		// Create our weight arrays
		int outputSize = this.bits.length + multiplier.bits.length - 1;
		List<List<Bit>> weights = new ArrayList<>(outputSize);
		for (int i = 0; i < outputSize; i++) {
			weights.add(new ArrayList<>());
		}
		
		// TODO: This is probably not too tough
		if (signed && this.bits.length != multiplier.bits.length) {
			throw new IllegalArgumentException("Only nxn multiplication is supported when signed");
		}
		
		// Populate with ANDs of each bit in each word (n^2)
		for (int i = 0; i < this.bits.length; i++) {
			boolean is = i == this.bits.length - 1;
			for (int j = 0; j < multiplier.bits.length; j++) {
				boolean js = j == multiplier.bits.length - 1;
				int weight = i + j;
				Bit partial = this.bits[i].and(multiplier.bits[j]);
				if (signed && is ^ js) {
					partial = partial.not();
				}
				weights.get(weight).add(partial);
			}
		}
		
		// Signed multiplication via subtle manipulation of bits
		// https://www.dsprelated.com/showarticle/555.php
		if (signed) {
			weights.get(this.bits.length).add(this.bits[0].one());
		}
		
		daddaReduce(multiplier, weights);
		
		// Build our output row from the remaining weights
		Bit[] bits1 = new Bit[weights.size()];
		Bit[] bits2 = new Bit[weights.size()];
		for (int i = 0; i < weights.size(); i++) {
			List<Bit> w = weights.get(i);
			bits1[i] = w.get(0);
			bits2[i] = w.size() > 1 ? w.get(1) : null;
		}
		
		// Invert the carry bit in the signed case
		WordAndBit added = addWithCarry(bits1, bits2, null);
		return added.getWord().concat(signed ? added.getBit().not() : added.getBit());
	}

	/**
	 * Generates a Dadda multiplier (https://en.wikipedia.org/wiki/Dadda_multiplier) for
	 * this Word and the multiplier, optionally signed.
	 */
	public Word multiplyDadda(Word multiplier, Bit signed) {
		// Create our weight arrays
		int outputSize = this.bits.length + multiplier.bits.length - 1;
		List<List<Bit>> weights = new ArrayList<>(outputSize);
		for (int i = 0; i < outputSize; i++) {
			weights.add(new ArrayList<>());
		}
		
		// TODO: This is probably not too tough
		if (this.bits.length != multiplier.bits.length) {
			throw new IllegalArgumentException("Only nxn multiplication is supported when signed");
		}
		
		// Populate with ANDs of each bit in each word (n^2)
		for (int i = 0; i < this.bits.length; i++) {
			boolean is = i == this.bits.length - 1;
			for (int j = 0; j < multiplier.bits.length; j++) {
				boolean js = j == multiplier.bits.length - 1;
				int weight = i + j;
				Bit partial = this.bits[i].and(multiplier.bits[j]);
				weights.get(weight).add(is ^ js ? signed.ifThen(partial.not(), partial) : partial);
			}
		}
		
		// Signed multiplication via subtle manipulation of bits
		// https://www.dsprelated.com/showarticle/555.php
		weights.get(this.bits.length).add(signed);
		
		daddaReduce(multiplier, weights);
		
		// Build our output row from the remaining weights
		Bit[] bits1 = new Bit[weights.size()];
		Bit[] bits2 = new Bit[weights.size()];
		for (int i = 0; i < weights.size(); i++) {
			List<Bit> w = weights.get(i);
			bits1[i] = w.get(0);
			bits2[i] = w.size() > 1 ? w.get(1) : null;
		}
		
		// Invert the carry bit in the signed case
		WordAndBit added = addWithCarry(bits1, bits2, null);
		return added.getWord().concat(signed.ifThen(added.getBit().not(), added.getBit()));
	}

	private void daddaReduce(Word multiplier, List<List<Bit>> weights) {
		// Determine the initial stage based on the max column height
		int max = Math.max(this.bits.length, multiplier.bits.length);
		int stage = 1;
		while (dadda(stage + 1) < max) {
			stage++;
		}

		// Now reduce according to the rules
		for (; stage >= 1; stage--) {
			max = dadda(stage);
			for (int i = 0; i < weights.size(); i++) {
				List<Bit> column = weights.get(i);
				int columnSize = column.size();
				while (columnSize > max) {
					Bit a = column.remove(0);
					Bit b = column.remove(0);
					BitAndBit res;
					if (columnSize == max + 1) {
						columnSize--;
						res = a.halfAdd(b);
					} else {
						columnSize -= 2;
						Bit c = column.remove(0);
						res = a.fullAdd(b, c);
					}
					column.add(res.getBit1());
					weights.get(i + 1).add(res.getBit2());
				}
			}
		}
	}
	
	/**
	 * See https://en.wikipedia.org/wiki/Dadda_multiplier
	 */
	private static int dadda(int n) {
		if (n == 1)
			return 2;
		
		return (int)(3.0 * dadda(n - 1) / 2.0);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < bits.length; i++) {
			builder.append(bit(bits.length - i - 1));
		}
		return builder.toString();
	}
}
