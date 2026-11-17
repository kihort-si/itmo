#ifndef __LOGGER__H
#define __LOGGER__H

#include "process.h"

void log_init(void);
void log_event(const char *fmt, ...);
void log_pipes(ProcessInfo *info);
void log_close(void);

#endif
