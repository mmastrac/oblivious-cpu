mov r0, pointers
mov r2, 4
loop:
mov r1, [r0]
mov r1, [r1]
add r3, r1
add r0, 1
loop r2, loop
sub r3, 1 # 10+20+40+30-1=99
mov [out], r3
halt

.data
out: data 0
pointers: data p1, p2, p3, p4
p1: data 10
p2: data 20
p4: data 40
p3: data 30
