package com.grack.hidecpu.assembler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Compiler {
	public Compiler() {
	}

	public void compile(Program program) {
		List<Line> lines = program.getLines();

		// Remove all the empty lines
		lines.removeIf((line) -> line.isEmpty());

		// Move labels to a real line (assuming no label on the next line)
		for (int i = 0; i < lines.size(); i++) {
			Line line = lines.get(i);
			String label = line.getLabel();
			if (label != null && line.getOpcode() == null
					&& lines.get(i + 1).getLabel() == null) {
				lines.remove(i);
				lines.get(i).setLabel(label);
				i--;
			}
		}

		Map<String, Integer> indexes = new HashMap<>();

		// Process indexes
		int lineNumber = 0;
		for (Line line : lines) {
			if (line.getLabel() != null) {
				indexes.put(line.getLabel(), lineNumber);
				line.setLabel(null);
			}
			lineNumber += line.size();
		}

		lineNumber = 0;
		for (int i = 0; i < lines.size(); i++) {
			indexes.put("pc", lineNumber);
			updateValue(indexes, lines.get(i).getValue());
			lineNumber += lines.get(i).size();
		}
	}

	private void updateValue(Map<String, Integer> indexes, Value value) {
		if (value == null)
			return;

		if (value.getValue() instanceof String) {
			Integer index = indexes.get(value.getValue());
			if (index == null)
				throw new IllegalStateException("Index not found: "
						+ value.getValue());
			value.setValue(index);
		}
	}
}
