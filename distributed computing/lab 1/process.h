#ifndef __PROCESS__H
#define __PROCESS__H

#include "ipc.h"

typedef struct {
    local_id id;
    int      num_processes;
    int      pipes[MAX_PROCESS_ID + 1][MAX_PROCESS_ID + 1][2];
} ProcessInfo;

#endif
