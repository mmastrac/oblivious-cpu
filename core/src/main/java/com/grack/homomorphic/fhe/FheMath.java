package com.grack.homomorphic.fhe;

import java.math.BigInteger;

import org.jscience.mathematics.function.Polynomial;
import org.jscience.mathematics.function.Term;
import org.jscience.mathematics.function.Variable;
import org.jscience.mathematics.number.LargeInteger;

public class FheMath {
	/**
	 * Slow, recursive determinant.
	 * 
	 * This will be replaced by something better.
	 */
	public static LargeInteger determinant(LargeInteger[][] matrix) {
		LargeInteger sum = LargeInteger.ZERO;
		LargeInteger s;
		if (matrix.length == 1) {
			return (matrix[0][0]);
		}
		for (int i = 0; i < matrix.length; i++) {
			LargeInteger[][] smaller = new LargeInteger[matrix.length - 1][matrix.length - 1];
			for (int a = 1; a < matrix.length; a++) {
				for (int b = 0; b < matrix.length; b++) {
					if (b < i) {
						smaller[a - 1][b] = matrix[a][b];
					} else if (b > i) {
						smaller[a - 1][b - 1] = matrix[a][b];
					}
				}
			}

			if (i % 2 == 0) {
				s = LargeInteger.ONE;
			} else {
				s = LargeInteger.ONE.opposite();
			}

			sum = sum.plus(s.times(matrix[0][i]).times(determinant(smaller)));
		}
		return (sum);
	}

	public static LargeInteger determinantFast(LargeInteger[][] a) {
		int n = a.length;
		LargeInteger[][] b = new LargeInteger[n][n];
		for (int i = 0; i < n; i++)
			for (int j = 0; j < n; j++)
				b[i][j] = a[i][j];

		int sign = 1;
		int[] pow = new int[n];
		for (int i = 0; i < n; ++i) {
			int k = i;
			for (int j = i + 1; j < n; ++j) {
				if (b[k][i].isZero() || !b[j][i].isZero()
						&& b[k][i].abs().compareTo(b[j][i].abs()) > 0) {
					k = j;
				}
			}
			if (b[k][i].isZero())
				return LargeInteger.ZERO;
			if (i != k) {
				sign = -sign;
				LargeInteger[] t = b[i];
				b[i] = b[k];
				b[k] = t;
			}
			++pow[i];
			for (int j = i + 1; j < n; ++j)
				if (!b[j][i].isZero()) {
					for (int p = i + 1; p < n; ++p) {
						b[j][p] = b[j][p].times(b[i][i]).minus(
								b[i][p].times(b[j][i]));
					}
					--pow[i];
				}
		}
		LargeInteger det = LargeInteger.ONE;
		for (int i = 0; i < n; i++)
			if (pow[i] > 0)
				det = det.times(b[i][i].pow(pow[i]));
		for (int i = 0; i < n; i++)
			if (pow[i] < 0)
				det = det.divide(b[i][i].pow(-pow[i]));
		if (sign < 0)
			det = det.opposite();
		return det;
	}

	/**
	 * Compute the determinant of the Sylvester Matrix.
	 */
	public static LargeInteger resultant(Polynomial<LargeInteger> a,
			Polynomial<LargeInteger> b, Variable<LargeInteger> v) {
		int oa = a.getOrder(v);
		int ob = b.getOrder(v);
		int order = oa + ob;

		LargeInteger[][] rows = new LargeInteger[order][];
		for (int i = 0; i < order; i++) {
			rows[i] = new LargeInteger[order];
			for (int j = 0; j < order; j++) {
				rows[i][j] = LargeInteger.ZERO;
			}
		}
		for (Term t : a.getTerms()) {
			int pow = t.getPower(v);
			LargeInteger ca = a.getCoefficient(t);
			for (int i = 0; i < ob; i++) {
				rows[i + (oa - pow)][i] = ca;
			}
		}

		for (Term t : b.getTerms()) {
			int pow = t.getPower(v);
			LargeInteger cb = b.getCoefficient(t);
			for (int i = 0; i < oa; i++) {
				rows[i + (ob - pow)][i + ob] = cb;
			}
		}

		return determinantFast(rows);
	}

}
