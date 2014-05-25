package com.grack.shapecpu;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.grack.shapecpu.light.LightNativeBitFactory;

public class BitTest {
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
		assertEquals(1, factory.extract(one.and(one)));
		assertEquals(0, factory.extract(one.and(zero)));
		assertEquals(0, factory.extract(zero.and(one)));
	}

	@Test
	public void orWorks() {
		assertEquals(0, factory.extract(zero.or(zero)));
		assertEquals(1, factory.extract(one.or(one)));
		assertEquals(1, factory.extract(one.or(zero)));
		assertEquals(1, factory.extract(zero.or(one)));
	}

	@Test
	public void xorWorks() {
		assertEquals(0, factory.extract(one.xor(one)));
		assertEquals(1, factory.extract(one.xor(zero)));
		assertEquals(1, factory.extract(zero.xor(one)));
	}
	
	@Test
	public void ifThenWorks() {
		assertEquals(1, factory.extract(one.ifThen(one, zero)));
		assertEquals(0, factory.extract(one.ifThen(zero, one)));
		assertEquals(1, factory.extract(zero.ifThen(zero, one)));
		assertEquals(0, factory.extract(zero.ifThen(one, zero)));
	}
}
