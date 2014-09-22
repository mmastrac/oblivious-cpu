INITAC 0
INITPC start

list    L 6
		L 5
		L 4
		L 9
		L 5

len     L 5
		 
idxa	L 0
idxb	L 1
temp	L 0
swap	L 0

start	La idxa
		STa loada
		STa loada2
		STa storeb
		La idxb
		STa cmpb
		STa loadb
		STa storea
		L 0
		STa swap
loada	La 0
cmpb	CMPa 0
		BMI noswap
loada2	La 0
		STa	temp
loadb	La 0
storeb	STa 0
		La temp
storea	STa 0
		L 1
		STa swap
noswap	La idxa
		CLC
		ADD 1
		STa idxa
		La idxb
		CLC
		ADD 1
		STa idxb
		CMPa len
		BEQ next
		J start
		
next	La swap
		BEQ end
		
		L 0
		STa idxa
		L 1
		STa idxb
		J start
		
end		J end									