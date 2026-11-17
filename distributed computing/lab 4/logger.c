#include "logger.h"
#include "common.h"

#include <stdarg.h>
#include <stdio.h>

static FILE *events_file = NULL;
static FILE *pipes_file = NULL;

void log_init(void) {
    events_file = fopen(events_log, "w");
    pipes_file = fopen(pipes_log, "w");
}

void log_event(const char *fmt, ...) {
    va_list args;

    va_start(args, fmt);
    vprintf(fmt, args);
    va_end(args);
    fflush(stdout);

    if (events_file) {
        va_start(args, fmt);
        vfprintf(events_file, fmt, args);
        va_end(args);
        fflush(events_file);
    }
}

void log_pipes(ProcessInfo *info) {
    if (!pipes_file) return;
    for (int i = 0; i < info->num_processes; i++) {
        for (int j = 0; j < info->num_processes; j++) {
            if (i == j) continue;
            fprintf(pipes_file,
                    "Pipe from process %d to process %d: read fd = %d, write fd = %d\n",
                    i, j, info->pipes[i][j][0], info->pipes[i][j][1]);
        }
    }
    fflush(pipes_file);
}

void log_close(void) {
    if (events_file) {
        fclose(events_file);
        events_file = NULL;
    }
    if (pipes_file) {
        fclose(pipes_file);
        pipes_file = NULL;
    }
}
