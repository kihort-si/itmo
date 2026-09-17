#include "child.h"
#include "ipc.h"
#include "pa1.h"
#include "logger.h"

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>

static void fill_message(Message *msg, int16_t type, const char *body, uint16_t len) {
    msg->s_header.s_magic = MESSAGE_MAGIC;
    msg->s_header.s_type = type;
    msg->s_header.s_payload_len = len;
    msg->s_header.s_local_time = 0;
    if (len > 0)
        memcpy(msg->s_payload, body, len);
}

void child_work(ProcessInfo *info) {
    char buf[MAX_PAYLOAD_LEN];
    Message msg;
    int len;

    len = snprintf(buf, sizeof(buf), log_started_fmt,
                   info->id, getpid(), getppid());
    log_event("%s", buf);

    fill_message(&msg, STARTED, buf, (uint16_t)len);
    send_multicast(info, &msg);

    for (local_id j = 1; j < info->num_processes; j++) {
        if (j == info->id) continue;
        receive(info, j, &msg);
    }
    log_event(log_received_all_started_fmt, info->id);

    len = snprintf(buf, sizeof(buf), log_done_fmt, info->id);
    log_event("%s", buf);

    fill_message(&msg, DONE, buf, (uint16_t)len);
    send_multicast(info, &msg);

    for (local_id j = 1; j < info->num_processes; j++) {
        if (j == info->id) continue;
        receive(info, j, &msg);
    }
    log_event(log_received_all_done_fmt, info->id);

    log_close();
    exit(0);
}
