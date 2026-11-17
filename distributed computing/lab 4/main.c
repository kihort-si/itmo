#include "ipc.h"
#include "process.h"
#include "logger.h"
#include "child.h"
#include "pa1.h"

#include <errno.h>
#include <fcntl.h>
#include <signal.h>
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

static int set_nonblocking(int descriptor) {
    int flags = fcntl(descriptor, F_GETFL, 0);
    if (flags < 0)
        return -1;
    return fcntl(descriptor, F_SETFL, flags | O_NONBLOCK);
}

static int parse_options(int argc, char *argv[], ProcessInfo *info) {
    int seen_p = 0;
    for (int i = 1; i < argc; i++) {
        if (strcmp(argv[i], "--mutexl") == 0 && !info->mutex_enabled) {
            info->mutex_enabled = 1;
        } else if (strcmp(argv[i], "-p") == 0 && !seen_p && i + 1 < argc) {
            char *end;
            const char *text = argv[++i];
            errno = 0;
            long count = strtol(text, &end, 10);
            if (errno || *text == '\0' || *end != '\0' ||
                count < 1 || count > MAX_PROCESS_ID)
                return -1;
            info->num_processes = (int)count + 1;
            seen_p = 1;
        } else {
            return -1;
        }
    }
    return seen_p ? 0 : -1;
}

int main(int argc, char *argv[]) {
    ProcessInfo info;
    memset(&info, 0, sizeof(info));
    info.id = PARENT_ID;
    if (parse_options(argc, argv, &info) != 0)
        return 1;
    if (signal(SIGPIPE, SIG_IGN) == SIG_ERR)
        return 1;

    for (int i = 0; i < info.num_processes; i++) {
        for (int j = 0; j < info.num_processes; j++) {
            if (i == j) continue;
            int descriptors[2];
            if (pipe(descriptors) < 0 ||
                set_nonblocking(descriptors[0]) != 0 ||
                set_nonblocking(descriptors[1]) != 0)
                return 1;
            info.pipes[i][j][0] = descriptors[0];
            info.pipes[i][j][1] = descriptors[1];
        }
    }

    log_init();
    log_pipes(&info);
    for (local_id i = 1; i < info.num_processes; i++) {
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

    Message message;
    for (local_id peer = 1; peer < info.num_processes; peer++) {
        if (receive(&info, peer, &message) != 0 ||
            message.s_header.s_type != STARTED)
            return 1;
    }
    log_event(log_received_all_started_fmt, info.id);
    for (local_id peer = 1; peer < info.num_processes; peer++) {
        if (receive(&info, peer, &message) != 0 ||
            message.s_header.s_type != DONE)
            return 1;
    }
    log_event(log_received_all_done_fmt, info.id);

    int failed = 0;
    for (int i = 1; i < info.num_processes; i++) {
        int status;
        pid_t pid;
        do {
            pid = wait(&status);
        } while (pid < 0 && errno == EINTR);
        if (pid < 0 || !WIFEXITED(status) || WEXITSTATUS(status) != 0)
            failed = 1;
    }
    log_close();
    return failed;
}
