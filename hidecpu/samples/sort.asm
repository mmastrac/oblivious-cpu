start:
	mov r0, 0
	mov [swapcount], r0
	mov r0, list
	mov r1, [len]
	sub r1, 2
loop:
	mov [counttmp], r1

	mov r1, [r0]      # r1<-a
	cmp r1, [r0+1]    # cmp w/b
	blte noswap

	mov [swaptmp], r1 # tmp<-a
	mov r1, [r0+1]    # r1<-b
	mov [r0], r1      # a<-b
	mov r1, [swaptmp] # r1<-a
	mov [r0+1], r1    # b<-a

	mov r1, [swapcount]
	add r1, 1
	mov [swapcount], r1

noswap:
	mov r1, [counttmp]
	add r0, 1
	loop r1, loop
	mov r1, [swapcount]
	cmp r1, 0
	bne start

done:
	mov r1, 99
	mov [len], r1
	halt

swaptmp:
	data 0
counttmp:
	data 0
swapcount:
	data 0

marker1:
	data 99
len:
	data 6
marker2:
	data 99
list:
	data 66, 55, 44, 55, 88, 11
