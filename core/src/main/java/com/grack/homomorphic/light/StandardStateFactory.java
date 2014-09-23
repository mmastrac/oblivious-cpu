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
	private Map<String, Object> initialState;
	private boolean debug;

	public StandardStateFactory(NativeBitFactory bitFactory) {
		this.bitFactory = bitFactory;
	}

	public StandardStateFactory(NativeBitFactory bitFactory,
			Map<String, Object> initialState, boolean debug) {
		this.bitFactory = bitFactory;
		this.initialState = initialState;
		this.debug = debug;
	}

	@Override
	public void allocateWordRegister(String name, int width) {
		if (initialState != null && initialState.containsKey(name)) {
			Object value = initialState.get(name);
			if (value instanceof Number) {
				words.put(name, bitFactory.encodeWord(
						((Number) value).intValue(), width));
			} else {
				throw new IllegalArgumentException(
						"Word register doesn't match with initial value: "
								+ value);
			}
		} else {
			words.put(name, bitFactory.encodeWord(0, width));
		}
	}

	@Override
	public void allocateBitRegister(String name) {
		if (initialState != null && initialState.containsKey(name)) {
			Object value = initialState.get(name);
			if (value instanceof Number) {
				bits.put(name,
						bitFactory.encodeBit(((Number) value).intValue()));
			} else {
				throw new IllegalArgumentException(
						"Bit register doesn't match with initial value: "
								+ value);
			}
		} else {
			bits.put(name, bitFactory.encodeBit(0));
		}
	}

	@Override
	public void allocateWordArrayRegister(String name, int width, int size) {
		if (initialState != null && initialState.containsKey(name)) {
			Object value = initialState.get(name);
			if (value instanceof int[]) {
				int[] values = (int[]) value;

				Word[] words = new Word[values.length];
				for (int i = 0; i < values.length; i++) {
					words[i] = bitFactory.encodeWord(values[i], width);
				}
				wordArrays.put(name, words);
			} else {
				throw new IllegalArgumentException(
						"Word array register doesn't match with initial value: "
								+ value);
			}
		} else {
			Word[] words = new Word[size];
			for (int i = 0; i < size; i++) {
				words[i] = bitFactory.encodeWord(0, width);
			}
			wordArrays.put(name, words);
		}
	}

	@Override
	public State createState() {
		return new StandardState(bitFactory.encodeBit(1),
				bitFactory.encodeBit(0), bits, words, wordArrays, debug);
	}
}
