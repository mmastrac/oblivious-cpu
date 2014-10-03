# Bubble sort

# TODO: this could avoid spilling r1 to memory if we traversed 
# the array backwards instead

start:
	mov r3, 0     # swap count
	mov r0, list  # cmp/swap index
	mov r1, [len] # count
	sub r1, 2
loop:
	# Ditch the current count into memory for now since we need the register
	mov [counttmp], r1

	mov r1, [r0]      # r1<-a
	cmp r1, [r0+1]    # cmp w/b
	blte noswap

	mov r2, [r0+1]    # r2<-b
	mov [r0], r2      # a<-b
	mov [r0+1], r1    # b<-a

	add r3, 1

noswap:
	mov r1, [counttmp]
	add r0, 1
	loop r1, loop
	
	# Can use this instead of cmp/bne since it'll work the same 
	# way if we don't care about it after
	loop r3, start

done:
	mov r1, 99
	mov [len], r1
	halt

counttmp:
	data 0

marker1:
	data 99
len:
	data 5
marker2:
	data 99
list:
	data 6, 5, 4, 9, 5
