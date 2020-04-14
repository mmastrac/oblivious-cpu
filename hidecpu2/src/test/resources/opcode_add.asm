# add
clrc
add r0, 50
setc
add r0, 48
mov [a1], r0
halt

.data
a1:
	data 0
