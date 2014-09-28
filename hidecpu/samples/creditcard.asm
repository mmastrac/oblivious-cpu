# double every second digit
	mov r0, 15
doubler:
	mov r1, rol [data + r0]
	cmp r1, 10
	blt no_sub
# if n * 2 >= 10, we sum the sum of the digits instead of n
	sub r1, 9
no_sub:
	mov [data + r0], r1
	sub r0, 1
	loop r0, doubler

# sum all the digits
	clc
	mov r1, 0
	mov r0, 16
adder:
	add r1, [data + r0]
	loop r0, adder 
	
	mov [sum], r1
	
	# Now check that the sum % 10 == 0
check:
	cmp r1, 10
	blt done
	sub r1, 10
	jump check

done:
	mov [res], r1
	halt

sum:
	data 0
res:
	data 0
	
data:
	data 5, 4, 9, 7, 0, 3, 6, 5, 0, 2, 1, 6, 1, 6, 1, 1
