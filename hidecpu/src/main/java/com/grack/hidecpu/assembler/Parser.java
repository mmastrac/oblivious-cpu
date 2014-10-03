package com.grack.hidecpu.assembler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.io.CharSource;

public class Parser {
	private CharSource source;
	private Splitter OPCODE_SPLITTER = Splitter.on(CharMatcher.WHITESPACE)
			.trimResults().omitEmptyStrings().limit(2);
	private Splitter PARAM_SPLITTER = Splitter.on(',').trimResults()
			.omitEmptyStrings();

	public Parser(CharSource source) {
		this.source = source;
	}

	public Program parse() throws IOException {
		Program program = new Program();
		List<Line> lines = program.getLines();
		source.readLines().forEach((line) -> {
			lines.addAll(parseLine(line));
		});

		return program;
	}

	private List<Line> parseLine(String line) {
		try {
			if (line.indexOf('#') != -1)
				line = line.substring(0, line.indexOf('#'));

			line = line.trim();
			if (line.isEmpty())
				return Arrays.asList(new Line(null, null, null));

			String label = null;
			if (line.matches("^[a-zA-Z0-9_]+:$")) {
				label = line.substring(0, line.indexOf(':'));
				line = line.substring(line.indexOf(':') + 1);
			}

			line = line.trim();
			if (line.isEmpty())
				return Arrays.asList(new Line(label));

			Iterator<String> pieces = OPCODE_SPLITTER.split(line).iterator();

			String opcode = pieces.next().toLowerCase();
			String params = pieces.hasNext() ? pieces.next() : null;

			Opcode op;
			OpSource src = null;
			OpTarget target = null;
			Value value = null;

			switch (opcode) {
			case "data": {
				List<Line> lines = new ArrayList<>();
				for (String data : PARAM_SPLITTER.split(params)) {
					lines.add(new Line(parseValue(data)));
				}
				return lines;
			}
			case "mov": {
				Iterator<String> it = PARAM_SPLITTER.split(params).iterator();
				String left = it.next();
				String right = it.next();
				if (left.startsWith("[")) {
					op = Opcode.STORE;
					target = OpTarget.valueOf(right.toUpperCase());
					src = parseSource(left);
					value = parseSourceValue(left);
				} else {
					if (right.startsWith("rol")) {
						op = Opcode.ROL;
						right = right.substring(3).trim();
					} else if (right.startsWith("ror")) {
						op = Opcode.ROR;
						right = right.substring(3).trim();
					} else if (right.startsWith("not")) {
						op = Opcode.NOT;
						right = right.substring(3).trim();
					} else {
						op = Opcode.LOAD;
					}
					target = OpTarget.valueOf(left.toUpperCase());
					src = parseSource(right);
					value = parseSourceValue(right);
				}
				break;
			}
			case "jump":
				op = Opcode.JUMP;
				src = parseSource(params);
				value = parseSourceValue(params);
				break;
			case "clc":
				op = Opcode.CARRY;
				value = new Value(0);
				break;
			case "sec":
				op = Opcode.CARRY;
				value = new Value(1);
				break;
			case "blt":
			case "blte":
			case "beq":
			case "bca":
			case "bgt":
			case "bgte":
			case "bne":
			case "bnc":
				op = Opcode.BRA;
				BranchType branchType = BranchType.valueOf(opcode.substring(1)
						.toUpperCase());
				return Arrays.asList(new Line(label, Opcode.BRA, branchType,
						parseValue(params)));
			case "loop":
			case "cmp":
			case "add":
			case "sub":
			case "and":
			case "xor":
			case "or": {
				op = Opcode.valueOf(opcode.toUpperCase());
				Iterator<String> it = PARAM_SPLITTER.split(params).iterator();
				String left = it.next();
				String right = it.next();
				target = OpTarget.valueOf(left.toUpperCase());
				src = parseSource(right);
				value = parseSourceValue(right);
				break;
			}
			case "halt":
				// TODO: This could be cleaner
				return Arrays.asList(new Line(null, Opcode.JUMP,
						OpSource.CONSTANT, null, new Value("pc")));
			default:
				throw new RuntimeException("Invalid opcode: " + opcode);
			}

			Line result = new Line(label, op, src, target, value);
			return Arrays.asList(result);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Failed to parse '" + line + "'", e);
		}
	}

	private Value parseSourceValue(String src) {
		src = src.replaceAll("\\s+", "");
		if (src.startsWith("[")) {
			if (src.contains("+")) {
				if (src.startsWith("[r0+") || src.startsWith("[r1+"))
					return parseValue(src.substring(4, src.length() - 1));
				if (src.endsWith("+r0]") || src.endsWith("+r1]"))
					return parseValue(src.substring(1, src.length() - 4));
				throw new IllegalArgumentException("Unexpected relative load: "
						+ src);
			} else {
				if (src.equals("[r0]") || src.equals("[r1]"))
					return new Value(0);
				
				return parseValue(src.substring(1, src.length() - 1));
			}
		} else {
			// Register load
			if (src.equals("r0"))
				return new Value(255);
			if (src.equals("r1"))
				return new Value(254);
			if (src.equals("r2"))
				return new Value(253);
			if (src.equals("r3"))
				return new Value(252);
			return parseValue(src);
		}
	}

	private OpSource parseSource(String src) {
		src = src.replaceAll("\\s+", "");
		if (src.startsWith("[")) {
			if (src.contains("+")) {
				src = src.toLowerCase();

				if (src.startsWith("[r0+") || src.endsWith("+r0]"))
					return OpSource.R0_RELATIVE_LOAD;
				if (src.startsWith("[r1+") || src.endsWith("+r1]"))
					return OpSource.R1_RELATIVE_LOAD;
				throw new IllegalArgumentException("Unexpected relative load: "
						+ src);
			} else {
				if (src.equals("[r0]"))
					return OpSource.R0_RELATIVE_LOAD;
				if (src.equals("[r1]"))
					return OpSource.R1_RELATIVE_LOAD;
				return OpSource.CONSTANT_LOAD;
			}
		} else {
			if (src.equals("r0") || src.equals("r1") || src.equals("r2") || src.equals("r3"))
				return OpSource.CONSTANT_LOAD;

			return OpSource.CONSTANT;
		}
	}

	private Value parseValue(String value) {
		if (value == null)
			return null;

		if (value.equals("r0") || value.equals("r1"))
			throw new IllegalArgumentException("Illegal value name: " + value);

		if (value.matches("[0-9]+"))
			return new Value(Integer.parseInt(value));

		return new Value(value);
	}
}
