nasm -g hello.asm -felf64 -o hello.o
ld -o hello hello.o
./hello