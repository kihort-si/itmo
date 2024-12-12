/* fork-example-2.c */

#include <stdio.h>
#include <stdlib.h>
#include <sys/mman.h>
#include <sys/wait.h>
#include <unistd.h>
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

    int pipefd[2];
    if (pipe(pipefd) == -1) {
        perror("pipe");
        exit(EXIT_FAILURE);
    }

    int pid = fork();

    if (pid == 0) {
        close(pipefd[0]);

        int index, new_value;
        while (1) {
            printf("Enter index (0-9) and new value (negative index to quit): ");
            scanf("%d %d", &index, &new_value);

            if (index < 0) {
                break;
            }

            if (index >= 0 && index < 10) {
                shared_array[index] = new_value;

                char message[32];
                snprintf(message, sizeof(message), "Index %d updated", index);
                write(pipefd[1], message, strlen(message) + 1);
            } else {
                printf("Invalid index!\n");
            }
        }

        close(pipefd[1]);
        exit(0);
    } else if (pid > 0) {
        close(pipefd[1]);

        char buffer[32];
        while (1) {
            ssize_t count = read(pipefd[0], buffer, sizeof(buffer));
            if (count > 0) {
                printf("Message from child: %s\n", buffer);

                printf("Array after modification:\n");
                for (int i = 0; i < 10; i++) {
                    printf("%d ", shared_array[i]);
                }
                printf("\n");
            } else {
                break;
            }
        }

        close(pipefd[0]);
        wait(NULL);

        munmap(shared_array, array_size);
    } else {
        perror("fork");
        return 1;
    }

    return 0;
}
