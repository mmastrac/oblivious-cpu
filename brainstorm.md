## The Homomorphic "Hard Drive"

(not completely thought out...)

We can use an analagy to the physical hard drive using a virtual spinning platter that is connected to the network in a special way. A virtual hard drive controller is mapped to memory with registers like so:

```
| Address | Register                    |
| 238     | Operation (1=read, 2=write) |
| 239     | HDD linear address high     |
| 240-255 | HDD read/write window       | 
```

The virtual HDD "spins" each cycle, reading/writing a window of 16 bytes if it matches the HDD linear address.

Alternative idea: DMA to location in memory rather than using a memory window.
