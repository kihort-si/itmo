#include "child.h"
#include "ipc.h"
#include "banking.h"
#include "pa2345.h"
#include "logger.h"
#include "lamport.h"

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>

static void fill_header(Message *msg, int16_t type, uint16_t payload_len) {
    msg->s_header.s_magic       = MESSAGE_MAGIC;
    msg->s_header.s_type        = type;
    msg->s_header.s_payload_len = payload_len;
    msg->s_header.s_local_time  = lamport_tick();
}

static void fill_gaps(ProcessInfo *info, timestamp_t up_to) {
    timestamp_t end = up_to;
    uint16_t new_len;

    if (end < 0)
        return;
    /* The history length is uint8_t: at most 255 states fit on the wire. */
    if (end >= MAX_T)
        exit(1);

    for (timestamp_t i = (timestamp_t)info->history.s_history_len; i <= end; i++) {
        info->history.s_history[i].s_balance            = info->balance;
        info->history.s_history[i].s_time               = i;
        info->history.s_history[i].s_balance_pending_in = 0;
    }

    new_len = (uint16_t)end + 1;
    if (new_len > info->history.s_history_len)
        info->history.s_history_len = (uint8_t)new_len;
}

static void record_balance(ProcessInfo *info, timestamp_t t) {
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
        /* Source side: deduct balance and forward to dst */
        fill_header(msg, TRANSFER, sizeof(TransferOrder));
        timestamp_t t = get_lamport_time();
        fill_gaps(info, t > 0 ? t - 1 : 0);
        info->balance -= order.s_amount;
        record_balance(info, t);

        char buf[64];
        int len = snprintf(buf, sizeof(buf), log_transfer_out_fmt,
                           (int)t, info->id, order.s_amount, order.s_dst);
        log_event("%s", buf);
        (void)len;

        memcpy(msg->s_payload, &order, sizeof(TransferOrder));

        if (send(info, order.s_dst, msg) != 0)
            exit(1);
    } else {
        /* Destination side: receive funds and send ACK to parent */
        timestamp_t t = get_lamport_time();
        timestamp_t sent_at = msg->s_header.s_local_time;
        if (sent_at < 0 || sent_at >= t)
            exit(1);
        fill_gaps(info, t > 0 ? t - 1 : 0);
        /* In-transit funds belong to the destination from send until receive. */
        for (timestamp_t i = sent_at; i < t; i++)
            info->history.s_history[i].s_balance_pending_in += order.s_amount;
        info->balance += order.s_amount;
        record_balance(info, t);

        char buf[64];
        int len = snprintf(buf, sizeof(buf), log_transfer_in_fmt,
                           (int)t, info->id, order.s_amount, order.s_src);
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

    timestamp_t t0 = get_lamport_time();
    fill_gaps(info, t0);

    fill_header(&msg, STARTED, 0);
    len = snprintf(buf, sizeof(buf), log_started_fmt,
                   (int)get_lamport_time(), info->id,
                   getpid(), getppid(), info->balance);
    log_event("%s", buf);

    msg.s_header.s_payload_len = (uint16_t)len;
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
              (int)get_lamport_time(), info->id);

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

    fill_header(&msg, DONE, 0);
    timestamp_t t_done = get_lamport_time();
    fill_gaps(info, t_done);

    len = snprintf(buf, sizeof(buf), log_done_fmt,
                   (int)t_done, info->id, info->balance);
    log_event("%s", buf);

    msg.s_header.s_payload_len = (uint16_t)len;
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
              (int)get_lamport_time(), info->id);

    timestamp_t t_final = get_lamport_time();
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
