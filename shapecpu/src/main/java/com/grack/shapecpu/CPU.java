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

	private Bit alu_carry;
	private Bit alu_minus;
	private Bit alu_zero;
	private Word[] memory;
	private NativeBitFactory factory;

	public CPU(NativeBitFactory factory) {
		this.factory = factory;

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
			for (int i = 0; i < memoryContents.length; i++) {
				String line = memoryContents[i];
				String[] bytes = line.split(" ");
				int value = 0;
				for (int j = 0; j < 13; j++) {
					value |= (Integer.valueOf(bytes[j], 16) & 1) << j;
				}

//				System.out.println(String.format("%13s", Integer.toString(value, 2)));
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
//		debug("b1", addr.eq(0), 0, b1);

		for (int row = 1; row < MEMORY_SIZE; row++) {
			b1 = b1.or(addr.eq(row).and(memory[row].bits(size - 1, 0)));
//			debug("b1", addr.eq(row), row, b1);
		}

		return b1;
	}

	private Word memoryAccess(Word addr, Word ac, Bit rw) {
		Bit[] r = new Bit[MEMORY_SIZE];

		// Unroll the first time through the loop
		r[0] = addr.eq(0);
		memory[0] = (r[0].and(rw)).ifThen(ac, memory[0]);
		Word b1 = r[0].and(memory[0].bits(7, 0));

		for (int row = 1; row < MEMORY_SIZE; row++) {
			r[row] = addr.eq(row);
			memory[row] = (r[row].and(rw)).ifThen(ac, memory[row]);
			b1 = b1.or(r[row].and(memory[row].bits(7, 0)));
		}

		return b1;
	}

	public void tick() {
		debug("***** tick *****");

		debug("pc =", pc);

		Word b1 = memoryRead(pc, 13);
		Word cmd_param = b1.bits(7, 0);

		Word load_arg = memoryRead(cmd_param, 8);

		debug("b1 =", b1);
		debug("load_arg =", load_arg);

		// Bytecode looks like:
		// | address_flag[1] | cmd[4] | data[8] |
		Word cmd = b1.bits(11, 8);

		// Decode
		Bit cmd_store = cmd.eq(15); // Store ac to memory
		Bit cmd_load = cmd.eq(14); // Load memory to ac
		Bit cmd_rol = cmd.eq(13); // Rotate left through alu_carry
		Bit cmd_ror = cmd.eq(12); // Rotate right through alu_carry
		Bit cmd_add = cmd.eq(11); // Add ac to immediate or indirect
		Bit cmd_clc = cmd.eq(10); // Clear carry
		Bit cmd_sec = cmd.eq(9); // Set carry
		Bit cmd_xor = cmd.eq(8); // XOR ac with immediate
		Bit cmd_and = cmd.eq(7); // AND ac with immediate
		Bit cmd_or = cmd.eq(6); // OR ac with immediate
		Bit cmd_beq = cmd.eq(5); // Branch if alu_zero
		Bit cmd_jmp = cmd.eq(4); // Branch unconditionally
		Bit cmd_la = cmd.eq(3); // Load indirect
		Bit cmd_bmi = cmd.eq(2); // Branch if alu_minus
		Bit cmd_cmp = cmd.eq(1); // Compare ac with immediate or
									// indirect

		debug("Command select: ", "store:", cmd_store, "load:", cmd_load,
				"rol:", cmd_rol, "ror:", cmd_ror, "add:", cmd_add, "clc:", cmd_clc, "sec:", cmd_sec,
				"xor:", cmd_xor, "and:", cmd_and, "or:", cmd_or, "beq:", cmd_beq, "jmp:", cmd_jmp, "la:", cmd_la, "bmi:", cmd_bmi,
				"cmp:", cmd_cmp);

		// Address?
		Bit cmd_a = b1.bit(12);
		
		debug("cmd_param:", cmd_param, "cmd_a:", cmd_a);
		
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

		Word load_val = memoryAccess(b1, ac, cmd_store.not());

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
		System.out.println(pc + " " + pc_linear);

		pc = cmd_beq.ifThen(
				alu_zero.ifThen(cmd_param, pc_linear),
				cmd_bmi.ifThen(alu_minus.ifThen(cmd_param, pc_linear),
						cmd_jmp.ifThen(cmd_param, pc_linear)));

	}

	private void debug(Object... things) {
		for (Object thing : things) {
			System.out.print(thing + " ");
		}
		System.out.println();
	}
}
