ASM = nasm
ASMFLAGS =-felf64 -g
CFLAGS = -g -O2 -Wall -Werror -std=c17 -Wdiscarded-qualifiers -Wincompatible-pointer-types -Wint-conversion
CC = gcc
LD = gcc

hello: hello.o string.o
	$(LD) -o $@ $^

string.o: string.asm
	$(ASM) $(ASMFLAGS) -o $@ $<

hello.o: hello.c
	$(CC) -c $(CFLAGS) -o $@ $<

clean:
	rm -rf hello hello.o string.o

.PHONY: clean