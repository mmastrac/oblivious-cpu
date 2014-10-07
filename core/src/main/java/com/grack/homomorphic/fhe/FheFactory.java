package com.grack.homomorphic.fhe;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

import org.jscience.mathematics.function.Polynomial;
import org.jscience.mathematics.function.Term;
import org.jscience.mathematics.function.Variable;
import org.jscience.mathematics.number.LargeInteger;

public class FheFactory {
	private static final int N_BITS = 384;
	private static final int N = 8;

	public void generate() {
		Random r = new SecureRandom();

		Variable.Local<LargeInteger> x = new Variable.Local<>("x");

		// fn = x^N + 1
		Polynomial<LargeInteger> oneX = Polynomial.valueOf(LargeInteger.ONE, x);
		Polynomial<LargeInteger> fn = oneX.pow(N).plus(LargeInteger.ONE);

		// Generate a random polynomial for which the resultant of it an x^N+1
		// is prime
		LargeInteger res;
		Polynomial<LargeInteger> p;
		
		int test = 0;
		do {
			p = Polynomial.valueOf(LargeInteger.ONE, Term.ONE);
			for (int i = 0; i < N; i++) {
				BigInteger coef = new BigInteger(N_BITS, r);
				coef = coef.subtract(BigInteger.ONE.shiftLeft(N_BITS - 1))
						.shiftLeft(1);
				if (i == 0)
					p = p.plus(LargeInteger.valueOf(coef));
				else
					p = p.plus(oneX.pow(i).times(LargeInteger.valueOf(coef)));
			}
			res = FheMath.resultant(fn, p, x);
			test++;
		} while (!new BigInteger(res.toString()).isProbablePrime(10));

		System.out.println("Tests: " + test);
		
	}

	public static void main(String[] args) {
		new FheFactory().generate();
	}
}
