#ifndef __PROCESS__H
#define __PROCESS__H

#include "ipc.h"
#include "banking.h"

typedef struct {
    local_id       id;
    int            num_processes;
    int            pipes[MAX_PROCESS_ID + 1][MAX_PROCESS_ID + 1][2];
    Message        rx_messages[MAX_PROCESS_ID + 1];
    size_t         rx_received[MAX_PROCESS_ID + 1];
    size_t         rx_expected[MAX_PROCESS_ID + 1];
    uint8_t        rx_closed[MAX_PROCESS_ID + 1];
    balance_t      balance;
    BalanceHistory history;
} ProcessInfo;

#endif
