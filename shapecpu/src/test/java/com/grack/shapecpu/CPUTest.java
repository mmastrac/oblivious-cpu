package com.grack.shapecpu;

import org.junit.Test;

import com.grack.shapecpu.light.LightNativeBitFactory;

public class CPUTest {
	@Test
	public void cpuTurnsOn() {
		CPU cpu = new CPU(new LightNativeBitFactory());
		cpu.tick();
	}
}
