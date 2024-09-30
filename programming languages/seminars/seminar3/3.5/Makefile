ASM=nasm
ASMFLAGS=-f elf64
LD=ld

%.o: %.asm
    $(ASM) $(ASMFLAGS) -o $@ $<

program: 2.o lib.o
    $(LD) -o program 2.o lib.o