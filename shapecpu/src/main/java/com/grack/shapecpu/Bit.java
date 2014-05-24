package com.grack.shapecpu;

public class Bit {
	private Function fn;

	public Bit(Function fn) {
		this.fn = fn;
	}
	
	public Bit and(Bit b) {
		return fn.and(this, b);
	}

	public Bit or(Bit b) {
		return null;
	}
	
	public Word and(Word w) {
		return null;
	}
	
	public Bit xor(Bit b) {
		return fn.xor(this, b);
	}
	
	public Bit not() {
		return null;
	}

	public Bit ifThen(Bit a, Bit b) {
		return this.and(a).or(this.not().and(b));
	}
	
	public Word ifThen(Word a, Word b) {
		return this.and(a).or(this.not().and(b));
	}
}
