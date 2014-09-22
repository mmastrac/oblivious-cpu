package com.grack.homomorphic.light;

import java.util.Map;

import com.grack.homomorphic.ops.Bit;
import com.grack.homomorphic.ops.State;
import com.grack.homomorphic.ops.Word;

public class StandardState implements State {
	private Map<String, Bit> bits;
	private Map<String, Word> words;
	private Map<String, Word[]> wordArrays;
	private Bit one;
	private Bit zero;
	private boolean debug;

	public StandardState(Bit one, Bit zero, Map<String, Bit> bits,
			Map<String, Word> words, Map<String, Word[]> wordArrays) {
		this.one = one;
		this.zero = zero;
		this.bits = bits;
		this.words = words;
		this.wordArrays = wordArrays;
	}

	public Bit one() {
		return one;
	};

	@Override
	public Bit zero() {
		return zero;
	}

	@Override
	public Word getWordRegister(String name) {
		if (!words.containsKey(name))
			throw new IllegalArgumentException("Invalid key: " + name);
		return words.get(name);
	}

	@Override
	public Bit getBitRegister(String name) {
		if (!bits.containsKey(name))
			throw new IllegalArgumentException("Invalid key: " + name);
		return bits.get(name);
	}

	@Override
	public Word[] getWordArrayRegister(String name) {
		if (!wordArrays.containsKey(name))
			throw new IllegalArgumentException("Invalid key: " + name);
		return wordArrays.get(name);
	}

	@Override
	public void setBitRegister(String name, Bit value) {
		if (!bits.containsKey(name))
			throw new IllegalArgumentException("Invalid key: " + name);
		bits.put(name, value);
	}

	@Override
	public void setWordRegister(String name, Word value) {
		if (!words.containsKey(name))
			throw new IllegalArgumentException("Invalid key: " + name);
		words.put(name, value);
	}

	@Override
	public void setWordArrayRegister(String name, Word[] value) {
		if (!wordArrays.containsKey(name))
			throw new IllegalArgumentException("Invalid key: " + name);
		wordArrays.put(name, value);
	}

	public void debug(Object... things) {
		if (!debug)
			return;

		for (Object thing : things) {
			System.out.print(thing + " ");
		}
		System.out.println();
	}
}
