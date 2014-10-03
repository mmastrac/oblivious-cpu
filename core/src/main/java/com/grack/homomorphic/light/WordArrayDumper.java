package com.grack.homomorphic.light;

import com.grack.homomorphic.ops.Word;

public class WordArrayDumper {
	private static final int RUN_LENGTH = 5;
	private static final String DUMP = "%08x: %s  %s";

	public static void dump(LightBitFactory factory, Word[] memory) {
		long last = -1;
		int run = 0;
		int size = memory[0].size();

		int addr;
		for (addr = 0; addr < memory.length; addr++) {
			long val = factory.extract(memory[addr]);
			if (val == last) {
				run++;
			} else {
				if (run > RUN_LENGTH) {
					System.err.println("          *");
				} else {
					while (run > 0) {
						dumpLine(addr - run, factory, size, last);
						run--;
					}
				}
				run = 0;
				last = val;
				dumpLine(addr, factory, size, val);
			}
		}

		if (run > RUN_LENGTH) {
			System.err.println("          *");
			dumpLine(addr - 1, factory, size, last);
			System.err.println(String.format(DUMP, addr - 1,
					factory.encodeWord(last, size), last, last));
		} else {
			// Dump any remaining items that didn't make it into a run
			while (run > 0) {
				dumpLine(addr - run, factory, size, last);
				run--;
			}
		}
	}

	private static void dumpLine(int addr, LightBitFactory factory, int size,
			long value) {
		System.err.println(String.format(DUMP, addr,
				factory.encodeWord(value, size), value & 0xff));

	}
}
