package com.grack.hidecpu2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.grack.hidecpu2.assembler.Opcode;
import com.grack.hidecpu2.assembler.UnaryOpcode;
import com.grack.homomorphic.engine.Engine;
import com.grack.homomorphic.ops.Bit;
import com.grack.homomorphic.ops.NativeBitFactory;
import com.grack.homomorphic.ops.State;
import com.grack.homomorphic.ops.StateFactory;
import com.grack.homomorphic.ops.Word;
import com.grack.homomorphic.ops.WordAndBit;

/**
 * Implementation of HideCPU.
 * 
 * Memory is arranged in 8-bit bytes, instructions are two bytes wide.
 * Instruction fetch is done 16-bits at a time, while all other memory accesses
 * are byte-wise.
 */
public class CPU implements Engine {
	public static final String MEMORY_DATA = "memory_data";
	public static final String MEMORY_CODE = "memory_code";
	public static final String MEMORY_REG = "memory_reg";

	private static final int MEMORY_WIDTH = 8;
	private static final int REGISTER_WIDTH = 8;

	public CPU() {
	}

	@Override
	public void initialize(NativeBitFactory factory, StateFactory stateFactory) {
		stateFactory.allocateWordArrayRegister(MEMORY_REG, REGISTER_WIDTH, 8);
		stateFactory.allocateWordArrayRegister(MEMORY_CODE, MEMORY_WIDTH, 256);
		stateFactory.allocateWordArrayRegister(MEMORY_DATA, MEMORY_WIDTH, 256);
	}

	/**
	 * Reads a 16-bit chunk of memory, assumes that addr is 16-bit aligned.
	 */
	private Word memoryRead16(State state, Word[] memory, Word addr) {
		// Unroll the first time through the loop
		Word b1 = addr.eq(0).and(memory[0].bits(7, 0).concat(memory[1].bits(7, 0)));

		for (int row = 2; row < memory.length; row += 2) {
			b1 = b1.xor(addr.eq(row).and(
					memory[row].bits(7, 0).concat(memory[row + 1].bits(7, 0))));
		}

		state.debug("mem16", b1, "@", addr);

		return b1;
	}

	private Word memoryRead8(State state, Word[] memory, Word addr) {
		// Unroll the first time through the loop
		Word b1 = addr.eq(0).and(memory[0].bits(7, 0));

		for (int row = 1; row < memory.length; row++) {
			b1 = b1.xor(addr.eq(row).and(memory[row].bits(7, 0)));
		}

		state.debug("mem", b1, "@", addr);

		return b1;
	}

	/**
	 * Optionally writes to a memory address, depending on the state of the write flag.
	 */
	private void memoryWrite8(State state, Word[] memory, Word addr, Word reg,
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
	}

