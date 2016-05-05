	mov r0, 99
	mov [res1], r0

	mov r1, r0
	mov [res2], r1

	add r1, r0
	mov [res3], r1

	mov r0, res4
	mov [r0], r1

	halt

res1:
	data 0

res2:
	data 0

res3:
	data 0

res4:
	data 0