package com.grack.homomorphic.fhe;

import static org.junit.Assert.*;

import org.jscience.mathematics.function.Polynomial;
import org.jscience.mathematics.function.Variable;
import org.jscience.mathematics.number.LargeInteger;
import org.junit.Test;

public class FheMathTest {
	@Test
	public void resultantSimple() {
		Variable.Local<LargeInteger> x = new Variable.Local<>("x");
		Polynomial<LargeInteger> a = Polynomial.valueOf(LargeInteger.ONE, x)
				.pow(8).plus(LargeInteger.ONE);
		Polynomial<LargeInteger> b = Polynomial.valueOf(LargeInteger.ZERO, x);
		for (int i = 1; i < 4; i++) {
			b = b.plus(Polynomial.valueOf(LargeInteger.ONE, x).pow(i)
					.times(LargeInteger.valueOf(i)));
		}

		assertEquals(6596L, FheMath.resultant(a, b, x).longValue());
	}

	// Resultant[x^8+1, x^7+10 x^5+2x^4+99x,x]
	@Test
	public void resultantBigger() {
		Variable.Local<LargeInteger> x = new Variable.Local<>("x");
		Polynomial<LargeInteger> oneX = Polynomial.valueOf(LargeInteger.ONE, x);
		Polynomial<LargeInteger> a = oneX.pow(8).plus(LargeInteger.ONE);
		Polynomial<LargeInteger> b = oneX.pow(7)
				.plus(oneX.pow(5).times(LargeInteger.valueOf(10)))
				.plus(oneX.pow(4).times(LargeInteger.valueOf(2)))
				.plus(oneX.pow(1).times(LargeInteger.valueOf(99)));

		assertEquals(9610526820274436L, FheMath.resultant(a, b, x).longValue());
	}
}
