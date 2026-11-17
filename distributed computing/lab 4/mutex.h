#ifndef PA4_MUTEX_H
#define PA4_MUTEX_H

#include "process.h"

int process_message(ProcessInfo *info);
int wait_for_children(ProcessInfo *info, const uint8_t flags[]);
int do_work(ProcessInfo *info);

#endif
