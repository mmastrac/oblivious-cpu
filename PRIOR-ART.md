Prior art:

 - Storage inspired by rotating storage: gates and index "swapped" out. For example, 16 bytes might be available at a time. The CPU
 would wait for the index to match a given index, then that data could be moved into memory (or a single byte from a given index 
 might be loaded to a register). 
 - Grid of FHE-based CPUs. More limited CPUs (ie: 8-bit) could exchange data across inter-cpu busses to create higher-performance larger
 computing units.
 - Encrypted termination conditions. Clients can execute larger and larger cycle counts while returning the decrypted result (ie: 8, 16, 32,
 64, etc). CPU can continue executing while it waits for the caller to determine halt.
 - Encrypted channel with client. The client can continue to send bundles which are inserted into memory and available to program. The 
 program can also store encrypted values in a given memory location that are streamed to a client at certain intervals.
 - Client can submit both the program and initial data to a FHE execution server. It may include information to query from a
 given encrypted database and insert into memory.
 - Client may include a program, a given number of execution cycles, then a place to store the result in the database after that number
 of cycles have completed.
 - Optimizing compiler: provide optimal gates for system + cost, compiler can choose fundamental gates to optimize system for
 - Automatic pipelining for larger CPUs. Slice the gate graph into equal bits so that we can reduce the input-to-output path length.
    - Automatically fetch ahead assuming PC increments
 - PC that increments using a gray code system to avoid paying for additions
 - CPU that you can configure parametrically - bit size, register count, etc. Automatically optimize for that setup.
