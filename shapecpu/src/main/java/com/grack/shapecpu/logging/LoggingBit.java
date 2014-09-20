package com.grack.shapecpu.logging;

import java.util.ArrayList;
import java.util.List;

import com.grack.shapecpu.NativeBit;

public class LoggingBit implements NativeBit {
	private LoggingBitFactory bitFactory;
	private LoggingBitNodeType type;
	private int[] children;
	private int nodeIndex;
	private ArrayList<Integer> backRefs = new ArrayList<>();

	public LoggingBit(LoggingBitFactory bitFactory, LoggingBitNodeType type,
			int[] children) {
		this.bitFactory = bitFactory;
		this.type = type;
		this.children = children;
		this.nodeIndex = bitFactory.register(this);
	}

	@Override
	public NativeBit xor(NativeBit n) {
		return bitFactory.create(LoggingBitNodeType.XOR, new int[] {
				this.nodeIndex, ((LoggingBit) n).nodeIndex });
	}

	@Override
	public NativeBit and(NativeBit n) {
		return bitFactory.create(LoggingBitNodeType.AND, new int[] {
				this.nodeIndex, ((LoggingBit) n).nodeIndex });
	}

	@Override
	public NativeBit not() {
		return bitFactory.create(LoggingBitNodeType.NOT,
				new int[] { this.nodeIndex });
	}

	public String describe() {
		switch (type) {
		case TERMINAL:
			return Integer.toString(nodeIndex);
		case AND:
			return bitFactory.get(children[0]).describe() + " & "
					+ bitFactory.get(children[1]).describe();
		case XOR:
			return bitFactory.get(children[0]).describe() + " ^ "
					+ bitFactory.get(children[1]).describe();
		case NOT:
			return "!" + bitFactory.get(children[0]).describe();
		}

		return "";
	}

	public String toString() {
		switch (type) {
		case TERMINAL:
			return "=" + nodeIndex;
		case AND:
			return children[0] + " & " + children[1] + " -> " + nodeIndex;
		case XOR:
			return children[0] + " ^ " + children[1] + " -> " + nodeIndex;
		case NOT:
			return "!" + children[0] + " -> " + nodeIndex;
		}

		return "";
	}

	public int[] children() {
		return children;
	}

	public int index() {
		return nodeIndex;
	}

	public void addBackRef(int index) {
		backRefs.add(index);
	}

	public List<Integer> backRefs() {
		return backRefs;
	}

	public LoggingBitNodeType type() {
		return type;
	}
}
