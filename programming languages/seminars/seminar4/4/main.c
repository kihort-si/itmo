#include <stdio.h>
#include <stdlib.h>

extern void print_string(char*);
extern void print_file(char*);

int main(int argc, char *argv[]) {
    if (argc != 2) {
        printf("Usage: ./a.out <file_name>\n");
        exit(EXIT_FAILURE);
    }
    print_file(argv[1]);
    return 0;
}
