# load/store
mov r0, [m2]
mov [m1], r0
mov r0, 2
mov [m2], r0
halt

.data
m1:
	data 1
m2:
	data 99

