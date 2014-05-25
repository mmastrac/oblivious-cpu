package com.grack.shapecpu;

import static org.junit.Assert.*;

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
	public void andWorks() {
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
	public void orWorks() {
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
	public void xorWorks() {
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
	public void notWorks() {
		Word a = factory.encodeWord(0xffff0000L, 32);
		
		assertEquals(0x0000ffffL, factory.extract(a.not()));
	}

	@Test
	public void eqWorks() {
		Word a = factory.encodeWord(10, 32);
		
		assertEquals(0, factory.extract(a.eq(9)));
		assertEquals(1, factory.extract(a.eq(10)));
		assertEquals(0, factory.extract(a.eq(11)));
	}
	
	@Test
	public void addWithoutCarry() {
		Word a = factory.encodeWord(0x101010L, 32);
		Word b = factory.encodeWord(0x010101L, 32);
		
		assertEquals(0x111111, factory.extract(a.add(b)));
	}
	
	@Test
	public void ifThenWorks() {
		assertEquals(1, factory.extract(one.ifThen(one, zero)));
		assertEquals(0, factory.extract(one.ifThen(zero, one)));
		assertEquals(1, factory.extract(zero.ifThen(zero, one)));
		assertEquals(0, factory.extract(zero.ifThen(one, zero)));
	}
}
