package com.grack.hidecpu2.assembler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.io.CharSource;
import com.grack.hidecpu2.assembler.line.Line;
import com.grack.hidecpu2.assembler.line.LineBuilder;

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
			lines.add(parseLine(line));
		});

		return program;
	}

	private Line parseLine(String line) {
		try {
			LineBuilder lineBuilder = new LineBuilder();
			if (line.indexOf('#') != -1) {
				lineBuilder.withComment(line.substring(line.indexOf("#") + 1));
				line = line.substring(0, line.indexOf('#'));
			}
			
			line = line.trim();
			if (line.isEmpty())
				return lineBuilder.createEmpty();

			if (line.matches("\\s*[a-zA-Z0-9_]+:.*")) {
				lineBuilder.withLabel(line.substring(0, line.indexOf(':')).trim());
				line = line.substring(line.indexOf(':') + 1);
			}
			
			line = line.trim();
			if (line.isEmpty())
				return lineBuilder.createEmpty();

			if (line.startsWith(".")) {
				return lineBuilder.createSegment(line.substring(1));
			}
			
			Iterator<String> pieces = OPCODE_SPLITTER.split(line).iterator();

			String opcode = pieces.next().toLowerCase();
			String params = pieces.hasNext() ? pieces.next() : null;

			switch (opcode) {
			case "data": {
				List<Value> data = new ArrayList<>();
				for (String datum : PARAM_SPLITTER.split(params)) {
					data.add(parseValue(datum));
				}
				return lineBuilder.createData(data.toArray(new Value[data.size()]));
			}
			case "mov": {
				Iterator<String> it = PARAM_SPLITTER.split(params).iterator();
				String left = it.next();
				String right = it.next();
				OpSource src;
				OpTarget target;
				Value value;
				if (left.startsWith("[")) {
					lineBuilder.withOpcode(Opcode.STORE);
					target = OpTarget.valueOf(right.toUpperCase());
					src = parseSource(left);
					value = parseSourceValue(left);
				} else {
					lineBuilder.withOpcode(Opcode.LOAD);
					target = OpTarget.valueOf(left.toUpperCase());
					src = parseSource(right);
					value = parseSourceValue(right);
				}
				return lineBuilder.createStandard(target, src, value);
			}
			case "swap": {
				Iterator<String> it = PARAM_SPLITTER.split(params).iterator();
				String left = it.next();
				String right = it.next();
				lineBuilder.withOpcode(Opcode.SWAP);
				OpSource src;
				OpTarget target;
				Value value;
				// We're generous with what we allow for swap since it is identical both ways
				if (left.startsWith("[")) {
					target = OpTarget.valueOf(right.toUpperCase());
					src = parseSource(left);
					value = parseSourceValue(left);
				} else {
					target = OpTarget.valueOf(left.toUpperCase());
					src = parseSource(right);
					value = parseSourceValue(right);
				}
				return lineBuilder.createStandard(target, src, value);
			}
			case "jump":
				lineBuilder.withOpcode(Opcode.JUMP);
				return lineBuilder.createStandard(null, parseSource(params), parseSourceValue(params));
			case "clrc":
				lineBuilder.withOpcode(Opcode.SETFLAGS);
				// TODO
				return lineBuilder.createMaskValue(1, 0);
			case "setc":
				lineBuilder.withOpcode(Opcode.SETFLAGS);
				// TODO
				return lineBuilder.createMaskValue(1, 1);
			case "blt":
			case "blte":
			case "beq":
			case "bca":
			case "bgt":
			case "bgte":
			case "bne":
			case "bnc":
				lineBuilder.withOpcode(Opcode.BRA);
				BranchType branchType = BranchType.valueOf(opcode.substring(1)
						.toUpperCase());
				return lineBuilder.createBranch(branchType, parseValue(params));
			case "loop":
			case "cmp":
			case "add":
			case "sub":
			case "and":
			case "xor":
			case "or": {
				lineBuilder.withOpcode(Opcode.valueOf(opcode.toUpperCase()));
				Iterator<String> it = PARAM_SPLITTER.split(params).iterator();
				String left = it.next();
				String right = it.next();
				return lineBuilder.createStandard(OpTarget.valueOf(left.toUpperCase()), parseSource(right),
						parseSourceValue(right));
			}
			case "halt":
				lineBuilder.withOpcode(Opcode.JUMP);
				return lineBuilder.createStandard(null, OpSource.CONSTANT, new Value("pc"));
			default:
				throw new RuntimeException("Invalid opcode: " + opcode);
			}
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
			if (src.matches("r[0-3]"))
				return new Value(OpTarget.values()[src.charAt(1) - '0']);
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
