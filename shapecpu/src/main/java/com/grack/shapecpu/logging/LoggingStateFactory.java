package com.grack.shapecpu.logging;

import java.util.HashMap;
import java.util.Map;

import com.grack.shapecpu.Bit;
import com.grack.shapecpu.State;
import com.grack.shapecpu.StateFactory;
import com.grack.shapecpu.Word;

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
	public void allocateWordArrayRegister(String name, int width, int[] contents) {
		Word[] words = bitFactory.createNamedInputWordArray(name, width,
				contents.length);
		wordArrays.put(name, words);
	}

	@Override
	public State createState() {
		return new LoggingState(bitFactory,
				bitFactory.createNamedInputBit("one"),
				bitFactory.createNamedInputBit("zero"), bits, words, wordArrays);
	}
}
