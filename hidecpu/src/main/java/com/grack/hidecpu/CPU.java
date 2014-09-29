package com.grack.hidecpu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.grack.hidecpu.assembler.Opcode;
import com.grack.homomorphic.engine.Engine;
import com.grack.homomorphic.ops.Bit;
import com.grack.homomorphic.ops.NativeBitFactory;
import com.grack.homomorphic.ops.State;
import com.grack.homomorphic.ops.StateFactory;
import com.grack.homomorphic.ops.Word;
import com.grack.homomorphic.ops.WordAndBit;

public class CPU implements Engine {
	private static final String ALU_CARRY = "alu_carry";
	private static final String ALU_MINUS = "alu_minus";
	private static final String ALU_ZERO = "alu_zero";
	private static final String PC = "pc";
	private static final String MEMORY = "memory";
	private static final int R0 = 254;
	private static final int R1 = 255;

	private static final int MEMORY_WIDTH = 15;

	public CPU() {
	}

	@Override
	public void initialize(NativeBitFactory factory, StateFactory stateFactory) {
		stateFactory.allocateBitRegister(ALU_CARRY);
		stateFactory.allocateBitRegister(ALU_MINUS);
		stateFactory.allocateBitRegister(ALU_ZERO);

		stateFactory.allocateWordRegister(PC, 8);

		stateFactory.allocateWordArrayRegister(MEMORY, MEMORY_WIDTH, 256);
	}

	private Word memoryRead(State state, Word[] memory, Word addr, int size) {
		// Unroll the first time through the loop
		Word b1 = addr.eq(0).and(memory[0].bits(size - 1, 0));

		for (int row = 1; row < memory.length; row++) {
			b1 = b1.xor(addr.eq(row).and(memory[row].bits(size - 1, 0)));
		}

		state.debug("mem", b1, "@", addr);

		return b1;
	}

	/**
	 * Reads or writes a memory address to/from a register, depending on the
	 * state of the write flag.
	 */
	private Word memoryAccess(State state, Word[] memory, Word addr, Word reg,
			Bit write) {
		Bit[] r = new Bit[memory.length];

		// Unroll the first time through the loop
		r[0] = addr.eq(0);
		memory[0] = memory[0].setBits(7, 0,
				(r[0].and(write)).ifThen(reg, memory[0]));
		Word b1 = r[0].and(memory[0].bits(7, 0));

		for (int row = 1; row < memory.length; row++) {
			r[row] = addr.eq(row);
			memory[row] = memory[row].setBits(7, 0,
					(r[row].and(write)).ifThen(reg, memory[row]));
			b1 = b1.xor(r[row].and(memory[row].bits(7, 0)));
		}

		state.debug("b1", b1, addr, reg, write);
		return b1;
	}

