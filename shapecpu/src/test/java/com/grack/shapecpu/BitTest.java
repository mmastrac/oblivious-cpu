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
	public void and() {
		assertEquals(1, factory.extract(one.and(one)));
		assertEquals(0, factory.extract(one.and(zero)));
		assertEquals(0, factory.extract(zero.and(one)));
	}

	@Test
	public void or() {
		assertEquals(0, factory.extract(zero.or(zero)));
		assertEquals(1, factory.extract(one.or(one)));
		assertEquals(1, factory.extract(one.or(zero)));
		assertEquals(1, factory.extract(zero.or(one)));
	}

	@Test
	public void xor() {
		assertEquals(0, factory.extract(one.xor(one)));
		assertEquals(1, factory.extract(one.xor(zero)));
		assertEquals(1, factory.extract(zero.xor(one)));
	}
	
	@Test
	public void halfAdd() {
		BitAndBit b1;
		
		b1 = one.halfAdd(one);
		assertEquals(0, factory.extract(b1.getBit1()));
		assertEquals(1, factory.extract(b1.getBit2()));

		b1 = one.halfAdd(zero);
		assertEquals(1, factory.extract(b1.getBit1()));
		assertEquals(0, factory.extract(b1.getBit2()));

		b1 = zero.halfAdd(one);
		assertEquals(1, factory.extract(b1.getBit1()));
		assertEquals(0, factory.extract(b1.getBit2()));

		b1 = zero.halfAdd(zero);
		assertEquals(0, factory.extract(b1.getBit1()));
		assertEquals(0, factory.extract(b1.getBit2()));
	}

	@Test
	public void fullAdd() {
		BitAndBit b1;

		b1 = one.fullAdd(zero, zero);
		assertEquals(1, factory.extract(b1.getBit1()));
		assertEquals(0, factory.extract(b1.getBit2()));

		b1 = one.fullAdd(one, one);
		assertEquals(1, factory.extract(b1.getBit1()));
		assertEquals(1, factory.extract(b1.getBit2()));

		b1 = one.fullAdd(one, zero);
		assertEquals(0, factory.extract(b1.getBit1()));
		assertEquals(1, factory.extract(b1.getBit2()));

		b1 = one.fullAdd(zero, one);
		assertEquals(0, factory.extract(b1.getBit1()));
		assertEquals(1, factory.extract(b1.getBit2()));

		b1 = zero.fullAdd(one, one);
		assertEquals(0, factory.extract(b1.getBit1()));
		assertEquals(1, factory.extract(b1.getBit2()));

		b1 = zero.fullAdd(one, zero);
		assertEquals(1, factory.extract(b1.getBit1()));
		assertEquals(0, factory.extract(b1.getBit2()));

		b1 = zero.fullAdd(zero, one);
		assertEquals(1, factory.extract(b1.getBit1()));
		assertEquals(0, factory.extract(b1.getBit2()));

		b1 = zero.fullAdd(zero, zero);
		assertEquals(0, factory.extract(b1.getBit1()));
		assertEquals(0, factory.extract(b1.getBit2()));
	}
	
	@Test
	public void ifThen() {
		assertEquals(1, factory.extract(one.ifThen(one, zero)));
		assertEquals(0, factory.extract(one.ifThen(zero, one)));
		assertEquals(1, factory.extract(zero.ifThen(zero, one)));
		assertEquals(0, factory.extract(zero.ifThen(one, zero)));
	}
}
