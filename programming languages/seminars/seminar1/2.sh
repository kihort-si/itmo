nasm -g hello2.asm -felf64 -o hello.o
ld -o hello hello.o
./hello 1>out.txt 2>err.txt