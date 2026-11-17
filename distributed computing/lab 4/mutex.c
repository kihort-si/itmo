#include "mutex.h"
#include "pa2345.h"
#include "lamport.h"

#include <string.h>

/* Only children execute critical sections; the parent is an observer. */
static int multicast_children(ProcessInfo *info, const Message *message) {
    for (local_id peer = 1; peer < info->num_processes; peer++) {
        if (peer != info->id && send(info, peer, message) != 0)
            return -1;
    }
    return 0;
}

int process_message(ProcessInfo *info) {
    Message message;
    if (receive_any(info, &message) != 0)
        return -1;
    local_id from = info->last_sender;
    if (from <= PARENT_ID || from >= info->num_processes)
        return -1;
    switch (message.s_header.s_type) {
        case STARTED:
            info->started[(int)from] = 1;
            return 0;
        case DONE:
            info->done[(int)from] = 1;
            return 0;
        case CS_REQUEST: {
            if (message.s_header.s_payload_len != 0)
                return -1;
            info->request_active[(int)from] = 1;
            info->request_time[(int)from] = message.s_header.s_local_time;
            /* Lamport replies immediately, even with a request of its own. */
            Message reply;
            message_init(&reply, CS_REPLY, NULL, 0);
            return send(info, from, &reply);
        }
        case CS_REPLY:
            if (message.s_header.s_payload_len != 0)
                return -1;
            info->replies[(int)from] = 1;
            return 0;
        case CS_RELEASE:
            if (message.s_header.s_payload_len != 0)
                return -1;
            info->request_active[(int)from] = 0;
            return 0;
        default:
            return -1;
    }
}

int wait_for_children(ProcessInfo *info, const uint8_t flags[]) {
    for (;;) {
        int ready = 1;
        for (local_id peer = 1; peer < info->num_processes; peer++) {
            if (peer != info->id && !flags[(int)peer])
                ready = 0;
        }
        if (ready)
            return 0;
        if (process_message(info) != 0)
            return -1;
    }
}

static int may_enter(const ProcessInfo *info) {
    timestamp_t own_time = info->request_time[(int)info->id];
    for (local_id peer = 1; peer < info->num_processes; peer++) {
        if (peer == info->id)
            continue;
        if (!info->replies[(int)peer])
            return 0;
        if (info->request_active[(int)peer] &&
            (info->request_time[(int)peer] < own_time ||
             (info->request_time[(int)peer] == own_time && peer < info->id)))
            return 0;
    }
    return 1;
}

int request_cs(const void *self) {
    ProcessInfo *info = (ProcessInfo *)self;
    if (info->id == PARENT_ID || info->requesting)
        return -1;
    Message request;
    message_init(&request, CS_REQUEST, NULL, 0);
    memset(info->replies, 0, sizeof(info->replies));
    info->requesting = 1;
    info->request_active[(int)info->id] = 1;
    info->request_time[(int)info->id] = request.s_header.s_local_time;
    if (multicast_children(info, &request) != 0)
        return -1;
    while (!may_enter(info)) {
        if (process_message(info) != 0)
            return -1;
    }
    return 0;
}

int release_cs(const void *self) {
    ProcessInfo *info = (ProcessInfo *)self;
    if (!info->requesting)
        return -1;
    Message release;
    message_init(&release, CS_RELEASE, NULL, 0);
    info->request_active[(int)info->id] = 0;
    info->requesting = 0;
    return multicast_children(info, &release);
}

