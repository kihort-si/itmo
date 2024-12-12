/* fork-example-1.c */

#include <stdio.h>
#include <stdlib.h>
#include <sys/mman.h>
#include <sys/wait.h>
#include <unistd.h>

void* create_shared_memory(size_t size) {
    return mmap(NULL,
                size,
                PROT_READ | PROT_WRITE,
                MAP_SHARED | MAP_ANONYMOUS,
                -1, 0);
}

int main() {
    size_t array_size = 10 * sizeof(int);
    int* shared_array = (int*)create_shared_memory(array_size);

    for (int i = 0; i < 10; i++) {
        shared_array[i] = i + 1;
    }

    int pid = fork();

    if (pid == 0) {
        int index, new_value;
        printf("Enter index (0-9) and new value: ");
        scanf("%d %d", &index, &new_value);

        if (index >= 0 && index < 10) {
            shared_array[index] = new_value;
        } else {
            printf("Invalid index!\n");
        }

        exit(0);
    } else if (pid > 0) {
        wait(NULL);

        printf("Array after modification:\n");
        for (int i = 0; i < 10; i++) {
            printf("%d ", shared_array[i]);
        }
        printf("\n");

        munmap(shared_array, array_size);
    } else {
        perror("fork");
        return 1;
    }

    return 0;
}
