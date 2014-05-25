package com.grack.shapecpu;

/**
 * One encrypted bit.
 */
public class Bit {
	private NativeBit nativeBit;

	public Bit(NativeBit fn) {
		this.nativeBit = fn;
	}

	public Bit and(Bit b) {
		return new Bit(nativeBit.and(b.nativeBit));
	}

	public Word and(Word w) {
		Bit[] bits = new Bit[w.size()];
		for (int i = 0; i < w.size(); i++)
			bits[i] = and(w.bit(i));

		return new Word(bits);
	}

	public Bit ifThen(Bit a, Bit b) {
		return this.and(a).or(this.not().and(b));
	}

	public Word ifThen(Word a, Word b) {
		return this.and(a).or(this.not().and(b));
	}

	public Bit not() {
		return this.xor(nativeBit.factory().encodeBit(1));
	}

	public Bit or(Bit b) {
		return this.xor(b).xor(this.and(b));
	}

	public Bit xor(Bit b) {
		return new Bit(nativeBit.xor(b.nativeBit));
	}
	
	public NativeBit nativeBit() {
		return nativeBit;
	}
	
	@Override
	public String toString() {
		return nativeBit.toString();
	}
}
