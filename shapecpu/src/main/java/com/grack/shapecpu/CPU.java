package com.grack.shapecpu;

import java.io.IOException;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

public class CPU {
	private static final int MEMORY_SIZE = 256;
	private final Bit ZERO;
	private final Bit ONE;
	private final Word CMP_CONSTANT;

	Word pc;
	Word ac;

	Bit alu_carry;
	Bit alu_minus;
	Bit alu_zero;
	Word[] memory;
	
	private NativeBitFactory factory;
	private boolean debug;

	public CPU(NativeBitFactory factory, boolean debug) {
		this.factory = factory;
		this.debug = debug;

		ONE = factory.encodeBit(1);
		ZERO = factory.encodeBit(0);
		CMP_CONSTANT = factory.encodeWord(0b1000_0000, 8);

		ac = factory.encodeWord(0, 8);
		pc = factory.encodeWord(0, 8);
		alu_carry = ZERO;
		alu_minus = ZERO;
		alu_zero = ZERO;

		memory = new Word[MEMORY_SIZE];
		for (int i = 0; i < MEMORY_SIZE; i++) {
			memory[i] = factory.encodeWord(0, 13);
		}

		try {
			String mem = Resources.toString(getClass()
					.getResource("memory.txt"), Charsets.UTF_8);
			String[] memoryContents = mem.trim().split("\n");
			debug("memory size:", memoryContents.length);
			
			for (int i = 0; i < memoryContents.length; i++) {
				String line = memoryContents[i];
				String[] bytes = line.split(" ");
				int value = 0;
				for (int j = 0; j < 13; j++) {
					value |= (Integer.valueOf(bytes[j], 16) & 1) << j;
				}

				debug(i, String.format("%13s", Integer.toString(value, 2)));
				memory[i] = factory.encodeWord(value, 13);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
//		System.exit(0);
	}

	private Word memoryRead(Word addr, int size) {
		// Unroll the first time through the loop
		Word b1 = addr.eq(0).and(memory[0].bits(size - 1, 0));
//		debug("b1", addr.eq(0), 0, memory[0], b1);

		for (int row = 1; row < MEMORY_SIZE; row++) {
			b1 = b1.or(addr.eq(row).and(memory[row].bits(size - 1, 0)));
//			debug("b1", addr.eq(row), row, memory[row], b1);
		}

		debug("b1", b1, addr);

		return b1;
	}

	/**
	 * Reads or writes a memory address to/from a register, depending on the
	 * state of the write flag.
	 */
	private Word memoryAccess(Word addr, Word reg, Bit write) {
		Bit[] r = new Bit[MEMORY_SIZE];

		// Unroll the first time through the loop
		r[0] = addr.eq(0);
		memory[0] = (r[0].and(write)).ifThen(reg, memory[0]);
		Word b1 = r[0].and(memory[0].bits(7, 0));
//		debug("b1", r[0], memory[0], 0, b1);

		for (int row = 1; row < MEMORY_SIZE; row++) {
			r[row] = addr.eq(row);
			memory[row] = (r[row].and(write)).ifThen(reg, memory[row]);
			b1 = b1.or(r[row].and(memory[row].bits(7, 0)));
//			debug("b1", r[row], memory[row].bits(7, 0), row, b1);
		}

		debug("b1", b1, addr, reg, write);
		return b1;
	}

	public void tick() {
		debug("***** tick *****");

		debug("pc =", pc);
		debug("ac =", ac);

		Word cmd = memoryRead(pc, 13);
		Word cmd_param = cmd.bits(7, 0);

		Word load_arg = memoryRead(cmd_param, 8);

		debug("cmd =", cmd);
		debug("load_arg =", load_arg);

		// Bytecode looks like:
		// | address_flag[1] | cmd[4] | data[8] |
		Word cmd_op = cmd.bits(11, 8);

		// Decode
		Bit cmd_store = cmd_op.eq(15); // Store ac to memory
		Bit cmd_load = cmd_op.eq(14); // Load memory to ac
		Bit cmd_rol = cmd_op.eq(13); // Rotate left through alu_carry
		Bit cmd_ror = cmd_op.eq(12); // Rotate right through alu_carry
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

		debug("Command select: ", "store:", cmd_store, "load:", cmd_load,
				"rol:", cmd_rol, "ror:", cmd_ror, "add:", cmd_add, "clc:", cmd_clc, "sec:", cmd_sec,
				"xor:", cmd_xor, "and:", cmd_and, "or:", cmd_or, "beq:", cmd_beq, "jmp:", cmd_jmp, "la:", cmd_la, "bmi:", cmd_bmi,
				"cmp:", cmd_cmp);

		// Address?
		Bit cmd_a = cmd.bit(12);
		
		debug("cmd_param:", cmd_param, "cmd_a:", cmd_a);
		
		// CMP
		Word b_cmp = cmd_a.ifThen(cmd, load_arg).not().add(CMP_CONSTANT).add(ac);

		// ROR
		Bit carry_ror = ac.bit(0);
		Word b_ror = ac.bits(7, 1);
		b_ror = b_ror.setBit(7, alu_carry);

		// ROL
		Bit carry_rol = ac.bit(7);
		Word b_rol = ac.bits(6, 0).shl(1);
		b_rol = b_rol.setBit(0, alu_carry);

		// ADD
		WordAndBit add_1 = ac.addWithCarry(cmd_param, alu_carry);
		WordAndBit add_2 = ac.addWithCarry(load_arg, alu_carry);
		Word b_add = cmd_a.ifThen(add_1.getWord(), add_2.getWord());
		Bit carry_add = cmd_a.ifThen(add_1.getBit(), add_2.getBit());

		Word load_val = memoryAccess(cmd_param, ac, cmd_store);

		Bit ac_unchanged = cmd_sec.or(cmd_clc).or(cmd_beq).or(cmd_bmi)
				.or(cmd_cmp).or(cmd_jmp).or(cmd_store);

		Word ac_new;
		ac_new = cmd_load.and(cmd_param);
		ac_new = ac_new.or(cmd_ror.and(b_ror));
		ac_new = ac_new.or(cmd_rol.and(b_rol));
		ac_new = ac_new.or(cmd_add.and(b_add));
		ac_new = ac_new.or(cmd_and.and(ac.and(cmd_param)));
		ac_new = ac_new.or(cmd_xor.and(ac.xor(cmd_param)));
		ac_new = ac_new.or(cmd_or.and(ac.or(cmd_param)));
		ac_new = ac_new.or(cmd_la.and(load_val));
		ac_new = ac_new.or(ac_unchanged.and(ac));
		
		ac = ac_new;
		debug("ac =", ac);

		alu_zero = (cmd_cmp.ifThen(b_cmp.eq(0), ac.eq(0)).or(alu_zero
				.and(cmd_bmi.or(cmd_beq))));

		alu_minus = cmd_cmp.ifThen(b_cmp.bit(7), alu_minus);

		alu_carry = cmd_add.ifThen(
				carry_add,
				cmd_rol.ifThen(
						carry_rol,
						cmd_ror.ifThen(
								carry_ror,
								cmd_clc.ifThen(ZERO,
										cmd_sec.ifThen(ONE, alu_carry)))));

		debug("carry:", alu_carry, "minus:", alu_minus, "zero:", alu_zero);
		
		Word pc_linear = pc.add(ONE);

		pc = cmd_beq.ifThen(
				alu_zero.ifThen(cmd_param, pc_linear),
				cmd_bmi.ifThen(alu_minus.ifThen(cmd_param, pc_linear),
						cmd_jmp.ifThen(cmd_param, pc_linear)));
	}

	private void debug(Object... things) {
		if (!debug)
			return;
		
		for (Object thing : things) {
			System.out.print(thing + " ");
		}
		System.out.println();
	}
}
