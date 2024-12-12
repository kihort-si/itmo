/* fork-example-4.c */

#include <stdio.h>
#include <stdlib.h>
#include <sys/mman.h>
#include <sys/wait.h>
#include <unistd.h>
#include <semaphore.h>
#include <fcntl.h>
#include <string.h>

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

    sem_t* sem = sem_open("sem_example", O_CREAT | O_EXCL, 0644, 0);
    if (sem == SEM_FAILED) {
        perror("sem_open");
        exit(EXIT_FAILURE);
    }

    int pid = fork();

    if (pid == 0) {
        int index, new_value;
        while (1) {
            printf("Enter index (0-9) and new value (negative index to quit): ");
            scanf("%d %d", &index, &new_value);

            if (index < 0) {
                break;
            }

            if (index >= 0 && index < 10) {
                shared_array[index] = new_value;

                sem_post(sem);
            } else {
                printf("Invalid index!\n");
            }
        }

        sem_close(sem);
        sem_unlink("sem_example");
        exit(0);
    } else if (pid > 0) {
        while (1) {
            sem_wait(sem);

            printf("Array after modification:\n");
            for (int i = 0; i < 10; i++) {
                printf("%d ", shared_array[i]);
            }
            printf("\n");
        }

        wait(NULL);

        sem_close(sem);
        sem_unlink("sem_example");

        munmap(shared_array, array_size);
    } else {
        perror("fork");
        return 1;
    }

    return 0;
}