	public void tick(State state) {
		Word pc = state.getWordRegister(PC);
		Bit alu_carry = state.getBitRegister(ALU_CARRY);
		Bit alu_minus = state.getBitRegister(ALU_MINUS);
		Bit alu_zero = state.getBitRegister(ALU_ZERO);
		Word[] data = state.getWordArrayRegister(MEMORY_DATA);
		Word[] code = state.getWordArrayRegister(MEMORY_CODE);

		state.debug("***** tick *****");

		state.debug("pc:", pc, "r0:", r0(data), "r1:", r1(data), "r2:",
				r2(data), "r3:", r3(data));

		Word cmd = memoryRead16(state, code, pc);
		Word cmd_param = cmd.bits(7, 0);
		Word cmd_source = cmd.bits(9, 8);
		Word cmd_target = cmd.bits(11, 10);
		Word cmd_op = cmd.bits(15, 12);
		Word branch_type = cmd.bits(10, 8);
		Word sub_op = cmd_param.bits(3, 0);

		// Load arg is a constant, a constant read or a register-relative read
		Word addr00 = cmd_param; // we already have this read, but we need to read from somewhere
		Word addr01 = cmd_param;
		Word addr10 = r0(data).add(cmd_param);
		Word addr11 = r1(data).add(cmd_param);

		state.debug("cmd:", cmd);

		// Decode using opcode enum
		Map<Opcode, Bit> cmd_ops = new HashMap<>();
		ArrayList<Object> op_debug = new ArrayList<>();
		op_debug.add("Opcode select:");
		for (Opcode opcode : Opcode.values()) {
			Bit eq = cmd_op.eq(opcode.ordinal());
			cmd_ops.put(opcode, eq);
			op_debug.add(opcode + ":");
			op_debug.add(eq);
		}

		state.debug(op_debug.toArray());
		
		Map<UnaryOpcode, Bit> cmd_sub_ops = new HashMap<>();
		op_debug.add("Sub-opcode select:");
		for (UnaryOpcode subop : UnaryOpcode.values()) {
			Bit eq = sub_op.eq(subop.ordinal());
			cmd_sub_ops.put(subop,  eq);
			op_debug.add(subop + ":");
			op_debug.add(eq);
		}
		state.debug("cmd_op", cmd_op, "cmd_param:", cmd_param, "cmd_source:", cmd_source,
				"cmd_target:", cmd_target, "branch_type:", branch_type);

		// Override cmd_source for BRA
		Word zero_source = new Word(new Bit[]{ state.zero(), state.zero() });
		cmd_source = cmd_ops.get(Opcode.BRA).ifThen(zero_source, cmd_source);

		Word source_arg = cmd_source.eq(0).ifThen(cmd_param, 
				memoryRead8(state, data,
				cmd_source.decode(addr00, addr01, addr10, addr11)));

		state.debug("addr00:", addr00, "addr01:", addr01, "addr10:", addr10,
				"addr11:", addr11);

		Word target_arg = cmd_target.decode(r0(data), r1(data), r2(data), r3(data));

		state.debug("source_arg:", source_arg, "target_arg:", target_arg);

		// unary/neg (also used for cmp/sub)
		Word b_neg = source_arg.not().add(state.one());
		
		// unary/rorc
		Bit carry_rorc = source_arg.bit(0);
		Word b_rorc = source_arg.bits(7, 1);
		b_rorc = b_rorc.setBit(7, alu_carry);

		// unary/ror
		Word b_ror = source_arg.bits(7, 1);
		b_rorc = b_rorc.setBit(7, source_arg.bit(0));

		// unary/shr
		Word b_shr = source_arg.bits(7, 1);
		b_rorc = b_rorc.setBit(7, state.zero());

		// unary/rolc
		Bit carry_rolc = source_arg.bit(7);
		Word b_rolc = source_arg.bits(6, 0).shl(1, null); // null fill, since we're using alu_carry
		b_rolc = b_rolc.setBit(0, alu_carry);

		// unary/rol
		Word b_rol = source_arg.bits(6, 0).shl(1, source_arg.bit(0));

		// unary/shl
		Word b_shl = source_arg.bits(6, 0).shl(1, state.zero());

		// unary/swapn
		// Swaps the top and bottom nibbles
		Word b_swapn = source_arg.bits(3, 0).concat(source_arg.bits(7, 4));
		
		// cmp+sub (two's compliment, then add)
		Word b_cmp_sub = b_neg.add(target_arg);

		state.debug("cmp:", b_cmp_sub);

		// add
		WordAndBit add = target_arg.addWithCarry(source_arg, alu_carry);
		Bit carry_add = add.getBit();
		Word b_add = add.getWord();

		state.debug("b_add:", b_add, "carry_add:", carry_add);

		// loop
		Word b_loop = target_arg.add(state.negativeOne(8));
		// Detect zero by looking for a sign change
		Bit loop_zero = target_arg.bit(7).not().and(b_loop.bit(7));

		// Write-back
		// addr00 unused -- can we re-purpose this?
		Word addr = cmd_source.decode(addr01, addr01, addr10, addr11);

		memoryWrite8(state, data, addr, target_arg, cmd_ops.get(Opcode.STORE));

		// Update target_arg
		Word target_new = null;
		Word target_unchanged = target_arg;
		
		for (Opcode op : Opcode.values()) {
			Word new_value;
			switch (op) {
			case STORE:
			case BRA:
			case JUMP:
			case CMP:
			case SETFLAGS:
				new_value = target_unchanged;
				break;
			case LOAD:
			case SWAP:
				new_value = source_arg;
				break;
			case ADD:
				new_value = b_add;
				break;
			case SUB:
				new_value = b_cmp_sub;
				break;
			case AND:
				new_value = source_arg.and(cmd_param);
				break;
			case OR:
				new_value = source_arg.or(cmd_param);
				break;
			case XOR:
				new_value = source_arg.xor(cmd_param);
				break;
			case LOOP:
				new_value = b_loop;
				break;
			case UNARY:
				Word target_unary_new = null;
				for (UnaryOpcode subop : UnaryOpcode.values()) {
					Word new_unary_value;

					switch (subop) {
					case SHL:
						new_unary_value = b_shl;
						break;
					case SHR:
						new_unary_value = b_shr;
						break;
					case ROL:
						new_unary_value = b_rol;
						break;
					case ROLC:
						new_unary_value = b_rolc;
						break;
					case ROR:
						new_unary_value = b_ror;
						break;
					case RORC:
						new_unary_value = b_rorc;
						break;
					case NEG:
						new_unary_value = b_neg;
						break;
					case SWAPN:
						new_unary_value = b_swapn;
						break;
					default:
						throw new RuntimeException("Unhandled unary opcode: " + sub_op);
					}
					
					if (target_unary_new == null)
						target_unary_new = cmd_sub_ops.get(subop).and(new_unary_value);
					else
						target_unary_new = target_unary_new.xor(cmd_sub_ops.get(subop).and(new_unary_value));
				}
				new_value = target_unary_new;
				break;
			default:
				throw new RuntimeException("Unhandled opcode: " + op);
			}
			
			if (target_new == null)
				target_new = cmd_ops.get(op).and(new_value);
			else
				target_new = target_new.xor(cmd_ops.get(op).and(new_value));
		}
		
		state.debug("target_new:", target_new);

		set_r0(data,
				cmd_target.decode(target_new, r0(data), r0(data), r0(data)));
		set_r1(data,
				cmd_target.decode(r1(data), target_new, r1(data), r1(data)));
		set_r2(data,
				cmd_target.decode(r2(data), r2(data), target_new, r2(data)));
		set_r3(data,
				cmd_target.decode(r3(data), r3(data), r3(data), target_new));

		alu_zero = cmd_ops.get(Opcode.CMP).xor(cmd_ops.get(Opcode.SUB))
				.ifThen(b_cmp_sub.eq(0), alu_zero);

		alu_minus = cmd_ops.get(Opcode.CMP).xor(cmd_ops.get(Opcode.SUB))
				.ifThen(b_cmp_sub.bit(7), alu_minus);

//		alu_carry = cmd_ops.get(Opcode.ADD).ifThen(
//				carry_add,
//				cmd_ops.get(Opcode.UNARY).ifThen(a, b)
//				cmd_ops.get(Opcode.ROL).ifThen(
//						carry_rolc,
//						cmd_ops.get(Opcode.ROR).ifThen(
//								carry_rorc,
//								cmd_ops.get(Opcode.CARRY).ifThen(
//										source_arg.bit(0), alu_carry))));

		state.debug("carry:", alu_carry, "minus:", alu_minus, "zero:", alu_zero);

		// Add two to PC
		Word pc_linear = pc.shr(1).add(state.one()).shl(1, state.zero());

		Bit bra00 = alu_minus;
		Bit bra01 = alu_minus.or(alu_zero);
		Bit bra10 = alu_zero;
		Bit bra11 = alu_carry;

		Bit bra_success = cmd_ops.get(Opcode.BRA).and(branch_type.bit(2).xor(
				branch_type.bits(1, 0).decode(bra00, bra01, bra10, bra11)));
		Bit loop_not_zero = cmd_ops.get(Opcode.LOOP).and(loop_zero.not());
		Bit jump = cmd_ops.get(Opcode.JUMP);
		
		state.debug("bra?", bra_success, "!", branch_type.bit(2), "sel:",
				branch_type.bits(1, 0), "lt:", bra00, "lte:", bra01, "eq:",
				bra10, "ca:", bra11);

		pc = bra_success.xor(loop_not_zero).xor(jump).ifThen(source_arg, pc_linear);

		state.debug("pc:", pc, "r0:", r0(data), "r1:", r1(data), "r2:",
				r2(data), "r3:", r3(data));

		// Update CPU state
		state.setBitRegister(ALU_CARRY, alu_carry);
		state.setBitRegister(ALU_MINUS, alu_minus);
		state.setBitRegister(ALU_ZERO, alu_zero);
		state.setWordRegister(PC, pc);
		state.setWordArrayRegister(MEMORY_CODE, code);
		state.setWordArrayRegister(MEMORY_DATA, data);
	}

	private void set_r0(Word[] memory, Word value) {
		memory[R0] = memory[R0].setBits(7, 0, value);
	}

	private void set_r1(Word[] memory, Word value) {
		memory[R1] = memory[R1].setBits(7, 0, value);
	}

	private void set_r2(Word[] memory, Word value) {
		memory[R2] = memory[R2].setBits(7, 0, value);
	}

	private void set_r3(Word[] memory, Word value) {
		memory[R3] = memory[R3].setBits(7, 0, value);
	}

	private Word r0(Word[] memory) {
		return memory[R0].bits(7, 0);
	}

	private Word r1(Word[] memory) {
		return memory[R1].bits(7, 0);
	}

	private Word r2(Word[] memory) {
		return memory[R2].bits(7, 0);
	}

	private Word r3(Word[] memory) {
		return memory[R3].bits(7, 0);
	}}
