package com.grack.hidecpu2.assembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;

import org.junit.Test;
import org.junit.experimental.theories.ParametersSuppliedBy;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import com.google.common.base.Charsets;
import com.google.common.io.CharSource;
import com.google.common.io.Resources;
import com.grack.hidecpu2.CPU;
import com.grack.homomorphic.graph.Graph;
import com.grack.homomorphic.light.LightBitFactory;
import com.grack.homomorphic.light.StandardStateFactory;
import com.grack.homomorphic.logging.LoggingBitFactory;
import com.grack.homomorphic.logging.LoggingStateFactory;
import com.grack.homomorphic.ops.State;
import com.grack.homomorphic.ops.StateFactory;

@RunWith(Theories.class)
public class RunTest {
	@Test
	public void cpuLogging() throws FileNotFoundException, IOException {
		LoggingBitFactory factory = new LoggingBitFactory();
		LoggingStateFactory stateFactory = new LoggingStateFactory(factory);
		CPU cpu = new CPU();
		cpu.initialize(factory, stateFactory);
		State state = stateFactory.createState();
		cpu.tick(state);

		Graph graph = factory.toGraph();
		graph.optimize();

		try (Writer w = new OutputStreamWriter(new FileOutputStream(
				"/tmp/output.txt"))) {
			graph.toC(w);
		}
	}

	/**
	 * Attempt to parse all the samples.
	 */
	@Theory
	public void runFile(@ParametersSuppliedBy(AllTests.class) String file) throws IOException {
		CharSource src = Resources.asCharSource(getClass().getResource("/" + file), Charsets.UTF_8);
		Parser parser = new Parser(src);
		Program program = parser.parse();
		Compiler compiler = new Compiler();
		compiler.compile(program);
		HashMap<String, Object> initialState = new HashMap<>();
		initialState.put(CPU.MEMORY_CODE, program.getProgram("code"));
		initialState.put(CPU.MEMORY_DATA, program.getProgram("data"));

		CPU cpu = new CPU();
		LightBitFactory factory = new LightBitFactory();
		StateFactory stateFactory = new StandardStateFactory(factory,
				initialState, false);
		State state = stateFactory.createState();
		cpu.initialize(factory, stateFactory);
		long lastPc = -1;
		for (int i = 0; i < 1000; i++) {
			cpu.tick(state);
			long pc = factory.extract(state.getWordRegister("pc"));
			if (pc == lastPc) {
//				dumpMemory(factory, state);
				assertEquals("Invalid output in " + file, 99, factory.extract(state.getWordArrayRegister(CPU.MEMORY_DATA)[0]));
				System.out.println("XOR count = " + factory.getXorCount());
				System.out.println("AND count = " + factory.getAndCount());
				return;
			}

			lastPc = pc;
		}

		fail("Ran 10,000 cycles in " + file);
	}

}
