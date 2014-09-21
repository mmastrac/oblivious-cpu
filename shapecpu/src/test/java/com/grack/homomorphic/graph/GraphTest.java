package com.grack.homomorphic.graph;

import org.junit.Test;

public class GraphTest {
	@Test
	public void testOptimize() {
		Graph graph = new Graph();
		InputNode in1, in2;
		graph.addInput(in1 = new InputNode("in1"));
		graph.addInput(in2 = new InputNode("in2"));
		
		AndNode andNode1 = new AndNode(in1, in2);
		AndNode andNode2 = new AndNode(in1, in2);

		OutputNode out1, out2;
		graph.addOutput(out1 = new OutputNode("out1", andNode1));
		graph.addOutput(out2 = new OutputNode("out2", andNode2));
		
		graph.optimize();
	}
}
