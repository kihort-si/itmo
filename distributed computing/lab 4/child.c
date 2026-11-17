#include "child.h"
#include "ipc.h"
#include "pa1.h"
#include "logger.h"
#include "lamport.h"
#include "mutex.h"

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

void child_work(ProcessInfo *info) {
    char buffer[MAX_PAYLOAD_LEN];
    Message message;
    int length = snprintf(buffer, sizeof(buffer), log_started_fmt,
                          info->id, (int)getpid(), (int)getppid());
    if (length < 0 || (size_t)length >= sizeof(buffer))
        exit(1);
    log_event("%s", buffer);
    message_init(&message, STARTED, buffer, (uint16_t)length);
    info->started[(int)info->id] = 1;
    if (send_multicast(info, &message) != 0 ||
        wait_for_children(info, info->started) != 0)
        exit(1);
    log_event(log_received_all_started_fmt, info->id);

    if (do_work(info) != 0)
        exit(1);

    length = snprintf(buffer, sizeof(buffer), log_done_fmt, info->id);
    if (length < 0 || (size_t)length >= sizeof(buffer))
        exit(1);
    log_event("%s", buffer);
    message_init(&message, DONE, buffer, (uint16_t)length);
    info->done[(int)info->id] = 1;
    if (send_multicast(info, &message) != 0 ||
        wait_for_children(info, info->done) != 0)
        exit(1);
    log_event(log_received_all_done_fmt, info->id);

    log_close();
    exit(0);
}
