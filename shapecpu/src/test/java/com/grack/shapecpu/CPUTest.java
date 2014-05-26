package com.grack.shapecpu;

import static org.junit.Assert.*;

import org.junit.Test;

import com.grack.shapecpu.light.LightNativeBitFactory;

public class CPUTest {
	@Test
	public void cpuTurnsOn() {
		CPU cpu = new CPU(new LightNativeBitFactory(), true);
		cpu.tick();
	}

	@Test
	public void cpuTicksTwice() {
		CPU cpu = new CPU(new LightNativeBitFactory(), true);
		cpu.tick();
		cpu.tick();
	}

	@Test
	public void cpuTicksThrice() {
		CPU cpu = new CPU(new LightNativeBitFactory(), true);
		cpu.tick();
		cpu.tick();
		cpu.tick();
	}
	
	@Test
	public void cpuTicksUntilDone() {
		LightNativeBitFactory factory = new LightNativeBitFactory();
		CPU cpu = new CPU(factory, false);
		long lastPC = -1;
		for (int i = 0; i < 20000; i++) {
			long pc = factory.extract(cpu.pc);
			if (pc == lastPC) {
				// Success
				System.out.println("Total ticks: " + i);
				for (int j = 0; j < 132; j++) {
					System.out.println(String.format("%3d: %s %5d", j, cpu.memory[j].toString(), factory.extract(cpu.memory[j]) & 0xff));
				}
				return;
			}
			lastPC = pc;
			
			cpu.tick();
		}
		
		fail();
	}
}
