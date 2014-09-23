package com.grack.shapecpu.assembler;

import java.util.ArrayList;
import java.util.List;

import com.google.common.primitives.Ints;

public class Program {
	private Value initAc;
	private Value initPc;
	
	private List<Line> lines = new ArrayList<>();
	
	public Value getInitAc() {
		return initAc;
	}
	
	public void setInitAc(Value initAc) {
		this.initAc = initAc;
	}
	
	public Value getInitPc() {
		return initPc;
	}
	
	public void setInitPc(Value initPc) {
		this.initPc = initPc;
	}
	
	public List<Line> getLines() {
		return lines;
	}
	
	public int[] getProgram() {
		ArrayList<Integer> program = new ArrayList<Integer>();
		for (Line line : lines) {
			if (line.getOpcode() != null)
				program.add(line.assemble());
		}
		
		return Ints.toArray(program);
	}
}
