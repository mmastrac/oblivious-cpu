HideCPU
=======

The HideCPU has four eight-bit registers: r0-r3. r0 and r1 can be used for relative addressing and loads, while
r2 and r3 are for arithmetic only.

Opcode layout
=============

    aaaa bb cc dddddddd
    a = opcode
    b = target register (source for store)
    c = source type
    d = data

Source argument
===============

    0: constant 
    1: [constant]       # indirect, constant
    2: [r0 + constant] 
    3: [r1 + constant]
  
Target argument
===============

    0-3: r0 - r3

Assembler Opcodes
=================

The assembler opcodes aren't a one-to-one match with the machine opcodes.

Most assembler opcodes consist of the form:

    op target, source

Target is one of the registers, r0-r3. Source is either a constant, a constant-relative load, or a constant 
load relative to r0 or r1.

mov
===

`mov (r0-r3), [ror|rol|not] (constant|constant-load|r0-relative|r1-relative|r0-r3)`

Operates as a load, optionally rotating left or right or inverting. If loading from
one register to another, the memory location for the register is used instead.

`mov (constant-store|r0-relative|r1-relative), (r0|r1|r2|r3)`

Store operation, placing the contents in memory either in a constant location 
or relative to r0/r1.

Examples:

`mov r0, ror [r0+10] => r0 = *(r0 + 10) ror 1`

math
====

`(add|sub|and|or|xor), (constant|constant-load|r0-relative|r1-relative)`

Loads the source and target, applies a mathematical or binary operation to it, then stores
it back into the target register.

Examples:

`add r0, [r0+10] => r0 = r0 + *(r0 + 10)`

cmp
===

Compares the source and target registers, setting the minus, and zero flags appropriately.

Examples:

```
cmp r0, [r0+10] 
    => minus = r0 < *(r0 + 10), zero = r0 == *(r0 + 10)
```

sec/clc
=======

Sets or clears the carry flag explicitly.

branch/jump/loop
================

Jumps to the source location, conditionally (eg: loop, blt, beq), or unconditionally (jump).

    b[lt|lte|eq|ca|gte|gt|ne|nc] (constant|constant-load|r0-relative|r1-relative)

Conditional branch based on status registers: alu_minus, alu_zero, alu_carry.

    jump (constant|constant-load|r0-relative|r1-relative)

Unconditional branch.

    loop (r0-r3), (constant|constant-load|r0-relative|r1-relative)

Decrements the register and jumps if the register was zero before decrementing.

Machine Opcodes
===============

```
0  load  # load          \_ mov
1  store # store           |
2  ror   # rotate right    |
3  rol   # rotate left     |
4  not   # invert bits    /  <- is this necessary?

5  add rX, something # add
6  sub rX, something # subtract
7  and rX, something # and
8  or  rX, something # or
9  xor rX, something # xor

10 cmp rX, something # set minus, zero
11 carry value       # set carry
12 bra flags, mask, target # branch based on flags (minus/zero/carry)
13 loop r0, target   # decrement and jump if not zero
14 jump target       # jump unconditionally to target
15 unused for now
```

```
# Load indirect
mov r0, [r0]
mov r0, label    \_ same encoding (load)
mov r0, constant /
mov r0, [label]    \__ same encoding (load)
mov r0, [constant] /
mov r0, ror [r0] <- encoded as ror
mov r0, rol [r0]
mov r0, not [r0]
mov [r1], r0 <-- store 
```
