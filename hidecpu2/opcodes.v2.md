# HideCPU

The HideCPU is a CPU with separate eight-bit data and 16-bit code spaces, each with a maximum size of 256 bytes. Instructions are encoded as 16 bits in the code space (four for the opcode and 12 for the opcode data), while data is represented as eight bits in the data space. Instructions are immutable and there is currently no way to read or write them.

The HideCPU has four eight-bit registers: r0-r3. r0 and r1 can be used for relative addressing and arithmetic, while r2 and r3 are for arithmetic only.

The registers and flags are aliases of memory locations to simplify the instruction format. The layout of these locations in memory is:

  * 0x00: Flags
  * 0x01: r0
  * 0x02: r1
  * 0x03: r2
  * 0x04: r3

## Assembler Opcodes

Note: the assembler opcodes aren't a one-to-one match with the machine opcodes.

### Common form

Most assembler opcodes consist of the form:

    op target, source

Target is one of the registers, r0-r3. Source is either a constant, a constant-relative load, or a constant load relative to r0 or r1.

### mov

`mov (r0-r3), (constant|constant-load|r0-relative|r1-relative|r0-r3)`

Operates as a load. If loading from one register to another, the memory location for the register is used instead.

`mov (constant-store|r0-relative|r1-relative), (r0|r1|r2|r3)`

Store operation, placing the contents in memory either in a constant location 
or relative to r0/r1. The `constant` instruction form is unused and undefined.

Examples:

`mov r0, ror [r0+10] => r0 = *(r0 + 10) ror 1`

### math

`(add|sub|and|or|xor), (constant|constant-load|r0-relative|r1-relative)`

Loads the source and target, applies a mathematical or binary operation to it, then stores
it back into the target register.

Examples:

`add r0, [r0+10] => r0 = r0 + *(r0 + 10)`

### unary

`(shl|shr|rol|rolc|ror|rorc|not|neg) rX`

Unary op applied to a register rX.

| mnemonic | description |
|---|---|
| shl | Shift left |
| shr | Shift right |
| rol | Rotate left |
| rolc | Rotate left through carry |
| ror | Rotate right |
| rorc | Rotate right through carry |
| not | Invert bits (encoded as the xor operation)|
| neg | 2s compliment |
| swapn | Swap nibbles (`aaaabbbb` -> `bbbbaaaa`) |

### cmp

Compares the source and target registers, setting the minus, and zero flags appropriately.

Examples:

```
cmp r0, [r0+10] 
    => minus = r0 < *(r0 + 10), zero = r0 == *(r0 + 10)
```

### flags

`setflags mask, values`

Set the flags to `(flags & ~mask) | (mask & values)`

Shortcuts:

  * setc: setflags 1, 1
  * clc: setflags 1, 0

### branch

`b[flags] constant`

Conditional branch to constant address based on status registers: `alu_minus`, `alu_zero`, `alu_carry`, `alu_xtra`.

| flags | test |
|---|---|
| lt    | minus == 1 && zero == 0 |
| lte   | !(minus == 0 && zero == 0) |
| gt    | minus == 0 && zero == 0 |
| gte   | !(minus == 1 && zero == 0) |
| eq    | zero == 1 |
| ne    | !(zero == 1) |
| ca    | carry == 1 |
| nc    | !(carry == 1) |
| mi    | minus == 1 |
| nm    | !(minus == 1) |
| xt1    | xtra1 == 1 |
| nx1    | !(xtra1 == 1) |
| xt2    | xtra2 == 1 |
| nx2    | !(xtra2 == 1) |
| xt3    | xtra3 == 1 |
| nx3    | !(xtra3 == 1) |

### jump

`jump (constant|constant-load|r0-relative|r1-relative)`

Unconditional branch.

### loop

`loop (r0-r3), (constant|constant-load|r0-relative|r1-relative)`

Decrements the target register and jumps if the register was zero before decrementing.

## Machine Opcodes

### Opcodes

| opcode | mnemonic | encoding | description |
|---|---|---|---|
|0  |load rX, loc             |(a)|load rX from loc
|1  |store rX, loc            |(a)|store rX to loc
|2  |swap rX, loc             |(a)|swap rX and loc
|3  |add rX, loc              |(a)|rX = rX + loc
|4  |sub rX, loc              |(a)|rX = rX - loc
|5  |and rX, loc              |(a)|rX = rX & loc
|6  |or  rX, loc              |(a)|rX = rX \| loc
|7  |xor rX, loc              |(a)|rX = rX ^ loc
|8  |unary rX, op             |(b)|op = shl, shr, rol, rolc, ror, rorc
|9  |cmp rX, loc              |(a)|set minus, zero
|10  |setflags mask, flags     |(c)|set/clear flags based on mask
|11 |bra invert, flags, target|(d)|branch based on flags
|12 |loop rX, loc             |(a)|decrement and jump if not zero
|13 |jump loc                 |(a)|jump unconditionally to target
|14|-|-| unused for now
|15|-|-| unused for now

Encodings:

 * (a): `aaaa bb cc dddddddd`
 * (b): `aaaa bb XX cccccccc`
 * (c): `aaaa bbbbbb cccccc`
 * (d): `aaaa b ccc dddddddd`

### Encoding A (standard)

The majority of opcodes are encoded using target/source/data like so:

    aaaa bb cc dddddddd
    a = opcode
    b = target register (source for store)
    c = source type
    d = data
  
#### Target argument

    00: r0
    01: r1
    10: r2
    11: r3

#### Source argument

    00: constant         # load this instruction's data directly as a constant
    01: [constant]       # load from data space indirectly using this instruction's data
    10: [r0 + constant] 
    11: [r1 + constant]
 
### Encoding B (unary)

Unary opcodes operate on a register only. The sub-op is encoded in the instruction like so:

    aaaa bb XX ccccdddd
    a = opcode
    b = target register
    c = sub-op
    d = argument

#### Target argument

    00: r0
    01: r1
    10: r2
    11: r3

#### Sub-op

|value|sub-op|
|---|---|
|0000|shl|
|0001|shr|
|0010|rol|
|0011|rolc|
|0100|ror|
|0101|rorc|
|0110|rorx|
|0111|neg|

#### Argument

The argument is four bits, used only for the bit shifting operations. If the high bit is one, the lowest two bits select a register. If the the high bit is zero, the lowest three bits specify an immediate value.



### Encoding C (setflags)

`setflags` has a unique encoding for the mask and flag values:

    aaaa bbbbbb cccccc
    a = opcode
    b = mask
    c = flags

### Encoding D (branch)

Branch operations require the flags to test and a constrant address like so:

    aaaa b ccc dddddddd
    a = opcode
    b = invert
    c = flags
    d = constant address

#### Flags

|invert|flags|mnemonic|
|---|---|---|
|0|000|lt|
|0|001|gt|
|0|010|eq|
|0|011|ca|
|0|100|mi|
|0|101|x1|
|0|110|x2|
|0|111|x3|
|1|000|gte|
|1|001|lte|
|1|010|ne|
|1|011|nc|
|1|100|nm|
|1|101|nx1|
|1|110|nx2|
|1|111|nx3|

### Load/store

```
# Load indirect
mov r0, [r0]
mov r0, label    \_ same encoding (load)
mov r0, constant /
mov r0, [label]    \__ same encoding (load)
mov r0, [constant] /
mov [r1], r0 <-- store 
```
