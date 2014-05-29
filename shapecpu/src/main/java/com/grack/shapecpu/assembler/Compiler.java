package com.grack.shapecpu.assembler;

import java.util.HashMap;
import java.util.Map;

public class Compiler {
	public Compiler() {
	}

	public void compile(Program program) {
		// Remove all the empty lines
		program.getLines().removeIf((line) -> line.isEmpty());

		Map<String, Integer> indexes = new HashMap<>();

		// Process indexes
		int lineNumber = 0;
		for (Line line : program.getLines()) {
			if (line.getLabel() != null) {
				indexes.put(line.getLabel(), lineNumber);
				line.setLabel(null);
			}
			lineNumber++;
		}

		updateValue(indexes, program.getInitAc());
		updateValue(indexes, program.getInitPc());

		program.getLines().forEach(
				(line) -> updateValue(indexes, line.getValue()));
	}

	private void updateValue(Map<String, Integer> indexes, Value value) {
		if (value == null)
			return;
		
		if (value.getValue() instanceof String)
			value.setValue(indexes.get(value.getValue()));
	}
}
