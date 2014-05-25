package com.grack.shapecpu;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.grack.shapecpu.light.LightNativeBitFactory;

public class WordTest {
	private LightNativeBitFactory factory;
	private Bit one;
	private Bit zero;

	@Before
	public void setup() {
		factory = new LightNativeBitFactory();
		one = factory.encodeBit(1);
		zero = factory.encodeBit(0);
	}

	@Test
	public void and() {
		Word a = factory.encodeWord(0x101010L, 32);
		Word b = factory.encodeWord(0xffff0000L, 32);

		assertEquals(0x100000L, factory.extract(a.and(b)));
		assertEquals(0x100000L, factory.extract(b.and(a)));
	}

	@Test
	public void andWithDifferentSizes() {
		Word a = factory.encodeWord(0xffL, 8);
		Word b = factory.encodeWord(0xffff00f0L, 32);

		assertEquals(0xf0L, factory.extract(a.and(b)));
		assertEquals(0xf0L, factory.extract(b.and(a)));
	}

	@Test
	public void or() {
		Word a = factory.encodeWord(0x101010L, 32);
		Word b = factory.encodeWord(0xffff0000L, 32);

		assertEquals(0xffff1010L, factory.extract(a.or(b)));
		assertEquals(0xffff1010L, factory.extract(b.or(a)));
	}

	@Test
	public void orWithDifferentSizes() {
		Word a = factory.encodeWord(0xffL, 8);
		Word b = factory.encodeWord(0xffff0000L, 32);

		assertEquals(0xffff00ffL, factory.extract(a.or(b)));
		assertEquals(0xffff00ffL, factory.extract(b.or(a)));
	}

	@Test
	public void xor() {
		Word a = factory.encodeWord(0x101010L, 32);
		Word b = factory.encodeWord(0xffff0000L, 32);

		assertEquals(0xffef1010L, factory.extract(a.xor(b)));
		assertEquals(0xffef1010L, factory.extract(b.xor(a)));
	}

	@Test
	public void xorWithDifferentSizes() {
		Word a = factory.encodeWord(0xffL, 8);
		Word b = factory.encodeWord(0xffff0000L, 32);

		assertEquals(0xffff00ffL, factory.extract(a.xor(b)));
		assertEquals(0xffff00ffL, factory.extract(b.xor(a)));
	}

	@Test
	public void not() {
		Word a = factory.encodeWord(0xffff0000L, 32);

		assertEquals(0x0000ffffL, factory.extract(a.not()));
	}

	@Test
	public void eq() {
		Word a = factory.encodeWord(10, 32);

		assertEquals(0, factory.extract(a.eq(9)));
		assertEquals(1, factory.extract(a.eq(10)));
		assertEquals(0, factory.extract(a.eq(11)));
	}

	@Test
	public void bitsEq() {
		Word a = factory.encodeWord(0b1100, 32);

		assertEquals(1, factory.extract(a.bitsEq(3, 3, 1)));
		assertEquals(0, factory.extract(a.bitsEq(3, 3, 0)));
		assertEquals(1, factory.extract(a.bitsEq(2, 2, 1)));
		assertEquals(0, factory.extract(a.bitsEq(1, 1, 1)));
		assertEquals(0, factory.extract(a.bitsEq(0, 0, 1)));

		assertEquals(1, factory.extract(a.bitsEq(3, 0, 0b1100)));
		assertEquals(0, factory.extract(a.bitsEq(3, 0, 0b0001)));
		assertEquals(0, factory.extract(a.bitsEq(3, 0, 0b0100)));

		assertEquals(1, factory.extract(a.bitsEq(2, 0, 0b100)));
		assertEquals(0, factory.extract(a.bitsEq(2, 0, 0b110)));
	}

	@Test
	public void add1() {
		Word a = factory.encodeWord(0x101010L, 32);
		Word b = factory.encodeWord(0x010101L, 32);

		assertEquals(0x111111, factory.extract(a.add(b)));
	}

	@Test
	public void add2() {
		Word a = factory.encodeWord(0b111111L, 10);
		Word b = factory.encodeWord(0b000001L, 10);

		assertEquals(0b1000000, factory.extract(a.add(b)));
	}

	@Test
	public void add3() {
		Word a = factory.encodeWord(0b110111L, 10);
		Word b = factory.encodeWord(0b000001L, 10);

		assertEquals(0b111000, factory.extract(a.add(b)));
	}

	@Test
	public void addWithCarry1() {
		Word a = factory.encodeWord(0b101010L, 6);
		Word b = factory.encodeWord(0b010101L, 6);

		WordAndBit r = a.addWithCarry(b, zero);
		assertEquals(0b111111, factory.extract(r.getWord()));
		assertEquals(0, factory.extract(r.getBit()));
	}

	@Test
	public void addWithCarry2() {
		Word a = factory.encodeWord(0b101010L, 6);
		Word b = factory.encodeWord(0b010101L, 6);

		WordAndBit r = a.addWithCarry(b, one);
		assertEquals(0b000000, factory.extract(r.getWord()));
		assertEquals(1, factory.extract(r.getBit()));
	}

	@Test
	public void addWithCarry3() {
		Word a = factory.encodeWord(0b111111L, 6);
		Word b = factory.encodeWord(0b000001L, 6);
		
		WordAndBit r = a.addWithCarry(b, zero);
		assertEquals(0b000000, factory.extract(r.getWord()));
		assertEquals(1, factory.extract(r.getBit()));
	}

	@Test
	public void addWithCarry4() {
		Word a = factory.encodeWord(0b111111L, 6);
		Word b = factory.encodeWord(0b000001L, 6);
		
		WordAndBit r = a.addWithCarry(b, one);
		assertEquals(0b000001, factory.extract(r.getWord()));
		assertEquals(1, factory.extract(r.getBit()));
	}

	@Test
	public void ifThen() {
		assertEquals(1, factory.extract(one.ifThen(one, zero)));
		assertEquals(0, factory.extract(one.ifThen(zero, one)));
		assertEquals(1, factory.extract(zero.ifThen(zero, one)));
		assertEquals(0, factory.extract(zero.ifThen(one, zero)));
	}
}
