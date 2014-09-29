package com.grack.hidecpu;


import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.docopt.Docopt;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.grack.hidecpu.assembler.Compiler;
import com.grack.hidecpu.assembler.Opcode;
import com.grack.hidecpu.assembler.Parser;
import com.grack.hidecpu.assembler.Program;
import com.grack.homomorphic.light.LightBitFactory;
import com.grack.homomorphic.light.StandardStateFactory;
import com.grack.homomorphic.ops.State;
import com.grack.homomorphic.ops.Word;

public class Main {
	public static void main(String[] args) throws IOException {
		Map<String, Object> parsed = new Docopt(
				Main.class.getResourceAsStream("/command-line.txt"),
				Charsets.US_ASCII).withExit(true).withHelp(true).parse(args);

		if ((Boolean) parsed.get("run")) {
			int ticks = parsed.get("--ticks") == null ? -1 : Integer
					.parseInt((String) parsed.get("--ticks"));
			run((String) parsed.get("<asm-or-obj-file>"), ticks,
					(Boolean) parsed.get("--debug"));
			return;
		}

		if ((Boolean) parsed.get("assemble")) {
			PrintStream out = (Boolean) parsed.get("-o") ? new PrintStream(
					new File((String) parsed.get("<output-file>")))
					: System.out;
			assemble((String) parsed.get("<asm-file>"), out);
			return;
		}

		if ((Boolean) parsed.get("pretty")) {
			PrintStream out = (Boolean) parsed.get("-o") ? new PrintStream(
					new File((String) parsed.get("<output-file>")))
					: System.out;
			pretty((String) parsed.get("<asm-file>"),
					(Boolean) parsed.get("--process-labels"), out);
			return;
		}
	}

	private static void run(String file, int ticks, boolean debug)
			throws IOException {
		if (ticks == -1)
			System.err.println("Running " + file + " until complete");
		else
			System.err.println("Running " + file + " for " + ticks + " tick(s)");
		
		Map<String, Object> initialState = new HashMap<>();

		if (file.endsWith(".asm")) {
			Parser parser = new Parser(Files.asCharSource(new File(file),
					Charsets.UTF_8));
			Program program = parser.parse();
			Compiler compiler = new Compiler();
			compiler.compile(program);

			initialState.put("pc", 0);
			initialState.put("memory", program.getProgram());
		} else if (file.endsWith(".obj")) {

		} else {
			System.err.println("Only .obj and .asm files may be run");
			System.exit(1);
		}

		CPU cpu = new CPU();
		LightBitFactory factory = new LightBitFactory();
		StandardStateFactory stateFactory = new StandardStateFactory(factory,
				initialState, debug);
		cpu.initialize(factory, stateFactory);

		State state = stateFactory.createState();

		int actualTicks = 0;
		long lastPc = -1;
		for (int i = 0; ticks == -1 ? true : i < ticks; i++) {
			actualTicks++;
			cpu.tick(state);
			long pc = factory.extract(state.getWordRegister("pc"));
			if (ticks == -1 && pc == lastPc) {
				System.err.println("Complete after " + actualTicks + " tick(s)");
				break;
			}
			lastPc = pc;
		}

		Word[] memory = state.getWordArrayRegister("memory");
		for (int i = 0; i < memory.length; i++) {
			if (factory.extract(memory[i].bits(14, 11)) == Opcode.DATA.ordinal()) {
				Word mem = memory[i].and(factory.encodeWord(0xff, 8));
				System.err.println(String.format("%08x: %s  %s", i, mem,
						factory.extract(mem)));
			}
		}

		System.err.println("XOR count = " + factory.getXorCount());
		System.err.println("AND count = " + factory.getAndCount());
	}

	private static void pretty(String file, boolean processLabels,
			PrintStream out) throws IOException {
		System.err.println("Pretty-printing " + file);

		Parser parser = new Parser(Files.asCharSource(new File(file),
				Charsets.UTF_8));
		Program program = parser.parse();

		if (processLabels) {
			Compiler compiler = new Compiler();
			compiler.compile(program);
		}

		program.getLines().forEach((line) -> {
			out.println(line);
		});
	}

	private static void assemble(String file, PrintStream out)
			throws IOException {
		System.err.println("Assembling " + file);

		Parser parser = new Parser(Files.asCharSource(new File(file),
				Charsets.UTF_8));
		Program program = parser.parse();

		Compiler compiler = new Compiler();
		compiler.compile(program);

		program.getLines().forEach((line) -> {
			out.println(encode(15, line.assemble()));
		});
	}

	private static String encode(int bits, int value) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < bits; i++) {
			int out = (int) (Math.random() * 64);
			if ((value & (1 << i)) == 0) {
				out = out & ~1;
			} else {
				out = out | 1;
			}

			builder.append(String.format("%02x", out));
			if (i != bits - 1)
				builder.append(" ");
		}

		return builder.toString();
	}
}
