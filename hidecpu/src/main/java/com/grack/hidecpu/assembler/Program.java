package com.grack.hidecpu.assembler;

import java.util.ArrayList;
import java.util.List;

import com.google.common.primitives.Ints;

public class Program {
	private List<Line> lines = new ArrayList<>();
	
	public List<Line> getLines() {
		return lines;
	}
	
	public int[] getProgram() {
		ArrayList<Integer> program = new ArrayList<Integer>();
		for (Line line : lines) {
			if (line.getOpcode() != null)
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
			if (line.getOpcode() == null)
				builder.append(line);
			else
				builder.append(line.toString(lineNumber++));
			builder.append('\n');
		}
		
		return builder.toString();
	}
}
