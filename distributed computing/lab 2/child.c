#include "child.h"
#include "ipc.h"
#include "banking.h"
#include "pa2345.h"
#include "logger.h"

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>

static void fill_header(Message *msg, int16_t type, uint16_t payload_len) {
    msg->s_header.s_magic       = MESSAGE_MAGIC;
    msg->s_header.s_type        = type;
    msg->s_header.s_payload_len = payload_len;
    msg->s_header.s_local_time  = get_physical_time();
}

static void fill_gaps(ProcessInfo *info, timestamp_t up_to) {
    timestamp_t end = up_to;
    uint16_t new_len;

    if (end < 0)
        return;
    if (end >= MAX_T)
        end = MAX_T - 1;

    for (timestamp_t i = info->history.s_history_len; i <= end; i++) {
        info->history.s_history[i].s_balance            = info->balance;
        info->history.s_history[i].s_time               = i;
        info->history.s_history[i].s_balance_pending_in = 0;
    }

    new_len = (uint16_t)end + 1;
    if (new_len > info->history.s_history_len)
        info->history.s_history_len = (uint8_t)new_len;
}

static void record_balance(ProcessInfo *info, timestamp_t t) {
    if (t < 0)
        return;
    if (t >= MAX_T)
        t = MAX_T - 1;
    fill_gaps(info, t);
    info->history.s_history[t].s_balance = info->balance;
}

static void handle_transfer(ProcessInfo *info, Message *msg) {
    TransferOrder order;

    if (msg->s_header.s_payload_len != sizeof(TransferOrder))
        exit(1);
    memcpy(&order, msg->s_payload, sizeof(TransferOrder));

    if (order.s_src <= PARENT_ID || order.s_src >= info->num_processes ||
        order.s_dst <= PARENT_ID || order.s_dst >= info->num_processes ||
        order.s_src == order.s_dst || order.s_amount <= 0 ||
        (order.s_src != info->id && order.s_dst != info->id))
        exit(1);

    if (order.s_src == info->id) {
        timestamp_t t = get_physical_time();
        fill_gaps(info, t > 0 ? t - 1 : 0);
        info->balance -= order.s_amount;
        record_balance(info, t);

        char buf[64];
        int len = snprintf(buf, sizeof(buf), log_transfer_out_fmt,
                           t, info->id, order.s_amount, order.s_dst);
        log_event("%s", buf);
        (void)len;

        fill_header(msg, TRANSFER, sizeof(TransferOrder));
        memcpy(msg->s_payload, &order, sizeof(TransferOrder));
        if (send(info, order.s_dst, msg) != 0)
            exit(1);
    } else {
        timestamp_t t = get_physical_time();
        fill_gaps(info, t > 0 ? t - 1 : 0);
        info->balance += order.s_amount;
        record_balance(info, t);

        char buf[64];
        int len = snprintf(buf, sizeof(buf), log_transfer_in_fmt,
                           t, info->id, order.s_amount, order.s_src);
        log_event("%s", buf);
        (void)len;

        Message ack;
        fill_header(&ack, ACK, 0);
        if (send(info, PARENT_ID, &ack) != 0)
            exit(1);
    }
}

void child_work(ProcessInfo *info) {
    char buf[MAX_PAYLOAD_LEN];
    Message msg;
    int len;
    int done_count = 0;

    info->history.s_id          = info->id;
    info->history.s_history_len = 0;

    timestamp_t t0 = get_physical_time();
    fill_gaps(info, t0);

    len = snprintf(buf, sizeof(buf), log_started_fmt,
                   (int)get_physical_time(), info->id,
                   getpid(), getppid(), info->balance);
    log_event("%s", buf);

    fill_header(&msg, STARTED, (uint16_t)len);
    memcpy(msg.s_payload, buf, (size_t)len);
    if (send_multicast(info, &msg) != 0)
        exit(1);

    for (local_id j = 1; j < info->num_processes; j++) {
        if (j == info->id) continue;
        if (receive(info, j, &msg) != 0 ||
            msg.s_header.s_type != STARTED)
            exit(1);
    }
    log_event(log_received_all_started_fmt,
              (int)get_physical_time(), info->id);

    while (1) {
        if (receive_any(info, &msg) != 0)
            exit(1);
        if (msg.s_header.s_type == TRANSFER) {
            handle_transfer(info, &msg);
        } else if (msg.s_header.s_type == DONE) {
            done_count++;
        } else if (msg.s_header.s_type == STOP) {
            break;
        }
    }

    timestamp_t t_done = get_physical_time();
    fill_gaps(info, t_done);

    len = snprintf(buf, sizeof(buf), log_done_fmt,
                   (int)t_done, info->id, info->balance);
    log_event("%s", buf);

    fill_header(&msg, DONE, (uint16_t)len);
    memcpy(msg.s_payload, buf, (size_t)len);
    if (send_multicast(info, &msg) != 0)
        exit(1);

    int need = info->num_processes - 2;
    while (done_count < need) {
        if (receive_any(info, &msg) != 0)
            exit(1);
        if (msg.s_header.s_type == DONE) {
            done_count++;
        } else if (msg.s_header.s_type == TRANSFER) {
            handle_transfer(info, &msg);
        }
    }

    log_event(log_received_all_done_fmt,
              (int)get_physical_time(), info->id);

    timestamp_t t_final = get_physical_time();
    fill_gaps(info, t_final);

    uint16_t hist_payload = (uint16_t)(
        sizeof(local_id) + sizeof(uint8_t) +
        info->history.s_history_len * sizeof(BalanceState));
    fill_header(&msg, BALANCE_HISTORY, hist_payload);
    memcpy(msg.s_payload, &info->history, hist_payload);
    if (send(info, PARENT_ID, &msg) != 0)
        exit(1);

    log_close();
    exit(0);
}
