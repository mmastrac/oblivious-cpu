package com.grack.shapecpu.logging;

import java.util.Map;

import com.grack.shapecpu.Bit;
import com.grack.shapecpu.State;
import com.grack.shapecpu.Word;

public class LoggingState implements State {
	private Map<String, Bit> bits;
	private Map<String, Word> words;
	private Map<String, Word[]> wordArrays;
	private Bit one;
	private Bit zero;
	private LoggingBitFactory bitFactory;

	public LoggingState(LoggingBitFactory bitFactory, Bit one, Bit zero, Map<String, Bit> bits,
			Map<String, Word> words, Map<String, Word[]> wordArrays) {
		this.bitFactory = bitFactory;
		this.one = one;
		this.zero = zero;
		this.bits = bits;
		this.words = words;
		this.wordArrays = wordArrays;
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
		bitFactory.createNamedOutputBit(name, value);
	}

	@Override
	public void setWordRegister(String name, Word value) {
		if (!words.containsKey(name))
			throw new IllegalArgumentException("Invalid key: " + name);
		bitFactory.createNamedOutputWord(name, value);
	}

	@Override
	public void setWordArrayRegister(String name, Word[] value) {
		if (!wordArrays.containsKey(name))
			throw new IllegalArgumentException("Invalid key: " + name);
		bitFactory.createNamedOutputWordArray(name, value);
	}

	@Override
	public Bit one() {
		return one;
	}

	@Override
	public Bit zero() {
		return zero;
	}

}
