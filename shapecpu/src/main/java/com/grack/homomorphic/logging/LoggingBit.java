package com.grack.homomorphic.logging;

import java.util.ArrayList;
import java.util.List;

import com.grack.homomorphic.ops.NativeBit;

public class LoggingBit implements NativeBit {
	private LoggingBitFactory bitFactory;
	private LoggingBitNodeType type;
	private int[] children;
	private int nodeIndex;
	private ArrayList<Integer> backRefs = new ArrayList<>();
	private String name;

	public LoggingBit(LoggingBitFactory bitFactory, String name,
			LoggingBitNodeType type, int[] children) {
		this.bitFactory = bitFactory;
		this.name = name;
		this.type = type;
		this.children = children;
		this.nodeIndex = bitFactory.register(this);
	}

	@Override
	public NativeBit xor(NativeBit n) {
		return bitFactory.create(null, LoggingBitNodeType.XOR, new int[] {
				this.nodeIndex, ((LoggingBit) n).nodeIndex });
	}

	@Override
	public NativeBit and(NativeBit n) {
		return bitFactory.create(null, LoggingBitNodeType.AND, new int[] {
				this.nodeIndex, ((LoggingBit) n).nodeIndex });
	}

	@Override
	public NativeBit not() {
		return bitFactory.create(null, LoggingBitNodeType.NOT,
				new int[] { this.nodeIndex });
	}

	public String describe() {
		switch (type) {
		case INPUT:
			return Integer.toString(nodeIndex);
		case OUTPUT:
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
		case INPUT:
			return "IN: " + bitFactory.nameOf(nodeIndex);
		case OUTPUT:
			return "OUT: " + bitFactory.nameOf(nodeIndex) + " = " + bitFactory.nameOf(children[0]);
		case AND:
			return bitFactory.nameOf(nodeIndex) + " = "
					+ bitFactory.nameOf(children[0]) + " & "
					+ bitFactory.nameOf(children[1]);
		case XOR:
			return bitFactory.nameOf(nodeIndex) + " = "
					+ bitFactory.nameOf(children[0]) + " ^ "
					+ bitFactory.nameOf(children[1]);
		case NOT:
			return bitFactory.nameOf(nodeIndex) + " = !"
					+ bitFactory.nameOf(children[0]);
		}

		return "";
	}

	public int[] children() {
		return children;
	}

	public int index() {
		return nodeIndex;
	}

	public String name() {
		return name;
	}

	public void name(String name) {
		if (this.name != null)
			throw new IllegalStateException("This node already has a name");
		this.name = name;
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
