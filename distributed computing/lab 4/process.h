#ifndef PA4_PROCESS_H
#define PA4_PROCESS_H

#include "ipc.h"

typedef struct {
    local_id id;
    int num_processes;
    int mutex_enabled;
    int pipes[MAX_PROCESS_ID + 1][MAX_PROCESS_ID + 1][2];
    Message rx_messages[MAX_PROCESS_ID + 1];
    size_t rx_received[MAX_PROCESS_ID + 1];
    size_t rx_expected[MAX_PROCESS_ID + 1];
    uint8_t rx_closed[MAX_PROCESS_ID + 1];
    local_id last_sender;
    local_id next_sender;
    uint8_t started[MAX_PROCESS_ID + 1];
    uint8_t done[MAX_PROCESS_ID + 1];
    uint8_t request_active[MAX_PROCESS_ID + 1];
    timestamp_t request_time[MAX_PROCESS_ID + 1];
    uint8_t replies[MAX_PROCESS_ID + 1];
    int requesting;
} ProcessInfo;

#endif
