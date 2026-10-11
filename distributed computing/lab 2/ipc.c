#include "ipc.h"
#include "process.h"

#include <unistd.h>
#include <errno.h>
#include <string.h>

enum {
    RECEIVE_READY = 0,
    RECEIVE_AGAIN = 1,
    RECEIVE_CLOSED = 2,
    RECEIVE_ERROR = -1
};

static int receive_progress(ProcessInfo *info, local_id from, Message *msg) {
    int fd = info->pipes[from][info->id][0];
    Message *pending = &info->rx_messages[(int)from];
    size_t *received = &info->rx_received[(int)from];
    size_t *expected = &info->rx_expected[(int)from];

    if (info->rx_closed[(int)from])
        return RECEIVE_CLOSED;

    if (*expected == 0)
        *expected = sizeof(MessageHeader);

    while (1) {
        ssize_t result = read(fd, (char *)pending + *received,
                              *expected - *received);

        if (result > 0) {
            *received += (size_t)result;

            if (*received == sizeof(MessageHeader) &&
                *expected == sizeof(MessageHeader)) {
                if (pending->s_header.s_magic != MESSAGE_MAGIC ||
                    pending->s_header.s_payload_len > MAX_PAYLOAD_LEN) {
                    return RECEIVE_ERROR;
                }
                *expected = sizeof(MessageHeader) +
                            pending->s_header.s_payload_len;
            }

            if (*received == *expected) {
                memcpy(msg, pending, *expected);
                *received = 0;
                *expected = 0;
                return RECEIVE_READY;
            }
            continue;
        }

        if (result == 0) {
            info->rx_closed[(int)from] = 1;
            return *received == 0 ? RECEIVE_CLOSED : RECEIVE_ERROR;
        }

        if (errno == EINTR)
            continue;
        if (errno == EAGAIN || errno == EWOULDBLOCK)
            return RECEIVE_AGAIN;
        return RECEIVE_ERROR;
    }
}

int send(void *self, local_id dst, const Message *msg) {
    ProcessInfo *info = (ProcessInfo *)self;
    int fd = info->pipes[info->id][dst][1];
    size_t total = sizeof(MessageHeader) + msg->s_header.s_payload_len;
    const char *buf = (const char *)msg;
    size_t written = 0;

    while (written < total) {
        ssize_t res = write(fd, buf + written, total - written);
        if (res > 0) {
            written += (size_t)res;
            continue;
        }
        if (res < 0 && errno == EINTR)
            continue;
        if (res < 0 && (errno == EAGAIN || errno == EWOULDBLOCK))
            continue;
        return -1;
    }
    return 0;
}

int send_multicast(void *self, const Message *msg) {
    ProcessInfo *info = (ProcessInfo *)self;
    for (local_id j = 0; j < info->num_processes; j++) {
        if (j == info->id) continue;
        if (send(self, j, msg) != 0) return -1;
    }
    return 0;
}

int receive(void *self, local_id from, Message *msg) {
    ProcessInfo *info = (ProcessInfo *)self;
    while (1) {
        int status = receive_progress(info, from, msg);
        if (status == RECEIVE_READY)
            return 0;
        if (status == RECEIVE_CLOSED || status == RECEIVE_ERROR)
            return -1;
    }
}

int receive_any(void *self, Message *msg) {
    ProcessInfo *info = (ProcessInfo *)self;

    while (1) {
        int open_count = 0;
        for (local_id from = 0; from < info->num_processes; from++) {
            if (from == info->id) continue;
            if (info->rx_closed[(int)from]) continue;

            open_count++;
            int status = receive_progress(info, from, msg);
            if (status == RECEIVE_READY)
                return 0;
            if (status == RECEIVE_ERROR)
                return -1;
        }
        if (open_count == 0)
            return -1;
    }
}