	public void tick(State state) {
		Word pc = state.getWordRegister(PC);
		Bit alu_carry = state.getBitRegister(ALU_CARRY);
		Bit alu_minus = state.getBitRegister(ALU_MINUS);
		Bit alu_zero = state.getBitRegister(ALU_ZERO);
		Word[] memory = state.getWordArrayRegister(MEMORY);

		state.debug("***** tick *****");

		state.debug("pc:", pc, "r0:", r0(memory), "r1:", r1(memory));

		Word cmd = memoryRead(state, memory, pc, MEMORY_WIDTH);
		Word cmd_param = cmd.bits(7, 0);
		Word cmd_source = cmd.bits(9, 8);
		Word cmd_target = cmd.bits(10, 10);
		Word cmd_op = cmd.bits(14, 11);
		Word branch_type = cmd.bits(10, 8);

		// Load arg is a constant, a constant read or a register-relative read
		Word addr00 = pc;
		Word addr01 = cmd_param;
		Word addr10 = r0(memory).add(cmd_param);
		Word addr11 = r1(memory).add(cmd_param);

		state.debug("cmd:", cmd);
		state.debug("cmd_param:", cmd_param, "cmd_source:", cmd_source,
				"cmd_target:", cmd_target);

		Word source_arg = memoryRead(state, memory,
				cmd_source.decode(addr00, addr01, addr10, addr11), 8);

		state.debug("addr00:", addr00, "addr01:", addr01, "addr10:", addr10,
				"addr11:", addr11);

		Word target_arg = cmd_target.bit(0).ifThen(r1(memory), r0(memory));

		state.debug("source_arg:", source_arg, "target_arg:", target_arg);

		// Decode using opcode enum
		Map<Opcode, Bit> cmd_ops = new HashMap<>();
		ArrayList<Object> op_debug = new ArrayList<>();
		op_debug.add("Command select:");
		for (Opcode opcode : Opcode.values()) {
			Bit eq = cmd_op.eq(opcode.ordinal());
			cmd_ops.put(opcode, eq);
			op_debug.add(opcode + ":");
			op_debug.add(eq);
		}

		state.debug(op_debug.toArray());

		// CMP/SUB (two's compliment, then add)
		Word b_cmp_sub = source_arg.not().add(state.one()).add(target_arg);

		state.debug("cmp:", b_cmp_sub);

		// ROR
		Bit carry_ror = source_arg.bit(0);
		Word b_ror = source_arg.bits(7, 1);
		b_ror = b_ror.setBit(7, alu_carry);

		// ROL
		Bit carry_rol = source_arg.bit(7);
		Word b_rol = source_arg.bits(6, 0).shl(1);
		b_rol = b_rol.setBit(0, alu_carry);

		// ADD
		WordAndBit add = target_arg.addWithCarry(source_arg, alu_carry);
		Bit carry_add = add.getBit();
		Word b_add = add.getWord();

		state.debug("b_add:", b_add, "carry_add:", carry_add);

		// LOOP
		Word b_loop = target_arg.add(state.negativeOne(8));
		// TODO: could optimize by testing highest bit here
		Bit loop_zero = target_arg.eq(0);

		// Write-back
		// addr00 unused -- can we re-purpose this?
		Word addr = cmd_source.decode(addr01, addr01, addr10, addr11);

		memoryAccess(state, memory, addr, target_arg, cmd_ops.get(Opcode.STORE));

		Bit src_unchanged = cmd_ops.get(Opcode.BRA)
				.xor(cmd_ops.get(Opcode.CARRY)).xor(cmd_ops.get(Opcode.CMP))
				.xor(cmd_ops.get(Opcode.STORE));

		// Update target_arg
		Word target_new;
		target_new = cmd_ops.get(Opcode.LOAD).and(source_arg);
		target_new = target_new.xor(cmd_ops.get(Opcode.ROR).and(b_ror));
		target_new = target_new.xor(cmd_ops.get(Opcode.ROL).and(b_rol));
		target_new = target_new.xor(cmd_ops.get(Opcode.ADD).and(b_add));
		target_new = target_new.xor(cmd_ops.get(Opcode.SUB).and(b_cmp_sub));
		target_new = target_new.xor(cmd_ops.get(Opcode.AND).and(
				source_arg.and(cmd_param)));
		target_new = target_new.xor(cmd_ops.get(Opcode.XOR).and(
				source_arg.xor(cmd_param)));
		target_new = target_new.xor(cmd_ops.get(Opcode.OR).and(
				source_arg.or(cmd_param)));
		target_new = target_new.xor(cmd_ops.get(Opcode.LOOP).and(b_loop));

		r0(memory,
				src_unchanged.ifThen(r0(memory),
						cmd_target.bit(0).ifThen(r0(memory), target_new)));
		r1(memory,
				src_unchanged.ifThen(r1(memory),
						cmd_target.bit(0).ifThen(target_new, r1(memory))));

		alu_zero = cmd_ops.get(Opcode.CMP).xor(cmd_ops.get(Opcode.SUB))
				.ifThen(b_cmp_sub.eq(0), alu_zero);

		alu_minus = cmd_ops.get(Opcode.CMP).xor(cmd_ops.get(Opcode.SUB))
				.ifThen(b_cmp_sub.bit(7), alu_minus);

		alu_carry = cmd_ops.get(Opcode.ADD).ifThen(
				carry_add,
				cmd_ops.get(Opcode.ROL).ifThen(
						carry_rol,
						cmd_ops.get(Opcode.ROR).ifThen(
								carry_ror,
								cmd_ops.get(Opcode.CARRY).ifThen(
										source_arg.bit(0), alu_carry))));

		state.debug("carry:", alu_carry, "minus:", alu_minus, "zero:", alu_zero);

		// Update PC
		// TODO: LFSR
		Word pc_linear = pc.add(state.one());

		Bit bra00 = alu_minus;
		Bit bra01 = alu_minus.or(alu_zero);
		Bit bra10 = alu_zero;
		Bit bra11 = alu_carry;

		state.debug("!", branch_type.bit(0), "lt:", bra00, "lte:", bra01,
				"eq:", bra10, "ca:", bra11);

		Bit bra_success = branch_type.bit(0).xor(
				branch_type.bits(2, 1).decode(bra00, bra01, bra10, bra11));

		pc = cmd_ops.get(Opcode.BRA).ifThen(
				bra_success.ifThen(source_arg, pc_linear),
				cmd_ops.get(Opcode.JUMP).ifThen(
						source_arg,
						cmd_ops.get(Opcode.LOOP).ifThen(
								loop_zero.ifThen(pc_linear, source_arg),
								pc_linear)));

		state.debug("pc:", pc, "r0:", r0(memory), "r1:", r1(memory));

		// Update CPU state
		state.setBitRegister(ALU_CARRY, alu_carry);
		state.setBitRegister(ALU_MINUS, alu_minus);
		state.setBitRegister(ALU_ZERO, alu_zero);
		state.setWordRegister(PC, pc);
		state.setWordArrayRegister(MEMORY, memory);
	}

	private void r0(Word[] memory, Word value) {
		memory[R0] = memory[R0].setBits(7, 0, value);
	}

	private void r1(Word[] memory, Word value) {
		memory[R1] = memory[R1].setBits(7, 0, value);
	}

	private Word r0(Word[] memory) {
		return memory[R0].bits(7, 0);
	}

	private Word r1(Word[] memory) {
		return memory[R1].bits(7, 0);
	}
}
