#include "banking.h"
#include "ipc.h"
#include "process.h"
#include "logger.h"
#include "child.h"
#include "pa2345.h"

#include <errno.h>
#include <fcntl.h>
#include <limits.h>
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

static int set_nonblocking(int fd) {
    int flags = fcntl(fd, F_GETFL, 0);
    if (flags == -1)
        return -1;
    return fcntl(fd, F_SETFL, flags | O_NONBLOCK);
}

static int parse_balance(const char *text, balance_t *balance) {
    char *end;
    long value;

    errno = 0;
    value = strtol(text, &end, 10);
    if (errno != 0 || *text == '\0' || *end != '\0' ||
        value < 1 || value > 99 || value > INT16_MAX)
        return -1;
    *balance = (balance_t)value;
    return 0;
}

void transfer(void *parent_data, local_id src, local_id dst, balance_t amount) {
    ProcessInfo *info = (ProcessInfo *)parent_data;
    Message msg;
    TransferOrder order;

    order.s_src    = src;
    order.s_dst    = dst;
    order.s_amount = amount;

    msg.s_header.s_magic       = MESSAGE_MAGIC;
    msg.s_header.s_type        = TRANSFER;
    msg.s_header.s_payload_len = sizeof(TransferOrder);
    msg.s_header.s_local_time  = get_physical_time();
    memcpy(msg.s_payload, &order, sizeof(TransferOrder));

    if (send(info, src, &msg) != 0)
        exit(1);

    Message ack;
    if (receive(info, dst, &ack) != 0 ||
        ack.s_header.s_type != ACK || ack.s_header.s_payload_len != 0)
        exit(1);
}

int main(int argc, char *argv[]) {
    int num_children = 0;
    int balance_start = 0;
    balance_t balances[MAX_PROCESS_ID + 1];

    for (int i = 1; i < argc; i++) {
        if (strcmp(argv[i], "-p") == 0 && i + 1 < argc) {
            num_children = atoi(argv[i + 1]);
            balance_start = i + 2;
            break;
        }
    }

    if (num_children < 2 || num_children > 10 || balance_start == 0 ||
        argc < balance_start + num_children)
        return 1;

    for (int i = 1; i <= num_children; i++) {
        if (parse_balance(argv[balance_start + i - 1], &balances[i]) != 0)
            return 1;
    }

    ProcessInfo info;
    memset(&info, 0, sizeof(info));
    info.id = PARENT_ID;
    info.num_processes = num_children + 1;

    for (int i = 0; i < info.num_processes; i++) {
        for (int j = 0; j < info.num_processes; j++) {
            if (i == j) continue;
            int fd[2];
            if (pipe(fd) < 0) return 1;
            if (set_nonblocking(fd[0]) != 0 ||
                set_nonblocking(fd[1]) != 0)
                return 1;
            info.pipes[i][j][0] = fd[0];
            info.pipes[i][j][1] = fd[1];
        }
    }

    log_init();
    log_pipes(&info);

    for (local_id i = 1; i <= num_children; i++) {
        pid_t pid = fork();
        if (pid < 0) return 1;
        if (pid == 0) {
            info.id = i;
            info.balance = balances[(int)i];
            close_unused_pipes(&info);
            child_work(&info);
        }
    }

    close_unused_pipes(&info);

    Message msg;
    for (local_id j = 1; j < info.num_processes; j++)
        if (receive(&info, j, &msg) != 0 ||
            msg.s_header.s_type != STARTED)
            return 1;

    log_event(log_received_all_started_fmt,
              (int)get_physical_time(), PARENT_ID);

    bank_robbery(&info, num_children);

    msg.s_header.s_magic       = MESSAGE_MAGIC;
    msg.s_header.s_type        = STOP;
    msg.s_header.s_payload_len = 0;
    msg.s_header.s_local_time  = get_physical_time();
    if (send_multicast(&info, &msg) != 0)
        return 1;

    for (local_id j = 1; j < info.num_processes; j++)
        if (receive(&info, j, &msg) != 0 ||
            msg.s_header.s_type != DONE)
            return 1;

    log_event(log_received_all_done_fmt,
              (int)get_physical_time(), PARENT_ID);

    AllHistory all;
    memset(&all, 0, sizeof(all));
    all.s_history_len = (uint8_t)num_children;
    for (local_id j = 1; j < info.num_processes; j++) {
        if (receive(&info, j, &msg) != 0 ||
            msg.s_header.s_type != BALANCE_HISTORY ||
            msg.s_header.s_payload_len > sizeof(BalanceHistory) ||
            msg.s_header.s_payload_len < sizeof(local_id) + sizeof(uint8_t))
            return 1;
        memcpy(&all.s_history[j - 1], msg.s_payload,
               msg.s_header.s_payload_len);
    }

    print_history(&all);

    for (int i = 0; i < num_children; i++)
        wait(NULL);

    log_close();
    return 0;
}
