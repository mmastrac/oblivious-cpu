package com.grack.homomorphic.graph;

import java.io.IOException;
import java.io.PrintWriter;

import org.junit.Test;

import com.grack.homomorphic.logging.LoggingBitFactory;
import com.grack.homomorphic.ops.Bit;
import com.grack.homomorphic.ops.Word;
import com.grack.homomorphic.ops.WordAndBit;

public class GraphTest {
	@SuppressWarnings("unused")
	@Test
	public void testOptimize() {
		Graph graph = new Graph();
		InputNode in1, in2;
		graph.addInput(in1 = new InputNode("in1"));
		graph.addInput(in2 = new InputNode("in2"));

		AndNode andNode1 = new AndNode(in1, in2);
		AndNode andNode2 = new AndNode(in1, in2);

		XorNode unused = new XorNode(andNode1, andNode2);
		NotNode unused2 = new NotNode(unused);

		OutputNode out1, out2;
		graph.addOutput(out1 = new OutputNode("out1", andNode1));
		graph.addOutput(out2 = new OutputNode("out2", andNode2));

		graph.optimize();
	}

	@Test
	public void testFullAdd() throws IOException {
		LoggingBitFactory factory = new LoggingBitFactory();
		Bit carryIn = factory.createNamedInputBit("carry");

		Bit in1a = factory.createNamedInputBit("in1a");
		Bit in1b = factory.createNamedInputBit("in1b");
		Bit in1c = factory.createNamedInputBit("in1c");
		Bit in1d = factory.createNamedInputBit("in1d");

		Bit in2a = factory.createNamedInputBit("in2a");
		Bit in2b = factory.createNamedInputBit("in2b");
		Bit in2c = factory.createNamedInputBit("in2c");
		Bit in2d = factory.createNamedInputBit("in2d");

		Word in1 = new Word(new Bit[] { in1a, in1b, in1c, in1d });
		Word in2 = new Word(new Bit[] { in2a, in2b, in2c, in2d });

		WordAndBit result = in1.addWithCarry(in2, carryIn);

		factory.createNamedOutputBit("outa", result.getWord().bit(3));
		factory.createNamedOutputBit("outb", result.getWord().bit(2));
		factory.createNamedOutputBit("outc", result.getWord().bit(1));
		factory.createNamedOutputBit("outd", result.getWord().bit(0));

		factory.createNamedOutputBit("carry", result.getBit());

		Graph graph = factory.toGraph();
		// graph.optimize();

		try (PrintWriter w = new PrintWriter(System.out)) {
			graph.toGraphviz(w);
		}
	}
}
