package com.grack.shapecpu;

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
	private static final String AC = "ac";
	private static final String MEMORY = "memory";

	public CPU() {
	}

	@Override
	public void initialize(NativeBitFactory factory, StateFactory stateFactory) {
		stateFactory.allocateBitRegister(ALU_CARRY);
		stateFactory.allocateBitRegister(ALU_MINUS);
		stateFactory.allocateBitRegister(ALU_ZERO);

		stateFactory.allocateWordRegister(AC, 8);
		stateFactory.allocateWordRegister(PC, 8);

		stateFactory.allocateWordArrayRegister(MEMORY, 13, 256);
	}

	private Word memoryRead(State state, Word[] memory, Word addr, int size) {
		// Unroll the first time through the loop
		Word b1 = addr.eq(0).and(memory[0].bits(size - 1, 0));

		for (int row = 1; row < memory.length; row++) {
			b1 = b1.xor(addr.eq(row).and(memory[row].bits(size - 1, 0)));
		}

		state.debug("b1", b1, addr);

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
		memory[0] = memory[0].setBits(7, 0, (r[0].and(write)).ifThen(reg, memory[0]));
		Word b1 = r[0].and(memory[0].bits(7, 0));

		for (int row = 1; row < memory.length; row++) {
			r[row] = addr.eq(row);
			memory[row] = memory[row].setBits(7, 0, (r[row].and(write)).ifThen(reg, memory[row]));
			b1 = b1.xor(r[row].and(memory[row].bits(7, 0)));
		}

		state.debug("b1", b1, addr, reg, write);
		return b1;
	}

	public void tick(State state) {
		Word pc = state.getWordRegister(PC);
		Word ac = state.getWordRegister(AC);
		Bit alu_carry = state.getBitRegister(ALU_CARRY);
		Bit alu_minus = state.getBitRegister(ALU_MINUS);
		Bit alu_zero = state.getBitRegister(ALU_ZERO);
		Word[] memory = state.getWordArrayRegister(MEMORY);

		state.debug("***** tick *****");

		state.debug("pc =", pc);
		state.debug("ac =", ac);

		Word cmd = memoryRead(state, memory, pc, 13);
		Word cmd_param = cmd.bits(7, 0);

		Word load_arg = memoryRead(state, memory, cmd_param, 8);

		state.debug("cmd =", cmd);
		state.debug("load_arg =", load_arg);

		// Bytecode looks like:
		// | address_flag[1] | cmd[4] | data[8] |
		Word cmd_op = cmd.bits(11, 8);

		// Decode
		Bit cmd_store = cmd_op.eq(15); // Store ac to memory
		Bit cmd_load = cmd_op.eq(14); // Load memory to ac
		Bit cmd_rol = cmd_op.eq(12); // Rotate left through alu_carry
		Bit cmd_ror = cmd_op.eq(13); // Rotate right through alu_carry
		Bit cmd_add = cmd_op.eq(11); // Add ac to immediate or indirect
		Bit cmd_clc = cmd_op.eq(10); // Clear carry
		Bit cmd_sec = cmd_op.eq(9); // Set carry
		Bit cmd_xor = cmd_op.eq(8); // XOR ac with immediate
		Bit cmd_and = cmd_op.eq(7); // AND ac with immediate
		Bit cmd_or = cmd_op.eq(6); // OR ac with immediate
		Bit cmd_beq = cmd_op.eq(5); // Branch if alu_zero
		Bit cmd_jmp = cmd_op.eq(4); // Branch unconditionally
		Bit cmd_la = cmd_op.eq(3); // Load indirect
		Bit cmd_bmi = cmd_op.eq(2); // Branch if alu_minus
		Bit cmd_cmp = cmd_op.eq(1); // Compare ac with immediate or
									// indirect

		state.debug("Command select: ", "cmd_op", cmd_op, "store:", cmd_store,
				"load:", cmd_load, "rol:", cmd_rol, "ror:", cmd_ror, "add:",
				cmd_add, "clc:", cmd_clc, "sec:", cmd_sec, "xor:", cmd_xor,
				"and:", cmd_and, "or:", cmd_or, "beq:", cmd_beq, "jmp:",
				cmd_jmp, "la:", cmd_la, "bmi:", cmd_bmi, "cmp:", cmd_cmp);

		// Address?
		Bit cmd_a = cmd.bit(12);

		state.debug("cmd_param:", cmd_param, "cmd_a:", cmd_a);

		// CMP (two's compliment, then add)
		Word b_cmp = cmd_a.ifThen(load_arg, cmd_param).not().add(state.one())
				.add(ac);

		state.debug("cmp:", b_cmp);

		// ROR
		Bit carry_ror = ac.bit(0);
		Word b_ror = ac.shr(1);
		b_ror = b_ror.setBit(7, alu_carry);

		// ROL
		Bit carry_rol = ac.bit(7);
		Word b_rol = ac.bits(6, 0).shl(1, alu_carry);

		// ADD
		WordAndBit add_1 = ac.addWithCarry(cmd_param, alu_carry);
		WordAndBit add_2 = ac.addWithCarry(load_arg, alu_carry);
		Word b_add = cmd_a.ifThen(add_2.getWord(), add_1.getWord());
		Bit carry_add = cmd_a.ifThen(add_2.getBit(), add_1.getBit());

		state.debug("add1:", add_1.getWord(), add_1.getBit(), "add2:",
				add_2.getWord(), add_2.getBit(), "b_add:", b_add, "carry_add",
				carry_add);

		Word load_val = memoryAccess(state, memory, cmd_param, ac, cmd_store);

		Bit ac_unchanged = cmd_sec.xor(cmd_clc).xor(cmd_beq).xor(cmd_bmi)
				.xor(cmd_cmp).xor(cmd_jmp).xor(cmd_store);

		Word ac_new;
		ac_new = cmd_load.and(cmd_param);
		ac_new = ac_new.xor(cmd_ror.and(b_ror));
		ac_new = ac_new.xor(cmd_rol.and(b_rol));
		ac_new = ac_new.xor(cmd_add.and(b_add));
		ac_new = ac_new.xor(cmd_and.and(ac.and(cmd_param)));
		ac_new = ac_new.xor(cmd_xor.and(ac.xor(cmd_param)));
		ac_new = ac_new.xor(cmd_or.and(ac.or(cmd_param)));
		ac_new = ac_new.xor(cmd_la.and(load_val));
		ac_new = ac_new.xor(ac_unchanged.and(ac));

		ac = ac_new;
		state.debug("ac =", ac);

		alu_zero = (cmd_cmp.ifThen(b_cmp.eq(0), ac.eq(0)).or(alu_zero
				.and(cmd_bmi.or(cmd_beq))));

		alu_minus = cmd_cmp.ifThen(b_cmp.bit(7), alu_minus);

		alu_carry = cmd_add.ifThen(
				carry_add,
				cmd_rol.ifThen(carry_rol, cmd_ror.ifThen(
						carry_ror,
						cmd_clc.ifThen(state.zero(),
								cmd_sec.ifThen(state.one(), alu_carry)))));

		state.debug("carry:", alu_carry, "minus:", alu_minus, "zero:", alu_zero);

		Word pc_linear = pc.add(state.one());

		pc = cmd_beq.ifThen(
				alu_zero.ifThen(cmd_param, pc_linear),
				cmd_bmi.ifThen(alu_minus.ifThen(cmd_param, pc_linear),
						cmd_jmp.ifThen(cmd_param, pc_linear)));

		// Update CPU state
		state.setBitRegister(ALU_CARRY, alu_carry);
		state.setBitRegister(ALU_MINUS, alu_minus);
		state.setBitRegister(ALU_ZERO, alu_zero);
		state.setWordRegister(AC, ac);
		state.setWordRegister(PC, pc);
		state.setWordArrayRegister(MEMORY, memory);
	}
}
