	mov r0, 15
doubler:
	# double every second digit
	add r2, [data + r0]
	sub r0, 1
	mov r1, [data + r0]
	shl r1
	cmp r1, 10
	blt no_sub
	# if n * 2 >= 10, we sum the sum of the digits instead of n
	sub r1, 9
no_sub:
	add r2, r1
	loop r0, doubler

	mov [sum], r2
	
	# Now check that the sum % 10 == 0
check:
	mov r1, r2
	# Use the top 5 bits to load the byte
	shr r1, 3
	mov r1, [mod10 + r1]
	# Use the bottom 3 bits to rotate (no need to mask them off - the shifter does) 
	mov r0, r2
	rorc r1, r0
	
	bnc fail
	mov r2, 99
fail:
	mov [res], r2
	halt
	


sum:
	data 0
res:
	data 0
	
data:
	# 5497 0365 0216 1618
	data 5, 4, 9, 7, 0, 3, 6, 5, 0, 2, 1, 6, 1, 6, 1, 8

# We can pre-compute all the valid multiples of 10 to save repeated subtraction
mod10:
	data 0x01, 0x04, 0x10, 0x40, 0x00 # 0->39
	data 0x01, 0x04, 0x10, 0x40, 0x00 # 40->79
	data 0x01, 0x04, 0x10, 0x40, 0x00 # 80->119
	data 0x01, 0x04, 0x10, 0x40, 0x00 # 120->159
	data 0x01, 0x04, 0x10, 0x40, 0x00 # 160->199
	data 0x01, 0x04, 0x10, 0x40, 0x00 # 200->239
