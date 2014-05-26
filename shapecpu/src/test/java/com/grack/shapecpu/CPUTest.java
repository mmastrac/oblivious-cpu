package com.grack.shapecpu;

import static org.junit.Assert.*;

import org.junit.Test;

import com.grack.shapecpu.light.LightNativeBitFactory;

public class CPUTest {
	@Test
	public void cpuTurnsOn() {
		CPU cpu = new CPU(new LightNativeBitFactory());
		cpu.tick();
	}

	@Test
	public void cpuTicksTwice() {
		CPU cpu = new CPU(new LightNativeBitFactory());
		cpu.tick();
		cpu.tick();
	}

	@Test
	public void cpuTicksThrice() {
		CPU cpu = new CPU(new LightNativeBitFactory());
		cpu.tick();
		cpu.tick();
		cpu.tick();
	}
	
	@Test
	public void cpuTicksUntilDone() {
		LightNativeBitFactory factory = new LightNativeBitFactory();
		CPU cpu = new CPU(factory);
		for (int i = 0; i < 10000; i++) {
			cpu.tick();
		}
		
		fail();
	}
}
