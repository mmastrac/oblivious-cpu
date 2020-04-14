package com.grack.hidecpu2.assembler;

import java.util.ArrayList;
import java.util.List;

import com.google.common.primitives.Ints;
import com.grack.hidecpu2.assembler.line.Line;
import com.grack.hidecpu2.assembler.line.SegmentLine;

public class Program {
	private List<Line> lines = new ArrayList<>();
	
	public List<Line> getLines() {
		return lines;
	}
	
	public int[] getProgram(String whichSegment) {
		ArrayList<Integer> program = new ArrayList<Integer>();
		String segment = "code";
		for (Line line : lines) {
			if (line instanceof SegmentLine) {
				segment = ((SegmentLine) line).getSegment();
				continue;
			}
			if (!segment.equals(whichSegment)) {
				continue;
			}
			for (int i : line.assemble())
				program.add(i);
		}
		
		return Ints.toArray(program);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		int lineNumber = 0;
		for (Line line : lines) {
			if (line instanceof SegmentLine)
				lineNumber = 0;
			builder.append(line.toString(lineNumber));
			lineNumber += line.size();
			builder.append('\n');
		}
		
		return builder.toString();
	}
}
