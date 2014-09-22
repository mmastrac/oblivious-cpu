package com.grack.homomorphic.logging;

import java.util.HashMap;
import java.util.Map;

import com.grack.homomorphic.ops.Bit;
import com.grack.homomorphic.ops.State;
import com.grack.homomorphic.ops.StateFactory;
import com.grack.homomorphic.ops.Word;

public class LoggingStateFactory implements StateFactory {
	private LoggingBitFactory bitFactory;
	private static final Map<String, Bit> bits = new HashMap<>();
	private static final Map<String, Word> words = new HashMap<>();
	private static final Map<String, Word[]> wordArrays = new HashMap<>();

	public LoggingStateFactory(LoggingBitFactory bitFactory) {
		this.bitFactory = bitFactory;
	}

	@Override
	public void allocateWordRegister(String name, int width) {
		Word word = bitFactory.createNamedInputWord(name, width);
		words.put(name, word);
	}

	@Override
	public void allocateBitRegister(String name) {
		Bit bit = bitFactory.createNamedInputBit(name);
		bits.put(name, bit);
	}

	@Override
	public void allocateWordArrayRegister(String name, int width, int size) {
		Word[] words = bitFactory.createNamedInputWordArray(name, width, size);
		wordArrays.put(name, words);
	}

	@Override
	public State createState() {
		return new LoggingState(bitFactory,
				bitFactory.createNamedInputBit("one"),
				bitFactory.createNamedInputBit("zero"), bits, words, wordArrays);
	}
}
