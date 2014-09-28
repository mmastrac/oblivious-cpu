package com.grack.hidecpu.assembler;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.io.CharSource;

public class Parser {
	private CharSource source;
	private Splitter LINE_SPLITTER = Splitter.on(CharMatcher.WHITESPACE)
			.trimResults().omitEmptyStrings();

	public Parser(CharSource source) {
		this.source = source;
	}

	public Program parse() throws IOException {
		Program program = new Program();
		List<Line> lines = program.getLines();
		source.readLines().forEach((line) -> {
			lines.add(parseLine(line));
		});

		return program;
	}

	private Line parseLine(String line) {
		if (line.trim().isEmpty())
			return new Line(null, null, null);

		Iterator<String> pieces = LINE_SPLITTER.split(line).iterator();

		String label = null;
		if (!Character.isWhitespace(line.charAt(0))) {
			label = pieces.next();
		}

		String opcode = pieces.next();
		String value = pieces.hasNext() ? pieces.next() : null;

		throw new RuntimeException("TODO");
//		return new Line(label, parseOpcode(opcode), parseValue(value));
	}

	private Value parseValue(String value) {
		if (value == null)
			return null;

		if (value.matches("[0-9]+"))
			return new Value(Integer.parseInt(value));

		return new Value(value);
	}

	private Opcode parseOpcode(String opcode) {
		return Opcode.valueOf(opcode);
	}
}
