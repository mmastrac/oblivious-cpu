package com.grack.homomorphic.logging;

import java.util.ArrayList;

import com.grack.homomorphic.graph.AndNode;
import com.grack.homomorphic.graph.ConstantNode;
import com.grack.homomorphic.graph.Graph;
import com.grack.homomorphic.graph.InputNode;
import com.grack.homomorphic.graph.Node;
import com.grack.homomorphic.graph.NotNode;
import com.grack.homomorphic.graph.OutputNode;
import com.grack.homomorphic.graph.XorNode;
import com.grack.homomorphic.ops.Bit;
import com.grack.homomorphic.ops.NativeBit;
import com.grack.homomorphic.ops.NativeBitFactory;
import com.grack.homomorphic.ops.Word;

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
		return idx;
	}

	public NativeBit create(String name, LoggingBitNodeType type, int[] children) {
		return new LoggingBit(this, name, type, children);
	}

	public String nameOf(int idx) {
		LoggingBit bit = get(idx);
		if (bit.name() != null)
			return bit.name();
		
		return Integer.toString(bit.index());
	}
	
	public Graph toGraph() {
		Graph graph = new Graph();
		ArrayList<Node> nodes = new ArrayList<>();
		for (LoggingBit bit : bits) {
			switch (bit.type()) {
			case INPUT: {
				InputNode inputNode;
				if (bit.name().equals("zero"))
					inputNode = new ConstantNode("zero", 0);
				else if (bit.name().equals("one"))
					inputNode = new ConstantNode("one", 1);
				else
					inputNode = new InputNode(bit.name());
				graph.addInput(inputNode);
				nodes.add(inputNode);
				break;
			}
			case OUTPUT: {
				OutputNode outputNode = new OutputNode(bit.name(), nodes.get(bit.children()[0]));
				graph.addOutput(outputNode);
				nodes.add(outputNode);
				break;
			}
			case AND: {
				AndNode andNode = new AndNode(nodes.get(bit.children()[0]), nodes.get(bit.children()[1]));
				nodes.add(andNode);
				break;
			}
			case XOR: {
				XorNode xorNode = new XorNode(nodes.get(bit.children()[0]), nodes.get(bit.children()[1]));
				nodes.add(xorNode);
				break;
			}
			case NOT: {
				NotNode notNode = new NotNode(nodes.get(bit.children()[0]));
				nodes.add(notNode);
			}
			}
		}
		return graph;
	}
}
