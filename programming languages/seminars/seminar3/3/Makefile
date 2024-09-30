2.o: ../2.asm
	nasm -f elf64 -o 2.o 2.asm

lib.o: ../lib.asm
	nasm -f elf64 -o lib.o lib.asm

program: 2.o lib.o
	ld -o program 2.o lib.o
