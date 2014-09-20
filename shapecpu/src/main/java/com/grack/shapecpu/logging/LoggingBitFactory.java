package com.grack.shapecpu.logging;

import java.util.ArrayList;
import java.util.Arrays;

import com.grack.shapecpu.NativeBit;
import com.grack.shapecpu.NativeBitFactory;

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
		return create(LoggingBitNodeType.TERMINAL, new int[0]);
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

	public NativeBit create(LoggingBitNodeType type, int[] children) {
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
		
		return new LoggingBit(this, type, children);
	}
}
