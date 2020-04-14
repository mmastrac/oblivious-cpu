# Bubble sort

# r0: cmp/swap index
# r1: count
# r2: tmp swap space
# r3: swap count

start:
	mov r3, 0     # swap count
	mov r0, list  # cmp/swap index
	mov r1, [len] # count
	sub r1, 2
loop:
	mov r2, [r0]      # r2<-a
	cmp r2, [r0+1]    # cmp w/b
	blte noswap

	swap r2, [r0+1]   # b<->r2
	swap r2, [r0]     # r2<->a
	
	add r3, 1

noswap:
	add r0, 1
	loop r1, loop
	
	# Can use this instead of cmp/bne since it'll work the same 
	# way if we don't care about it after
	loop r3, start

done:
	mov r1, 99
	mov [len], r1
	halt

marker1:
	data 99
len:
	data 5
marker2:
	data 99
list:
	data 6, 5, 4, 9, 5
