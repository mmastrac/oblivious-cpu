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
	# Add 10 to r2 so we can omit the zero case
	add r2, 10
	
	# r2 * 0x99 -> r0:r1
	mul r2, 0x99
	# r0 = (r2 * 0x199) >> 8
	add r0, r2
	# If r0 & 0xf == 0xf, this was a multiple of 10
	and r0, 0xf
	cmp r0, 0xf
	bne fail	
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
