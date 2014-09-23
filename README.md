oblivious-cpu
=============

A re-implementation of ShapeCPU via reverse-engineering and optimization.

To run:

    bin/build.sh
    # Run the sort test
    bin/shapecpu.sh run samples/sort.asm
    # Run the credit-card check digit test
    bin/shapecpu.sh run samples/creditcard.asm

TODO:

  * Proper documentation
  * A new, more optimal CPU
  * An implementation of C. Gentry's fully homomorphic system (perhaps through libScarab)

The original version of ShapeCPU is here: https://hcrypt.com/shape-cpu/
