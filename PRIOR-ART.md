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
 
