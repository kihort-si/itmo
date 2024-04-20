ORG 0x070
result: WORD 0x0535
dozens: WORD 0x0000
number: WORD 0x0000
start:
IN 0x1C
ST dozens
ASL
ST &1
LD dozens
ASL
ASL
ASL
ADD &1
ST dozens
POP
IN 0x1C
ADD dozens
ST number
PUSH
CALL $function
POP
write: ST (result)+
POP
CMP 0x0007
BEQ stop
BLT stop
PUSH
CALL $function
POP
JUMP write
stop: ST (result)
LD #0x0B
OUT 0x14
LD #0x1B
OUT 0x14
LD #0x2B
OUT 0x14
LD #0x3B
OUT 0x14
LD #0x4B
OUT 0x14
LD #0x5B
OUT 0x14
LD #0x6B
OUT 0x14
LD #0x7B
OUT 0x14
CLA
LD -(result)
ADD #0x70
OUT 0x14
LD number
CMP #0x0008
BLT finish
LD -(result)
ADD #0x60
OUT 0x14
LD number
CMP #0x0032
BLT finish
LD -(result)
ADD #0x50
OUT 0x14
finish: 
CLA
ST (result)+
ST (result)+
ST (result)+
ST (result)+
HLT

ORG 0x305
function:
CLA
ST total
ST next
LD &1
ST total
while: LD total
CMP base
BMI exit
BLT exit
SUB base
ST total
LD next
INC
ST next
JUMP while
exit: LD total
ST &1
LD next
ST &2
RET
total: WORD 0x0000
next: WORD 0x0000
base: WORD 0x0007