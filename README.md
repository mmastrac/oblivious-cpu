oblivious-cpu
=============

Two CPUs based on Fully Homomorphic Encryption ([Wikipedia](https://en.wikipedia.org/wiki/Homomorphic_encryption#Fully_homomorphic_encryption)).

1) A novel multiple-register CPU, HideCPU.

To run:

    bin/build.sh
    # Run the sort test
    bin/hidecpu.sh run hidecpu/samples/sort.asm
    # Run the credit-card check digit test
    bin/hidecpu.sh run hidecpu/samples/creditcard.asm

2) A re-implementation of [ShapeCPU](https://hcrypt.com/shape-cpu/), as well as a number of optimizations.

To run:

    bin/build.sh
    # Run the sort test
    bin/shapecpu.sh run shapecpu/samples/sort.asm
    # Run the credit-card check digit test
    bin/shapecpu.sh run shapecpu/samples/creditcard.asm

TODO:

  * Proper documentation
  * An implementation of C. Gentry's fully homomorphic system (perhaps through libScarab)

The original version of ShapeCPU is here: https://hcrypt.com/shape-cpu/

License: Apache v2.0 [license](http://www.apache.org/licenses/LICENSE-2.0.html)

Note that some of the ShapeCPU samples may be under the ShapeCPU license.
