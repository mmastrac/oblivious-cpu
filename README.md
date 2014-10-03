oblivious-cpu [![Build Status](https://travis-ci.org/mmastrac/oblivious-cpu.svg?branch=master)](https://travis-ci.org/mmastrac/oblivious-cpu)
=============

Two CPUs based on Fully Homomorphic Encryption ([Wikipedia](https://en.wikipedia.org/wiki/Homomorphic_encryption#Fully_homomorphic_encryption)).

1) HideCPU: a novel four-register, byte-based CPU.

To run:

    bin/build.sh
    # Run the sort test
    bin/hidecpu.sh run hidecpu/samples/sort.asm
    # Run the credit-card check digit test
    bin/hidecpu.sh run hidecpu/samples/creditcard.asm

2) A re-implementation of [ShapeCPU](https://hcrypt.com/shape-cpu/) with a number of optimizations.

To run:

    bin/build.sh
    # Run the sort test
    bin/shapecpu.sh run shapecpu/samples/sort.asm
    # Run the credit-card check digit test
    bin/shapecpu.sh run shapecpu/samples/creditcard.asm

Implementation Notes
====================

This project uses a register transfer language built in Java to execute a CPU in either an immediate or a "recording" mode that can be used to construct a graph of XOR, AND and NOT operators. 
This approach allows us to optimize the CPU offline, removing redundant operations and reducing the overall gate count of the CPU. The graph can also be exported to a form that can be executed 
in an alternate FHE environment (C, etc) rather that in the Java environment provided.

Optimizations
=============

The graph optimizer currently optimizes the following patterns:

  * Unused nodes with no outputs (trimmed)
  * Greedy common-subexpression extraction
  * Constant folding (x XOR constant, x AND constant)

The graph optimizer does not optimize the following (yet):

  * !!a -> a
  * (!a XOR !b) -> (a XOR b)
  * (a XOR a XOR b) -> b
  * (a XOR b) AND !(b XOR c) -> (a XOR b) AND (a XOR c)
  * (a AND b) XOR (a AND c) -> a AND (b XOR c)

TODO
====

  * Proper documentation
  * An implementation of C. Gentry's fully homomorphic system (perhaps through libScarab)


Links
=====

The original version of ShapeCPU is here: https://hcrypt.com/shape-cpu/

License: Apache v2.0 [license](http://www.apache.org/licenses/LICENSE-2.0.html)

Note that some of the ShapeCPU samples may be under the ShapeCPU license.
