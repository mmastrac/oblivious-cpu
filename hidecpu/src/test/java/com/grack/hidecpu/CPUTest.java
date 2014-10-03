package com.grack.hidecpu;

import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.grack.hidecpu.assembler.Compiler;
import com.grack.hidecpu.assembler.Opcode;
import com.grack.hidecpu.assembler.Parser;
import com.grack.hidecpu.assembler.Program;
import com.grack.homomorphic.graph.Graph;
import com.grack.homomorphic.light.LightBitFactory;
import com.grack.homomorphic.light.StandardStateFactory;
import com.grack.homomorphic.logging.LoggingBitFactory;
import com.grack.homomorphic.logging.LoggingStateFactory;
import com.grack.homomorphic.ops.State;
import com.grack.homomorphic.ops.StateFactory;
import com.grack.homomorphic.ops.Word;

public class CPUTest {
	private Map<String, Object> initialState;

	@Before
	public void setup() throws IOException {
		Parser parser = new Parser(Resources.asCharSource(getClass()
				.getResource("/creditcard.asm"), Charsets.UTF_8));
		Program program = parser.parse();

		// System.out.println(program);

		Compiler compiler = new Compiler();
		compiler.compile(program);

		initialState = new HashMap<>();
		initialState.put("memory", program.getProgram());
	}

	@Test
	public void runTicks3() {
		CPU cpu = new CPU();
		LightBitFactory factory = new LightBitFactory();
		StateFactory stateFactory = new StandardStateFactory(factory,
				initialState, false);
		State state = stateFactory.createState();
		cpu.initialize(factory, stateFactory);
		cpu.tick(state);
		cpu.tick(state);
		cpu.tick(state);
	}

	@Test
	public void runUntilDone() {
		CPU cpu = new CPU();
		LightBitFactory factory = new LightBitFactory();
		StateFactory stateFactory = new StandardStateFactory(factory,
				initialState, true);
		State state = stateFactory.createState();
		cpu.initialize(factory, stateFactory);
		long lastPc = -1;
		for (int i = 0; i < 10000; i++) {
			cpu.tick(state);
			long pc = factory.extract(state.getWordRegister("pc"));
			if (pc == lastPc) {
//				dumpMemory(factory, state);

				System.out.println("XOR count = " + factory.getXorCount());
				System.out.println("AND count = " + factory.getAndCount());
				return;
			}

			lastPc = pc;
		}

		fail();
	}

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

	private void dumpMemory(LightBitFactory factory, State state) {
		Word[] memory = state.getWordArrayRegister("memory");
		for (int j = 0; j < memory.length; j++) {
			long mem = factory.extract(memory[j]) & 0xff;
			System.out.println(String.format("%3d: %s %5d", j,
					memory[j].toString(), mem));
		}
	}

	public static void main(String[] args) {
		boolean[] bools = new boolean[] { false, true };
		for (boolean a : bools) {
			for (boolean b : bools) {
				for (boolean c : bools) {
					System.out.println(a + " " + b + " " + c + ": " + ((a && b) ^ (a && c)) + " " + (a && (b ^ c)));
				}
			}
		}
	}
}
