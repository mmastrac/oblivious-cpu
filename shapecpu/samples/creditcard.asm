INITAC 0
INITPC start

start	La no1
		ROR
		STa no1
		La no3
		ROR
		STa no3
		La no5
		ROR
		STa no5
		La no7
		ROR
		STa no7
		La no9
		ROR
		STa no9
		La no11
		ROR
		STa no11
		La no13
		ROR
		STa no13
		La no15
		ROR
		STa no15
		
chk1	La no1
		CMP 10
		BMI chk3
add1	CLC
		ADD 247
		STa no1

chk3	La no3
		CMP 10
		BMI chk5
add3	CLC
		ADD 247
		STa no3
		
chk5	La no5
		CMP 10
		BMI chk7
add5	CLC
		ADD 247
		STa no5

chk7	La no7
		CMP 10
		BMI chk9
add7	CLC
		ADD 247
		STa no7
		
chk9	La no9
		CMP 10
		BMI chk11
add9	CLC
		ADD 247
		STa no9
	
chk11	La no11
		CMP 10
		BMI chk13
add11	CLC
		ADD 247
		STa no11
		
chk13	La no13
		CMP 10
		BMI chk15
add13	CLC
		ADD 247
		STa no13
		
chk15	La no15
		CMP 10
		BMI add17
add15	CLC
		ADD 247
		STa no15
				
add17	CLC
		La no1
		ADDa no3
		ADDa no5
		ADDa no7
		ADDa no9
		ADDa no11
		ADDa no13
		ADDa no15
		STa temp	
		CLC
		La no2
		ADDa no4
		ADDa no6
		ADDa no8
		ADDa no10
		ADDa no12
		ADDa no14
		STa temp2
		ADDa temp
		STa temp3
		STa res
			
sub		CLC		
		ADD 246
		CMP 10
		BMI ready
		J sub		
														
ready	STa res 
end		J end

temp	L 0
temp2	L 0
temp3	L 0
res		L 0

		L 99
no1		L 5
no2		L 4
no3		L 9
no4		L 7
no5		L 0
no6		L 3
no7		L 6
no8		L 5
no9		L 0
no10	L 2
no11	L 1
no12	L 6
no13	L 1
no14	L 6
no15	L 0
no16	L 0