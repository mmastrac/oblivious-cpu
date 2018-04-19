package com.grack.homomorphic;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.grack.homomorphic.light.LightBitFactory;
import com.grack.homomorphic.ops.Bit;
import com.grack.homomorphic.ops.Word;
import com.grack.homomorphic.ops.WordAndBit;

public class WordTest {
	private LightBitFactory factory;
	private Bit one;
	private Bit zero;

	@Before
	public void setup() {
		factory = new LightBitFactory();
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
	public void add3a() {
		Word a = factory.encodeWord(0b110111L, 10);
		Word b = factory.encodeWord(0b000001L, 10);

		assertEquals(0b111000, factory.extract(a.add(b)));
	}

	@Test
	public void add3b() {
		Word a = factory.encodeWord(0b110111L, 10);
		Word b = factory.encodeWord(0b000001L, 1);

		assertEquals(0b111000, factory.extract(a.add(b)));
	}

	@Test
	public void addBit1() {
		Word a = factory.encodeWord(0b101010L, 32);

		assertEquals(0b101010, factory.extract(a.add(factory.encodeBit(0))));
	}

	@Test
	public void addBit2() {
		Word a = factory.encodeWord(0b101010L, 32);

		assertEquals(0b101011, factory.extract(a.add(factory.encodeBit(1))));
	}

	@Test
	public void addBit3() {
		Word a = factory.encodeWord(0b111111L, 32);

		assertEquals(0b111111L, factory.extract(a.add(factory.encodeBit(0))));
	}

	@Test
	public void addBit4() {
		Word a = factory.encodeWord(0b111111L, 32);

		assertEquals(0b1000000, factory.extract(a.add(factory.encodeBit(1))));
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
	public void shl() {
		Word a = factory.encodeWord(0b101111L, 6);
		assertEquals(0b1011110, factory.extract(a.shl(1, zero)));
		assertEquals(0b1011111, factory.extract(a.shl(1, one)));
	}
	
	@Test
	public void shr() {
		Word a = factory.encodeWord(0b101111L, 6);
		assertEquals(0b010111, factory.extract(a.shr(1)));
	}
	
	@Test
	public void ifThen() {
		assertEquals(1, factory.extract(one.ifThen(one, zero)));
		assertEquals(0, factory.extract(one.ifThen(zero, one)));
		assertEquals(1, factory.extract(zero.ifThen(zero, one)));
		assertEquals(0, factory.extract(zero.ifThen(one, zero)));
	}

	@Test
	public void compare() {
		Word a = factory.encodeWord(10, 8);
		Word b = factory.encodeWord(8, 8);

		// b is loaded, compare a
		Word compare1 = a.not().add(one).add(b);
		// minus
		assertEquals(1, factory.extract(compare1.bit(7)));

		// a is loaded, compare b
		Word compare2 = b.not().add(one).add(a);
		// not minus
		assertEquals(0, factory.extract(compare2.bit(7)));

		// a is loaded, compare a
		Word compare3 = a.not().add(one).add(a);
		// equal
		assertEquals(0, factory.extract(compare3));
	}

	@Test
	public void compare2() {
		Word a = factory.encodeWord(0b00001010, 8);
		Word b = factory.encodeWord(0b00001010, 8);

		// b is loaded, compare a
		Word compare1 = a.not().add(one).add(b);
		assertEquals(0, factory.extract(compare1));
	}

	@Test
	public void decode() {
		Word a00 = factory.encodeWord(0b00000000, 8);
		Word a01 = factory.encodeWord(0b11111111, 8);
		Word a10 = factory.encodeWord(0b11110000, 8);
		Word a11 = factory.encodeWord(0b00001111, 8);

		assertEquals(
				factory.extract(a00),
				factory.extract(factory.encodeWord(0b00, 2).decode(a00, a01,
						a10, a11)));
		assertEquals(
				factory.extract(a01),
				factory.extract(factory.encodeWord(0b01, 2).decode(a00, a01,
						a10, a11)));
		assertEquals(
				factory.extract(a10),
				factory.extract(factory.encodeWord(0b10, 2).decode(a00, a01,
						a10, a11)));
		assertEquals(
				factory.extract(a11),
				factory.extract(factory.encodeWord(0b11, 2).decode(a00, a01,
						a10, a11)));
	}
	
	@Test
	public void multiply8x8() {
		for (int i = 0; i < 255; i++) {
			for (int j = 0; j < 255; j++) {
				Word w1 = factory.encodeWord(j, 8);
				Word w2 = factory.encodeWord(i, 8);
				
				Word mul = w1.multiplyDadda(w2);
				assertEquals("(" + j + "*" + i + ") incorrect", (j * i) & 0xffff, factory.extract(mul));
			}
		}
	}

	@Test
	public void multiply8x8Signed() {
		for (int i = -128; i <= 127; i++) {
			for (int j = -128; j <= 127; j++) {
				Word w1 = factory.encodeWord(j & 0xff, 8);
				Word w2 = factory.encodeWord(i & 0xff, 8);
				
				Word mul = w1.multiplyDadda(w2, true);
				assertEquals("(" + j + "*" + i + ") incorrect", (j * i) & 0xffff, factory.extract(mul));
			}
		}
	}

	@Test
	public void multiply8x8SignedOrUnsigned() {
		for (int i = -128; i <= 127; i++) {
			for (int j = -128; j <= 127; j++) {
				Word w1 = factory.encodeWord(j & 0xff, 8);
				Word w2 = factory.encodeWord(i & 0xff, 8);
				
				Word mul = w1.multiplyDadda(w2, factory.encodeBit(1));
				assertEquals("(" + j + "*" + i + ") incorrect", (j * i) & 0xffff, factory.extract(mul));
			}
		}
		
		for (int i = 0; i < 255; i++) {
			for (int j = 0; j < 255; j++) {
				Word w1 = factory.encodeWord(j, 8);
				Word w2 = factory.encodeWord(i, 8);
				
				Word mul = w1.multiplyDadda(w2, factory.encodeBit(0));
				assertEquals("(" + j + "*" + i + ") incorrect", (j * i) & 0xffff, factory.extract(mul));
			}
		}
	}

	@Test
	public void multiply8x8InParts() {
		for (int i = 0; i < 255; i++) {
			for (int j = 0; j < 255; j++) {
				Word w1 = factory.encodeWord(j, 8);
				Word w2 = factory.encodeWord(i, 8);
				
				Word out1 = w1.multiplyDadda(w2.bits(3, 0));
				Word out2 = w1.multiplyDadda(w2.bits(7, 4));
				
				Word mul = out2.shl(4, factory.encodeBit(0)).add(out1);

				assertEquals("(" + j + "*" + i + ") incorrect", (j * i) & 0xffff, factory.extract(mul));
			}
		}
	}
	
	@Test
	public void multiply8x4() {
		for (int i = 0; i < 255; i++) {
			for (int j = 0; j < 15; j++) {
				Word w1 = factory.encodeWord(j, 4);
				Word w2 = factory.encodeWord(i, 8);
				
				Word mul1 = w1.multiplyDadda(w2);
				assertEquals("(" + j + "*" + i + ") incorrect", (j * i) & 0xffff, factory.extract(mul1));
				
				Word mul2 = w2.multiplyDadda(w1);
				assertEquals("(" + j + "*" + i + ") incorrect", (j * i) & 0xffff, factory.extract(mul2));
			}
		}
	}

	@Test
	public void multiply4x4() {
		for (int i = 0; i < 15; i++) {
			for (int j = 0; j < 15; j++) {
				Word w1 = factory.encodeWord(j, 4);
				Word w2 = factory.encodeWord(i, 4);
				
				Word mul = w1.multiplyDadda(w2);
				assertEquals("(" + j + "*" + i + ") incorrect", (j * i) & 0xff, factory.extract(mul));
			}
		}
	}

	@Test
	public void multiply32x32() {
		Word w1 = factory.encodeWord(0xffff_ffffL, 32);
		Word w2 = factory.encodeWord(0xffff_ffffL, 32);
		
		Word mul1 = w1.multiplyDadda(w2);
		assertEquals(0xffff_ffffL * 0xffff_ffffL, factory.extract(mul1));
	}
}
