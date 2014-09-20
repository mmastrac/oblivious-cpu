package com.grack.shapecpu;

import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.grack.shapecpu.light.LightNativeBitFactory;
import com.grack.shapecpu.logging.LoggingBitFactory;

public class CPUTest {
	private int[] memory;
	private int[] output;

	@Before
	public void setup() throws IOException {
		String mem = Resources.toString(
				getClass().getResource("creditcard_input.txt"), Charsets.UTF_8);
		String[] memoryContents = mem.trim().split("\n");
		// debug("memory size:", memoryContents.length);
		memory = new int[256];

		for (int i = 0; i < memoryContents.length; i++) {
			String line = memoryContents[i];
			String[] bytes = line.split(" ");
			int value = 0;
			for (int j = 0; j < 13; j++) {
				value |= (Integer.valueOf(bytes[j], 16) & 1) << j;
			}

			// System.out.println(i + " " + String.format("%13s",
			// Integer.toString(value, 2))
			// .replace(" ", "0"));
			memory[i] = value;
		}

		String out = Resources
				.toString(getClass().getResource("creditcard_output.txt"),
						Charsets.UTF_8);
		String[] outputContents = out.trim().split("\n");

		output = new int[outputContents.length];

		for (int i = 0; i < outputContents.length; i++) {
			String[] bits = outputContents[i].trim().split("  ");
			output[i] = Integer.valueOf(bits[1]);
		}
	}

	@Test
	public void cpuTurnsOn() {
		LightNativeBitFactory factory = new LightNativeBitFactory();
		NativeStateFactory stateFactory = new NativeStateFactory(factory);
		CPU cpu = new CPU(factory, stateFactory, memory, false);
		State state = stateFactory.createState();
		cpu.tick(state);
	}

	@Test
	public void cpuLogging() throws FileNotFoundException, IOException {
		LoggingBitFactory factory = new LoggingBitFactory();
		NativeStateFactory stateFactory = new NativeStateFactory(factory);
		CPU cpu = new CPU(factory, stateFactory, memory, false);
		State state = stateFactory.createState();
		cpu.tick(state);

		Bit bit = state.getBitRegister("alu_carry");
//		System.out.println(((LoggingBit) bit.nativeBit()).describe());

		try (Writer w = new OutputStreamWriter(new FileOutputStream(
				"/tmp/output.txt"))) {
			w.write("digraph G {\n");
			for (int i = 0; i < factory.nodeCount(); i++) {
				String s = factory.get(i).toString();
				if (s.length() > 0) {
					w.write(s);
					w.write(';');
					w.write('\n');
				}
			}
			w.write("}\n");
		}
	}

	@Test
	public void cpuTicksTwice() {
		LightNativeBitFactory factory = new LightNativeBitFactory();
		NativeStateFactory stateFactory = new NativeStateFactory(factory);
		CPU cpu = new CPU(factory, stateFactory, memory, false);
		State state = stateFactory.createState();
		cpu.tick(state);
		cpu.tick(state);
	}

	@Test
	public void cpuTicksThrice() {
		LightNativeBitFactory factory = new LightNativeBitFactory();
		NativeStateFactory stateFactory = new NativeStateFactory(factory);
		CPU cpu = new CPU(factory, stateFactory, memory, false);
		State state = stateFactory.createState();
		cpu.tick(state);
		cpu.tick(state);
		cpu.tick(state);
	}

	@Test
	public void cpuTicks300() {
		LightNativeBitFactory factory = new LightNativeBitFactory();
		NativeStateFactory stateFactory = new NativeStateFactory(factory);
		CPU cpu = new CPU(factory, stateFactory, memory, false);
		State state = stateFactory.createState();
		for (int i = 0; i < 300; i++) {
			cpu.tick(state);
		}

		System.out.println("XOR count = " + factory.getXorCount());
		System.out.println("AND count = " + factory.getAndCount());
	}

	@Test
	public void cpuTicksUntilDone() {
		LightNativeBitFactory factory = new LightNativeBitFactory();
		NativeStateFactory stateFactory = new NativeStateFactory(factory);
		CPU cpu = new CPU(factory, stateFactory, memory, false);
		State state = stateFactory.createState();

		long lastPC = -1;
		for (int i = 0; i < 20000; i++) {
			long pc = factory.extract(state.getWordRegister("pc"));
			if (pc == lastPC) {
				// Success
				System.out.println("Total ticks: " + i);
				for (int j = 0; j < output.length; j++) {
					long mem = factory.extract(state
							.getWordArrayRegister("memory")[j]) & 0xff;
					if (mem != output[j])
						fail();
					// System.out.println(String.format("%3d: %s %5d", j,
					// cpu.memory[j].toString(),
					// mem));
				}
				System.out.println("All memory locations match.");
				return;
			}
			lastPC = pc;

			cpu.tick(state);
		}

		fail();
	}
}
