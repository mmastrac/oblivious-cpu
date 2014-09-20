package com.grack.shapecpu;

import java.util.HashMap;
import java.util.Map;

public class NativeStateFactory {
	private static final Map<String, Bit> bits = new HashMap<>();
	private static final Map<String, Word> words = new HashMap<>();
	private static final Map<String, Word[]> wordArrays = new HashMap<>();
	private NativeBitFactory bitFactory;

	public NativeStateFactory(NativeBitFactory bitFactory) {
		this.bitFactory = bitFactory;
	}
	
	public void allocateWordRegister(String name) {
		words.put(name, bitFactory.encodeWord(0, 8));
	}
	
	public void allocateBitRegister(String name) {
		bits.put(name, bitFactory.encodeBit(0));
	}

	public void allocateWordArrayRegister(String name, int[] contents) {
		Word[] words = new Word[contents.length];
		// TODO: auto-size words
		for (int i = 0; i < contents.length; i++) {
			words[i] = bitFactory.encodeWord(contents[i], 13);
		}
		wordArrays.put(name, words);
	}

	public State createState() {
		return new State(bits, words, wordArrays);
	}
}
