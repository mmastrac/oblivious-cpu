package com.grack.shapecpu;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.grack.shapecpu.light.LightNativeBitFactory;

public class CPUTest {
	private int[] memory;
	private int[] output;

	@Before
	public void setup() throws IOException {
		String mem = Resources.toString(getClass().getResource("creditcard_input.txt"),
				Charsets.UTF_8);
		String[] memoryContents = mem.trim().split("\n");
		// debug("memory size:", memoryContents.length);
		memory = new int[memoryContents.length];

		for (int i = 0; i < memoryContents.length; i++) {
			String line = memoryContents[i];
			String[] bytes = line.split(" ");
			int value = 0;
			for (int j = 0; j < 13; j++) {
				value |= (Integer.valueOf(bytes[j], 16) & 1) << j;
			}

			// debug(i, String.format("%13s", Integer.toString(value, 2))
			// .replace(" ", "0"));
			memory[i] = value;
		}
		
		String out = Resources.toString(getClass().getResource("creditcard_output.txt"),
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
		CPU cpu = new CPU(new LightNativeBitFactory(), memory, false);
		cpu.tick();
	}

	@Test
	public void cpuTicksTwice() {
		CPU cpu = new CPU(new LightNativeBitFactory(), memory, false);
		cpu.tick();
		cpu.tick();
	}

	@Test
	public void cpuTicksThrice() {
		CPU cpu = new CPU(new LightNativeBitFactory(), memory, false);
		cpu.tick();
		cpu.tick();
		cpu.tick();
	}

	@Test
	public void cpuTicks300() {
		LightNativeBitFactory factory = new LightNativeBitFactory();
		CPU cpu = new CPU(factory, memory, false);
		for (int i = 0; i < 300; i++) {
			cpu.tick();
		}

		System.out.println("XOR count = " + factory.getXorCount());
		System.out.println("AND count = " + factory.getAndCount());
	}

	@Test
	public void cpuTicksUntilDone() {
		LightNativeBitFactory factory = new LightNativeBitFactory();
		CPU cpu = new CPU(factory, memory, false);
		long lastPC = -1;
		for (int i = 0; i < 20000; i++) {
			long pc = factory.extract(cpu.pc);
			if (pc == lastPC) {
				// Success
				System.out.println("Total ticks: " + i);
				for (int j = 0; j < output.length; j++) {
					long mem = factory.extract(cpu.memory[j]) & 0xff;
					if (mem != output[j])
						fail();
//					System.out.println(String.format("%3d: %s %5d", j,
//							cpu.memory[j].toString(),
//							mem));
				}
				System.out.println("All memory locations match.");
				return;
			}
			lastPC = pc;

			cpu.tick();
		}

		fail();
	}
}
