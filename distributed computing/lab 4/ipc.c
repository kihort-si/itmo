#include "ipc.h"
#include "process.h"
#include "lamport.h"

#include <errno.h>
#include <poll.h>
#include <string.h>
#include <unistd.h>

enum { RX_READY = 0, RX_AGAIN = 1, RX_CLOSED = 2, RX_ERROR = -1 };

static int wait_fd(int fd, short events) {
    struct pollfd descriptor;
    descriptor.fd = fd;
    descriptor.events = events;
    descriptor.revents = 0;
    int result;
    do {
        result = poll(&descriptor, 1, -1);
    } while (result < 0 && errno == EINTR);
    if (result <= 0 || (descriptor.revents & (POLLERR | POLLNVAL)))
        return -1;
    return 0;
}

static int receive_progress(ProcessInfo *info, local_id from, Message *msg) {
    Message *pending = &info->rx_messages[(int)from];
    size_t *received = &info->rx_received[(int)from];
    size_t *expected = &info->rx_expected[(int)from];
    if (info->rx_closed[(int)from])
        return RX_CLOSED;
    if (*expected == 0)
        *expected = sizeof(MessageHeader);
    for (;;) {
        ssize_t count = read(info->pipes[from][info->id][0],
                             (char *)pending + *received, *expected - *received);
        if (count > 0) {
            *received += (size_t)count;
            if (*received == sizeof(MessageHeader) &&
                *expected == sizeof(MessageHeader)) {
                if (pending->s_header.s_magic != MESSAGE_MAGIC ||
                    pending->s_header.s_payload_len > MAX_PAYLOAD_LEN ||
                    pending->s_header.s_local_time < 0)
                    return RX_ERROR;
                *expected += pending->s_header.s_payload_len;
            }
            if (*received == *expected) {
                memcpy(msg, pending, *expected);
                *received = *expected = 0;
                return RX_READY;
            }
            continue;
        }
        if (count == 0) {
            info->rx_closed[(int)from] = 1;
            return *received == 0 ? RX_CLOSED : RX_ERROR;
        }
        if (errno == EINTR)
            continue;
        if (errno == EAGAIN || errno == EWOULDBLOCK)
            return RX_AGAIN;
        return RX_ERROR;
    }
}

static int received_message(ProcessInfo *info, local_id from, Message *msg) {
    info->last_sender = from;
    info->next_sender = (local_id)((from + 1) % info->num_processes);
    lamport_update(msg->s_header.s_local_time);
    return 0;
}

int send(void *self, local_id dst, const Message *msg) {
    ProcessInfo *info = (ProcessInfo *)self;
    if (dst < 0 || dst >= info->num_processes || dst == info->id ||
        msg->s_header.s_magic != MESSAGE_MAGIC ||
        msg->s_header.s_payload_len > MAX_PAYLOAD_LEN)
        return -1;
    const char *bytes = (const char *)msg;
    size_t total = sizeof(MessageHeader) + msg->s_header.s_payload_len;
    size_t written = 0;
    int fd = info->pipes[info->id][dst][1];
    while (written < total) {
        ssize_t count = write(fd, bytes + written, total - written);
        if (count > 0) {
            written += (size_t)count;
            continue;
        }
        if (count < 0 && errno == EINTR)
            continue;
        if (count < 0 && (errno == EAGAIN || errno == EWOULDBLOCK)) {
            if (wait_fd(fd, POLLOUT) != 0)
                return -1;
            continue;
        }
        return -1;
    }
    return 0;
}

int send_multicast(void *self, const Message *msg) {
    ProcessInfo *info = (ProcessInfo *)self;
    for (local_id peer = 0; peer < info->num_processes; peer++) {
        if (peer != info->id && send(info, peer, msg) != 0)
            return -1;
    }
    return 0;
}

int receive(void *self, local_id from, Message *msg) {
    ProcessInfo *info = (ProcessInfo *)self;
    if (from < 0 || from >= info->num_processes || from == info->id)
        return -1;
    for (;;) {
        int status = receive_progress(info, from, msg);
        if (status == RX_READY)
            return received_message(info, from, msg);
        if (status != RX_AGAIN ||
            wait_fd(info->pipes[from][info->id][0], POLLIN) != 0)
            return -1;
    }
}

int receive_any(void *self, Message *msg) {
    ProcessInfo *info = (ProcessInfo *)self;
    for (;;) {
        struct pollfd descriptors[MAX_PROCESS_ID];
        nfds_t count = 0;
        for (int offset = 0; offset < info->num_processes; offset++) {
            local_id from = (local_id)((info->next_sender + offset) %
                                      info->num_processes);
            if (from == info->id || info->rx_closed[(int)from])
                continue;
            int status = receive_progress(info, from, msg);
            if (status == RX_READY)
                return received_message(info, from, msg);
            if (status == RX_ERROR)
                return -1;
            if (status == RX_AGAIN) {
                descriptors[count].fd = info->pipes[from][info->id][0];
                descriptors[count].events = POLLIN;
                descriptors[count].revents = 0;
                ++count;
            }
        }
        if (count == 0)
            return -1;
        int result;
        do {
            result = poll(descriptors, count, -1);
        } while (result < 0 && errno == EINTR);
        if (result < 0)
            return -1;
        for (nfds_t i = 0; i < count; i++) {
            if (descriptors[i].revents & (POLLERR | POLLNVAL))
                return -1;
        }
    }
}
