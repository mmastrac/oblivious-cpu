# HideCPU

The HideCPU is a CPU with separate eight-bit data and 16-bit code spaces, each with a maximum size of 256 bytes. Instructions are encoded as 16 bits in the code space (four for the opcode and 12 for the opcode data), while data is represented as eight bits in the data space. Instructions are immutable and there is currently no way to read or write them.

The HideCPU has four eight-bit registers: r0-r3. r0 and r1 can be used for relative addressing and arithmetic, while r2 and r3 are for arithmetic only.

The registers and flags are aliases of memory locations to simplify the instruction format. The layout of these locations in memory is:

  * 0x00: r0
  * 0x01: r1
  * 0x02: r2
  * 0x03: r3
  * 0x04: cr
  * 0x05: flags
  * 0x06: pc
  * 0x07: lr

## Assembler Opcodes

Note: the assembler opcodes aren't a one-to-one match with the machine opcodes.

### Common form

Most assembler opcodes consist of the form:

    op target, source

Target is one of the registers, r0-r3. Source is either a constant, a constant-relative load, or a constant load relative to r0 or r1.

### mov (load/store)

`mov (r0-r4,flags,pc,lr), (constant|constant-load|r0-relative|r1-relative|r0-r4|flags|pc|lr)`

Operates as a load. If loading from one register to another, the memory location for the register is used instead. `load0` is used for r0-r3, `load1` for r4 and other registers.

`mov (constant-store|r0-relative|r1-relative), (r0-r3)`

Store operation, placing the contents in memory either in a constant location 
or relative to r0/r1. The `constant` instruction form is unused and undefined.

Special forms:

  * `ret`: `mov pc, lr`
  * `bsub dst`: `mov lr, pc+8`, `mov pc, dst`
  * `bra dst`: `mov pc, dst`
  * `halt`: `mov pc, pc`
  * `mov lr, pc`: `mov lr, (constant)`
  
Examples:

`mov r0, [r0+10] => r0 = *(r0 + 10)`

### math

`(add|sub|and|or|xor) (r0-r3), (constant|constant-load|r0-relative|r1-relative|r0-r4|flags|pc|lr)`

Loads the source and target, applies a mathematical or binary operation to it, then stores
it back into the target register.

Special forms:

  * `inc r0`: `add r0, 1`
  * `dec r0`: `sub r0, 1`
  * `not r0`: `xor r0, 255`

Examples:

`add r0, [r0+10] => r0 = r0 + *(r0 + 10)`

### swap

`swap (r0-r3), (constant-load|r0-relative|r1-relative|r0-r4|flags|pc|lr)`

Loads the source and target, swaps them and stores them back. The `constant` instruction form is unused and undefined.

### unary

`(shl|shr|rol|rolc|ror|rorc|neg|swapn) rX`
`(shl|shr|rol|ror) rX, cr`

Unary op applied to a register rX.

| mnemonic | description |
|---|---|
| shl | Shift left |
| shr | Shift right |
| rol | Rotate left |
| rolc | Rotate left through carry |
| ror | Rotate right |
| rorc | Rotate right through carry |
| neg | 2s compliment |
| swapn | Swap nibbles (`aaaabbbb` -> `bbbbaaaa`) |

### cmp

`cmp (r0-r3), (constant|constant-load|r0-relative|r1-relative|r0-r4|flags|pc|lr)`

Compares the source and target registers, setting the minus, and zero flags appropriately.

Examples:

```
cmp r0, [r0+10] 
    => minus = r0 < *(r0 + 10), zero = r0 == *(r0 + 10)
```

### flags

`setflags mask, values`

Set the flags to `(flags & ~mask) | (mask & values)`

Special forms:

  * setc: setflags ca, ca
  * clc: setflags ca, 0
  * setz: setflags eq, eq
  * clz: setflags eq, 0

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


### loop

`loop (r0-r3), (constant|constant-load|r0-relative|r1-relative)`

Decrements the target register and jumps if the register was zero before decrementing.

## Machine Opcodes

### Opcodes

| opcode | mnemonic | encoding | description |
|---|---|---|---|
|0  |load0 rX, loc            |(a)|load rX from loc
|1  |store0 rX, loc           |(a)|store rX to loc
|2  |add rX, loc              |(a)|rX = rX + loc
|3  |sub rX, loc              |(a)|rX = rX - loc
|4  |and rX, loc              |(a)|rX = rX & loc
|5  |or rX, loc               |(a)|rX = rX \| loc
|6  |xor rX, loc              |(a)|rX = rX ^ loc
|7  |cmp rX, loc              |(a)|set minus, zero
|8  |load1 rX, loc            |(a)|load r(X+4) from loc
|9  |store1 rX, loc           |(a)|store r(X+4) to loc
|10 |swap rX, loc             |(a)|swap rX and loc
|11 |loop rX, loc             |(a)|decrement and jump if not zero
|12 |unary rX, op             |(b)|op = shl, shr, rol, ror, etc.
|13 |setflags mask, flags     |(c)|set/clear flags based on mask
|14 |bra invert, flags, target|(d)|branch based on flags
|15 |mul rX, loc       			|(e)|r0:r1 = rX * loc

Encodings:

 * (a): `aaaa bb cc dddddddd`
 * (b): `aaaa bb XX cccccccc`
 * (c): `aaaa bbbbbb cccccc`
 * (d): `aaaa b ccc dddddddd`
 * (e): `aaaa bb c ddd`

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

    00 (load): constant   # load this instruction's data directly as a constant
    00 (store): register  # store this instructions output to a register 
    01: [constant]        # load from data space indirectly using this instruction's data
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

The sub-op is a 4-bit selector for one of the possible 16 sub-ops, mainly using the barrel shifter:

| subop | mnemonic | description |
|---|---|---|
|0000| shl.n | Shift left by n (constant) |
|0001| shr.n | Shift right by n (constant) |
|0010| rol.n | Rotate left by n (constant) |
|0011| ror.n | Rotate right by n (constant) |
|0100| rolc | Rotate left through carry |
|0101| rorc | Rotate right through carry |
|0110| unused | unused |
|0111| unused | unused |
|1000| shl.cr | Shift left by cr |
|1001| shr.cr | Shift right by cr |
|1010| rol.cr | Rotate left by cr |
|1011| ror.cr | Rotate right by cr |
|1100| adc | If carry is set, adds 1 |
|1101| sbm | If minus is set, subtracts 1 |
|1110| neg | 2s compliment |
|1111| loadro | Loads instruction data from code segment to r0:r1 |

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

### Encoding E (mul)

Multiply operations require a signed flag, a source and a multiplier:

    aaaa b cc d X eeeeeeee
    a = opcode
    b = signed
    c = source multiplicand
    d = multiplier selection
    e = multiplier
    
#### Source argument

    00: r0
    01: r1
    10: r2
    11: r3

#### Multiplier selection

    0: register (multiplier is an index to register)
    1: constant

    
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
