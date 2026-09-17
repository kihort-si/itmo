#include "ipc.h"
#include "process.h"

#include <unistd.h>

int send(void *self, local_id dst, const Message *msg) {
    ProcessInfo *info = (ProcessInfo *)self;
    int fd = info->pipes[info->id][dst][1];
    size_t total = sizeof(MessageHeader) + msg->s_header.s_payload_len;
    const char *buf = (const char *)msg;
    size_t written = 0;

    while (written < total) {
        ssize_t res = write(fd, buf + written, total - written);
        if (res <= 0) return -1;
        written += (size_t)res;
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
    int fd = info->pipes[from][info->id][0];
    char *buf = (char *)msg;
    size_t to_read = sizeof(MessageHeader);
    size_t done = 0;

    while (done < to_read) {
        ssize_t res = read(fd, buf + done, to_read - done);
        if (res <= 0) return -1;
        done += (size_t)res;
    }

    size_t payload = msg->s_header.s_payload_len;
    if (payload > 0) {
        buf = msg->s_payload;
        done = 0;
        while (done < payload) {
            ssize_t res = read(fd, buf + done, payload - done);
            if (res <= 0) return -1;
            done += (size_t)res;
        }
    }
    return 0;
}

int receive_any(void *self, Message *msg) {
    ProcessInfo *info = (ProcessInfo *)self;

    while (1) {
        for (local_id from = 0; from < info->num_processes; from++) {
            if (from == info->id) {
                continue;
            }

            int fd = info->pipes[from][info->id][0];

            int flags = fcntl(fd, F_GETFL, 0);
            if (flags == -1) {
                return -1;
            }

            if (fcntl(fd, F_SETFL, flags | O_NONBLOCK) == -1) {
                return -1;
            }

            ssize_t res = read(
                fd,
                &msg->s_header,
                sizeof(MessageHeader)
            );

            int saved_errno = errno;

            if (fcntl(fd, F_SETFL, flags) == -1) {
                return -1;
            }

            errno = saved_errno;

            if (res == -1) {
                if (errno == EAGAIN || errno == EWOULDBLOCK) {
                    continue;
                }

                return -1;
            }

            if (res == 0) {
                continue;
            }

            if ((size_t)res != sizeof(MessageHeader)) {
                return -1;
            }

            if (msg->s_header.s_magic != MESSAGE_MAGIC) {
                return -1;
            }

            if (msg->s_header.s_payload_len > MAX_PAYLOAD_LEN) {
                return -1;
            }

            size_t payload = msg->s_header.s_payload_len;
            size_t done = 0;

            while (done < payload) {
                res = read(
                    fd,
                    msg->s_payload + done,
                    payload - done
                );

                if (res <= 0) {
                    return -1;
                }

                done += (size_t)res;
            }

            return 0;
        }
    }
}