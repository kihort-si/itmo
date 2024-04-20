ORG 0x4CB
result:	WORD 0x05EC
stop:	WORD 0x0000
START:	CLA	
s1:	IN 5
	AND #0x40
	BEQ	s1
	IN 4
	ST (result)
	PUSH	
	CMP stop
	BEQ exit
	CLA	
s2:	IN 5
	AND	#0x40
	BEQ s2
	IN 4
	SWAB	
	OR &1
	ST (result)
	SUB &1
	SWAB	
	CMP stop
	BEQ exit
	LD (result)+
	POP	
	CLA	
	JUMP s1
exit:	LD (result)+
	HLT	