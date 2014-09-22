package com.grack.shapecpu;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.grack.shapecpu.assembler.Compiler;
import com.grack.shapecpu.assembler.Parser;
import com.grack.shapecpu.assembler.Program;

public class Main {
	public static void main(String[] args) throws ParseException {
		Options options = new Options();
		options.addOption("processlabels", false, "Process label references");
		options.addOption("o", true,
				"Output file (if not specified, the output is written to the console)");
		CommandLineParser parser = new GnuParser();
		try {
			CommandLine line = parser.parse(options, args);
			String[] cmd = line.getArgs();
			
			if (cmd.length < 1)
				throw new ParseException("Command expected");
			
			PrintStream out = line.getOptionValue("o") == null ? System.out
					: new PrintStream(new File(line.getOptionValue("o")));

			switch (cmd[0]) {
			case "pretty":
				pretty(cmd[1], line.hasOption("processlabels"), out);
				break;
			case "assemble":
				assemble(cmd[1], out);
				break;
			case "run":
				run(cmd[1]);
				break;
			}
		} catch (ParseException e) {
			System.err.println(e.getMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("shapecpu.jar", options);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void run(String file) {

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

		out.println("INITAC " + program.getInitAc());
		out.println("INITPC " + program.getInitPc());
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

		out.println("INITAC "
				+ encode(13, (int) program.getInitAc().getValue()));
		out.println("INITPC " + encode(8, (int) program.getInitPc().getValue()));
		program.getLines().forEach((line) -> {
			out.println(encode(13, line.assemble()));
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
