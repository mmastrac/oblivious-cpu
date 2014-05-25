package com.grack.shapecpu;

import java.io.IOException;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

public class CPU {
	private static final int MEMORY_SIZE = 256;
	private final Bit MEMORY_READ;
	private final Bit ZERO;
	private final Bit ONE;
	private final Word CMP_CONSTANT;

	private Word pc;
	private Word ac;

	private Bit alu_carry;
	private Bit alu_minus;
	private Bit alu_zero;
	private Word[] memory;

	public CPU(NativeBitFactory factory) {
		MEMORY_READ = ONE = factory.encodeBit(1);
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
			for (int i = 0; i < memoryContents.length; i++) {
				String line = memoryContents[i];
				String[] bytes = line.split(" ");
				int value = 0;
				for (int j = 0; j < 13; j++) {
					value |= (Integer.valueOf(bytes[j], 16) & 1) << j;
				}

				memory[i] = factory.encodeWord(value, 13);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private Word memory_access(Word addr, Word ac, Bit rw) {
		Bit[] r = new Bit[MEMORY_SIZE];

		// Unroll the first time through the loop
		r[0] = addr.eq(0);
		Word b1 = r[0].and(memory[0]);
		memory[0] = (r[0].and(rw)).ifThen(ac, memory[0]);

		for (int row = 1; row < MEMORY_SIZE; row++) {
			r[row] = addr.eq(row);
			memory[row] = (r[row].and(rw)).ifThen(ac, memory[row]);
			b1 = b1.or(r[row].and(memory[row]));
		}

		return b1;
	}

	public void tick() {
		Word b1 = memory_access(pc, ac, MEMORY_READ);
		Word load_arg = memory_access(b1, ac, MEMORY_READ);

		Word cmd = b1.bits(11, 8).reverse();

		// Decode
		Bit cmd_store = cmd.eq(0); // Store ac to memory
		Bit cmd_load = cmd.eq(1); // Load memory to ac
		Bit cmd_rol = cmd.eq(2); // Rotate left through alu_carry
		Bit cmd_ror = cmd.eq(3); // Rotate right through alu_carry
		Bit cmd_add = cmd.eq(4); // Add ac to immediate or indirect
		Bit cmd_clc = cmd.eq(5); // Clear carry
		Bit cmd_sec = cmd.eq(6); // Set carry
		Bit cmd_xor = cmd.eq(7); // XOR ac with immediate
		Bit cmd_and = cmd.eq(8); // AND ac with immediate
		Bit cmd_or = cmd.eq(9); // OR ac with immediate
		Bit cmd_beq = cmd.eq(10); // Branch if alu_zero
		Bit cmd_jmp = cmd.eq(11); // Branch unconditionally
		Bit cmd_la = cmd.eq(12); // Load indirect
		Bit cmd_bmi = cmd.eq(13); // Branch if alu_minus
		Bit cmd_cmp = cmd.eq(14); // Compare ac with immediate or
									// indirect

		// Address?
		Bit cmd_a = b1.bit(12);
		Word cmd_param = b1.bits(8, 0);

		// CMP
		Word b_cmp = cmd_a.ifThen(b1, load_arg).not().add(CMP_CONSTANT).add(ac);

		// ROL
		Bit carry_rol = ac.bit(0);
		Word b_rol = ac.bits(7, 1);
		b_rol = b_rol.setBit(7, alu_carry);

		// ROR
		Bit carry_ror = ac.bit(7);
		Word b_ror = ac.bits(6, 0).shl(1);
		b_ror = b_rol.setBit(0, alu_carry);

		// ADD
		WordAndBit add_1 = ac.addWithCarry(b1, alu_carry);
		WordAndBit add_2 = ac.addWithCarry(load_arg, alu_carry);
		Word b_add = cmd_a.ifThen(add_1.getWord(), add_2.getWord());
		Bit carry_add = cmd_a.ifThen(add_1.getBit(), add_2.getBit());

		Word load_val = memory_access(b1, ac, cmd_store.not());

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

		Word pc_linear = pc.add(ONE);

		pc = cmd_beq.ifThen(
				alu_zero.ifThen(cmd_param, pc_linear),
				cmd_bmi.ifThen(alu_minus.ifThen(cmd_param, pc_linear),
						cmd_jmp.ifThen(cmd_param, pc_linear)));
	}
}
