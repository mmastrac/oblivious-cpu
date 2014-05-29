package com.grack.shapecpu.assembler;

import java.util.ArrayList;
import java.util.List;

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
}
