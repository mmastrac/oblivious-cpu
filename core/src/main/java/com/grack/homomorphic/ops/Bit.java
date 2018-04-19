package com.grack.homomorphic.ops;

import com.google.common.base.Preconditions;

/**
 * One encrypted bit.
 */
public class Bit {
	private NativeBit nativeBit;

	public Bit(NativeBit fn) {
		Preconditions.checkNotNull(fn);
		this.nativeBit = fn;
	}

	public Bit and(Bit b) {
		Preconditions.checkNotNull(b);
		return new Bit(nativeBit.and(b.nativeBit));
	}

	public Word and(Word w) {
		Preconditions.checkNotNull(w);
		Bit[] bits = new Bit[w.size()];
		for (int i = 0; i < w.size(); i++)
			bits[i] = and(w.bit(i));

		return new Word(bits);
	}

	public Bit ifThen(Bit a, Bit b) {
		Preconditions.checkNotNull(a);
		Preconditions.checkNotNull(b);
		return this.and(a).or(this.not().and(b));
	}

	public Word ifThen(Word a, Word b) {
		Preconditions.checkNotNull(a);
		Preconditions.checkNotNull(b);
		return this.and(a).xor(this.not().and(b));
	}

	public Bit not() {
		return new Bit(nativeBit.not());
	}

	public Bit or(Bit b) {
		Preconditions.checkNotNull(b);
		return this.xor(b).xor(this.and(b));
	}

	public Bit xor(Bit b) {
		Preconditions.checkNotNull(b);
		return new Bit(nativeBit.xor(b.nativeBit));
	}
	
	public NativeBit nativeBit() {
		return nativeBit;
	}
	
	@Override
	public String toString() {
		return nativeBit.toString();
	}

	public BitAndBit halfAdd(Bit b) {
		Preconditions.checkNotNull(b);
		return new BitAndBit(this.xor(b), this.and(b));
	}
	
	public BitAndBit fullAdd(Bit b, Bit carry) {
		Preconditions.checkNotNull(b);
		Preconditions.checkNotNull(carry);
		
		BitAndBit ha1 = this.halfAdd(b);
		BitAndBit ha2 = ha1.getBit1().halfAdd(carry);
		
		return new BitAndBit(ha2.getBit1(), ha1.getBit2().or(ha2.getBit2()));
	}
	
	public Bit one() {
		return new Bit(nativeBit().one());
	}
	
	public Bit zero() {
		return new Bit(nativeBit().one());
	}
}
