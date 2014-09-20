package com.grack.shapecpu;

import java.util.Map;

public class State {
	private Map<String, Bit> bits;
	private Map<String, Word> words;
	private Map<String, Word[]> wordArrays;
	
	public State(Map<String, Bit> bits, Map<String, Word> words, Map<String, Word[]> wordArrays) {
		this.bits = bits;
		this.words = words;
		this.wordArrays = wordArrays;
	}
	
	public Word getWordRegister(String name) {
		if (!words.containsKey(name))
			throw new IllegalArgumentException("Invalid key: " + name);
		return words.get(name);
	}

	public Bit getBitRegister(String name) {
		if (!bits.containsKey(name))
			throw new IllegalArgumentException("Invalid key: " + name);
		return bits.get(name);
	}

	public Word[] getWordArrayRegister(String name) {
		if (!wordArrays.containsKey(name))
			throw new IllegalArgumentException("Invalid key: " + name);
		return wordArrays.get(name);
	}

	public void setBitRegister(String name, Bit value) {
		if (!bits.containsKey(name))
			throw new IllegalArgumentException("Invalid key: " + name);
		bits.put(name, value);
	}

	public void setWordRegister(String name, Word value) {
		if (!words.containsKey(name))
			throw new IllegalArgumentException("Invalid key: " + name);
		words.put(name, value);
	}

	public void setWordArrayRegister(String name, Word[] value) {
		if (!wordArrays.containsKey(name))
			throw new IllegalArgumentException("Invalid key: " + name);
		wordArrays.put(name, value);
	}
}
