package com.grack.homomorphic.graph;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

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

		assertEquals(3, graph.maxDepth());
		graph.optimize();
		assertEquals(2, graph.maxDepth());
	}

	@Test
	public void testFullAdd() throws IOException {
		Graph graph = createAdditionGraph();
		assertEquals(14, graph.maxDepth());
		graph.optimize();
		assertEquals(14, graph.maxDepth());

		try (PrintWriter w = new PrintWriter(System.out)) {
			graph.toGraphviz(w);
		}
	}

	@Test
	public void testVisitOut() {
		Graph graph = createAdditionGraph();

		Set<Node> nodes = new HashSet<>();
		
		graph.visitOut((node) -> {
			nodes.add(node);
			return true;
		}, null);
		
		assertEquals(graph.nodeCount(), nodes.size());
	}

	@Test
	public void testVisitIn() {
		Graph graph = createAdditionGraph();

		Set<Node> nodes = new HashSet<>();
		
		graph.visitIn((node) -> {
			nodes.add(node);
			return true;
		}, null);
		
		assertEquals(graph.nodeCount(), nodes.size());
	}

	@Test
	public void testVisitTopographical() {
		Graph graph = createAdditionGraph();

		Set<Node> nodes = new HashSet<>();
		
		// Should visit once and only once
		graph.visitTopographical((node) -> {
			assertTrue(nodes.add(node));
		});
		
		assertEquals(graph.nodeCount(), nodes.size());
	}

	private Graph createAdditionGraph() {
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
		return factory.toGraph();
	}
}
