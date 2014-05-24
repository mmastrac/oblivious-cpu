package com.grack.shapecpu;

public class CPU {
	private static final int MEMORY_SIZE = 256;
	private final Bit MEMORY_READ;
	private Word pc;
	private Word ac;

	private Bit alu_carry;
	private Bit alu_minus;
	private Bit alu_zero;
	private Bit ZERO;
	private Bit ONE;
	private Word CMP_CONSTANT;
	private Word[] memory;

	public CPU(Function fn) {
		MEMORY_READ = ONE = fn.encodeBit(1);
		CMP_CONSTANT = fn.encodeWord(0b1000_0000);
		ZERO = fn.encodeBit(0);
	}

	private Word memory_access(Word addr, Word ac, Bit rw) {
		Bit[] r = new Bit[MEMORY_SIZE];
		Word b1 = addr.eq(0).and(memory[0]);

		for (int row = 0; row < MEMORY_SIZE; row++) {
			r[row] = addr.eq(row);
			memory[row] = (r[row].and(rw)).ifThen(ac, memory[row]);

			if (row != 0) {
				b1 = b1.or(r[row].and(memory[row]));
			}
		}
	
		return b1;
	}

	public void tick() {
		Word b1 = memory_access(pc, ac, MEMORY_READ);
		Word load_arg = memory_access(b1, ac, MEMORY_READ);

		// Decode
		Bit cmd_store = b1.bitsEq(11, 8, 0); // Store ac to memory
		Bit cmd_load = b1.bitsEq(11, 8, 1); // Load memory to ac
		Bit cmd_rol = b1.bitsEq(11, 8, 2); // Rotate left through alu_carry
		Bit cmd_ror = b1.bitsEq(11, 8, 3); // Rotate right through alu_carry
		Bit cmd_add = b1.bitsEq(11, 8, 4); // Add ac to immediate or indirect
		Bit cmd_clc = b1.bitsEq(11, 8, 5); // Clear carry
		Bit cmd_sec = b1.bitsEq(11, 8, 6); // Set carry
		Bit cmd_xor = b1.bitsEq(11, 8, 7); // XOR ac with immediate
		Bit cmd_and = b1.bitsEq(11, 8, 8); // AND ac with immediate
		Bit cmd_or = b1.bitsEq(11, 8, 9); // OR ac with immediate
		Bit cmd_beq = b1.bitsEq(11, 8, 10); // Branch if alu_zero
		Bit cmd_jmp = b1.bitsEq(11, 8, 11); // Branch unconditionally
		Bit cmd_la = b1.bitsEq(11, 8, 12); // Load indirect
		Bit cmd_bmi = b1.bitsEq(11, 8, 13); // Branch if alu_minus
		Bit cmd_cmp = b1.bitsEq(11, 8, 14); // Compare ac with immediate or indirect

		// Address?
		Bit cmd_a = b1.bit(12);
		Word cmd_param = b1.bits(8, 0);

		// CMP
		Word b_cmp = cmd_a.ifThen(b1, load_arg).not().add(CMP_CONSTANT).add(ac);

		// ROL
		Bit carry_rol= ac.bit(0);
		Word b_rol = ac.bits(7, 1);
		b_rol.setBit(7, alu_carry);

		// ROR
		Bit carry_ror = ac.bit(7);
		Word b_ror = ac.bits(6, 0).shl(1);
		b_ror.setBit(0, alu_carry);

		// ADD
		b_add_1, carry_1 =add_with_carry(ac,b1,alu_carry);
		b_add_2, carry_2 =add_with_carry(ac,load_arg,alu_carry);		
		Word b_add = cmd_a.ifThen(b_add_1, b_add_2);
		Bit carry_add = cmd_a.ifThen(carry_1, carry_2);

		Word load_val = memory_access(b1, ac, cmd_store.not());
		 
		Bit ac_unchanged = cmd_sec.or(cmd_clc).or(cmd_beq)
				.or(cmd_bmi).or(cmd_cmp).or(cmd_jmp).or(cmd_store);
		
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

		alu_zero = (cmd_cmp.ifThen(b_cmp.eq(0), ac.eq(0)).or(alu_zero.and(cmd_bmi.or(cmd_beq))));

		alu_minus = cmd_cmp.ifThen(b_cmp.bit(7), alu_minus);

		alu_carry = 
			cmd_add.ifThen(carry_add, 
					cmd_rol.ifThen(carry_rol, 
							cmd_ror.ifThen(carry_ror, 
									cmd_clc.ifThen(ZERO, 
											cmd_sec.ifThen(ONE, alu_carry)))));

		Word pc_linear = pc.add(ONE);

		pc = 
			cmd_beq.ifThen(alu_zero.ifThen(cmd_param, pc_linear),
					cmd_bmi.ifThen(alu_minus.ifThen(cmd_param, pc_linear), 
							cmd_jmp.ifThen(cmd_param, pc_linear)));
	}
}
