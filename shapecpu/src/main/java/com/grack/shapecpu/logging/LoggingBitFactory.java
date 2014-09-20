package com.grack.shapecpu.logging;

import java.util.ArrayList;
import java.util.Arrays;

import com.grack.shapecpu.Bit;
import com.grack.shapecpu.NativeBit;
import com.grack.shapecpu.NativeBitFactory;
import com.grack.shapecpu.Word;

public class LoggingBitFactory implements NativeBitFactory {
	private int index;
	private ArrayList<LoggingBit> bits = new ArrayList<>();
	
	int nextIndex() {
		return index++;
	}

	public LoggingBit get(int index) {
		return bits.get(index);
	}
	
	@Override
	public NativeBit encodeNativeBit(int bit) {
		throw new RuntimeException("Not implemented");
	}

	public NativeBit encodeNamedInputBit(String name) {
		return create(name, LoggingBitNodeType.INPUT, new int[0]);
	}

	public NativeBit encodeNamedOutputBit(String name, NativeBit value) {
		return create(name, LoggingBitNodeType.OUTPUT, new int[] { ((LoggingBit) value).index() });
	}
	
	public Bit createNamedInputBit(String name) {
		return new Bit(encodeNamedInputBit(name));
	}

	public Bit createNamedOutputBit(String name, Bit value) {
		return new Bit(encodeNamedOutputBit(name, value.nativeBit()));
	}

	public Word createNamedInputWord(String name, int width) {
		Bit[] bits = new Bit[width];
		for (int i = 0; i < width; i++)
			bits[i] = createNamedInputBit(name + ":" + i);
		return new Word(bits);
	}

	public Word createNamedOutputWord(String name, Word value) {
		Bit[] bits = new Bit[value.size()];
		for (int i = 0; i < value.size(); i++)
			bits[i] = createNamedOutputBit(name + ":" + i, value.bit(i));
		return new Word(bits);
	}

	public Word[] createNamedInputWordArray(String name, int width, int size) {
		Word[] words = new Word[size];
		for (int i = 0; i < size; i++)
			words[i] = createNamedInputWord(name + ":" + i, width);
		return words;
	}

	public Word[] createNamedOutputWordArray(String name, Word[] value) {
		Word[] words = new Word[value.length];
		for (int i = 0; i < value.length; i++)
			words[i] = createNamedOutputWord(name + ":" + i, value[i]);
		return words;
	}
	
	public int nodeCount() {
		return bits.size();
	}

	int register(LoggingBit loggingBit) {
		int idx = bits.size();
		bits.add(loggingBit);
		for (int i : loggingBit.children()) {
			get(i).addBackRef(idx);
		}
		return idx;
	}

	public NativeBit create(String name, LoggingBitNodeType type, int[] children) {
		// See if there is a bit with this definition already
		if (children.length > 0) {
			Arrays.sort(children);
			
			// Note: only need to check the first child as both children will
			// have any valid backrefs we can re-use
			LoggingBit bitRef = get(children[0]);
			for (int backRef : bitRef.backRefs()) {
				LoggingBit bit = get(backRef);
				if (bit.type() == type && Arrays.equals(children, bit.children())) {
					return bit;
				}
			}
		}
		
		return new LoggingBit(this, name, type, children);
	}

	public String nameOf(int idx) {
		LoggingBit bit = get(idx);
		if (bit.name() != null)
			return bit.name();
		
		return Integer.toString(bit.index());
	}
}
