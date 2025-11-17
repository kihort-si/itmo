//
// Created by Nikita on 03.10.2025.
//

#ifndef VTSH_SHELL_H
#define VTSH_SHELL_H

#define MAX_BUF_SIZE 1024
#define MAX_HISTORY_SIZE 100
#ifdef __APPLE__
#include <sys/syslimits.h>
#else
#include <linux/limits.h>
#endif

typedef struct {
  char current_path[PATH_MAX];
  int process_count;
  double execution_time;
  char history[MAX_HISTORY_SIZE][MAX_BUF_SIZE];
  int history_count;
  int history_start;
} Shell;

void executeCommand(Shell* shell, const char* command, const char* args);
void shell_cd(Shell* shell, const char* args);
void shell_exit(void);
void shell_history(Shell* shell);
void add_to_history(Shell* shell, const char* command);
void execute_command_list(Shell* shell, char* command_list);
#endif  // VTSH_SHELL_H
