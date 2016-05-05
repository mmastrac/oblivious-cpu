package com.grack.hidecpu2.assembler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.grack.hidecpu2.assembler.line.EmptyLine;
import com.grack.hidecpu2.assembler.line.Line;
import com.grack.hidecpu2.assembler.line.SegmentLine;

public class Compiler {
	public Compiler() {
	}

	public void compile(Program program) {
		Map<String, Integer> indexes = new HashMap<>();
		List<Line> lines = program.getLines();

		String segment = "code";
		
		// Process indexes
		int lineNumber = 0;
		for (Line line : lines) {
			if (line instanceof SegmentLine) {
				SegmentLine segmentLine = (SegmentLine) line;
				segment = segmentLine.getSegment();
				lineNumber = 0;
				continue;
			}
			
			if (line.getLabel() != null) {
				indexes.put(line.getLabel(), lineNumber);
				line.setLabel(null);
			}
			lineNumber += line.size();
		}

		// Remove all the empty lines
		lines.removeIf((line) -> line instanceof EmptyLine);

		segment = "code";
		
		lineNumber = 0;
		for (int i = 0; i < lines.size(); i++) {
			Line line = lines.get(i);
			line.setComment(null);
			if (line instanceof SegmentLine) {
				SegmentLine segmentLine = (SegmentLine) line;
				segment = segmentLine.getSegment();
				lineNumber = 0;
				continue;
			}			
			
			if (segment.equals("code")) {
				indexes.put("pc", lineNumber);
				lineNumber += line.size();
			} else {
				indexes.remove("pc");
			}
			Value[] values = line.getValues();
			if (values != null) {
				for (Value value : values)
					updateValue(indexes, value);
			}
		}
	}

	private void updateValue(Map<String, Integer> indexes, Value value) {
		if (value == null)
			return;

		Object val = value.getValue();
		if (val instanceof String) {
			if (((String)val).matches("r[0-3]"))
				return;
			
			Integer index = indexes.get(val);
			if (index == null)
				throw new IllegalStateException("Index not found: "
						+ val);
			value.setValue(index);
		}
	}
}
