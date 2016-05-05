package com.grack.hidecpu2.assembler;

import java.util.ArrayList;
import java.util.List;

import org.junit.experimental.theories.ParameterSignature;
import org.junit.experimental.theories.ParameterSupplier;
import org.junit.experimental.theories.PotentialAssignment;

public class AllTests extends ParameterSupplier {
	@Override
	public List<PotentialAssignment> getValueSources(ParameterSignature sig) {
		List<PotentialAssignment> list = new ArrayList<>();
		list.add(PotentialAssignment.forValue("name", "data_indirect.asm"));
		list.add(PotentialAssignment.forValue("name", "opcode_add.asm"));
		list.add(PotentialAssignment.forValue("name", "opcode_branch.asm"));
		list.add(PotentialAssignment.forValue("name", "opcode_load_store.asm"));
		list.add(PotentialAssignment.forValue("name", "opcode_swap.asm"));
		return list;
	}
}
