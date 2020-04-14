cmp r0, 1
blt lt
halt
lt:
mov r0, 200
cmp r0, 10
bgt gt
halt
gt:
mov r0, 99
mov [out], r0

.data
out:
	data 0