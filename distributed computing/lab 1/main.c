#include "ipc.h"
#include "process.h"
#include "logger.h"
#include "child.h"

#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/wait.h>

static void close_unused_pipes(ProcessInfo *info) {
    for (int i = 0; i < info->num_processes; i++) {
        for (int j = 0; j < info->num_processes; j++) {
            if (i == j) continue;
            if (i != info->id)
                close(info->pipes[i][j][1]);
            if (j != info->id)
                close(info->pipes[i][j][0]);
        }
    }
}

int main(int argc, char *argv[]) {
    int num_children = 0;
    ProcessInfo info;

    for (int i = 1; i < argc; i++) {
        if (strcmp(argv[i], "-p") == 0 && i + 1 < argc) {
            num_children = atoi(argv[i + 1]);
            break;
        }
    }

    if (num_children <= 0 || num_children > MAX_PROCESS_ID)
        return 1;

    memset(&info, 0, sizeof(info));
    info.id = PARENT_ID;
    info.num_processes = num_children + 1;

    for (int i = 0; i < info.num_processes; i++) {
        for (int j = 0; j < info.num_processes; j++) {
            if (i == j) continue;
            int fd[2];
            if (pipe(fd) < 0)
                return 1;
            info.pipes[i][j][0] = fd[0];
            info.pipes[i][j][1] = fd[1];
        }
    }

    log_init();
    log_pipes(&info);

    for (local_id i = 1; i <= num_children; i++) {
        pid_t pid = fork();
        if (pid < 0)
            return 1;
        if (pid == 0) {
            info.id = i;
            close_unused_pipes(&info);
            child_work(&info);
        }
    }

    close_unused_pipes(&info);

    Message msg;
    for (local_id j = 1; j < info.num_processes; j++)
        receive(&info, j, &msg);

    for (local_id j = 1; j < info.num_processes; j++)
        receive(&info, j, &msg);

    for (int i = 0; i < num_children; i++)
        wait(NULL);

    log_close();
    return 0;
}
