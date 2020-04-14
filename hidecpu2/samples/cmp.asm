	mov r0, 0
	cmp r0, 0
	beq correct
	mov r0, 99
	mov [res], r0
	halt

correct:
	mov r0, 1
	mov [res], r0
	halt
		
res:
	data 0