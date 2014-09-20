package com.grack.homomorphic.light;

import java.util.HashMap;
import java.util.Map;

import com.grack.homomorphic.ops.Bit;
import com.grack.homomorphic.ops.NativeBitFactory;
import com.grack.homomorphic.ops.State;
import com.grack.homomorphic.ops.StateFactory;
import com.grack.homomorphic.ops.Word;

public class StandardStateFactory implements StateFactory {
	private static final Map<String, Bit> bits = new HashMap<>();
	private static final Map<String, Word> words = new HashMap<>();
	private static final Map<String, Word[]> wordArrays = new HashMap<>();
	private NativeBitFactory bitFactory;

	public StandardStateFactory(NativeBitFactory bitFactory) {
		this.bitFactory = bitFactory;
	}

	@Override
	public void allocateWordRegister(String name, int width) {
		words.put(name, bitFactory.encodeWord(0, width));
	}

	@Override
	public void allocateBitRegister(String name) {
		bits.put(name, bitFactory.encodeBit(0));
	}

	@Override
	public void allocateWordArrayRegister(String name, int width, int[] contents) {
		Word[] words = new Word[contents.length];
		for (int i = 0; i < contents.length; i++) {
			words[i] = bitFactory.encodeWord(contents[i], width);
		}
		wordArrays.put(name, words);
	}

	@Override
	public State createState() {
		return new StandardState(bitFactory.encodeBit(1), bitFactory.encodeBit(0),
				bits, words, wordArrays);
	}
}
