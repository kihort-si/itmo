#include "mutex.h"
#include "pa2345.h"

#include <stdio.h>
#include <unistd.h>

/* The supplied runtime prints to fd 2. Keep its character-by-character print()
 * while exposing useful output on stdout and leaving stderr empty on success. */
static int print_iteration(const char *buffer) {
    int saved_stderr = dup(STDERR_FILENO);
    if (saved_stderr < 0)
        return -1;
    if (dup2(STDOUT_FILENO, STDERR_FILENO) < 0) {
        close(saved_stderr);
        return -1;
    }
    print(buffer);
    int result = dup2(saved_stderr, STDERR_FILENO);
    close(saved_stderr);
    return result < 0 ? -1 : 0;
}

int do_work(ProcessInfo *info) {
    const int iterations = info->id * 5;
    for (int iteration = 1; iteration <= iterations; iteration++) {
        char buffer[128];
        int length = snprintf(buffer, sizeof(buffer), log_loop_operation_fmt,
                              info->id, iteration, iterations);
        if (length < 0 || (size_t)length >= sizeof(buffer))
            return -1;
        if (info->mutex_enabled && request_cs(info) != 0)
            return -1;
        if (print_iteration(buffer) != 0)
            return -1;
        if (info->mutex_enabled && release_cs(info) != 0)
            return -1;
    }
    return 0;
}
