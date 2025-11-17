#include <signal.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <termios.h>
#include <unistd.h>
#include <vtsh.h>

#include "shell.h"
#include "util.h"

struct termios original_termios;

void signal_handler(int sig) {
  if (sig == SIGINT || sig == SIGTSTP || sig == SIGTERM) {
    printf("\nИспользуйте 'exit' для выхода из программы\n");
    printf("%s$ ", getenv("PWD") ? getenv("PWD") : "");
    fflush(stdout);
  }
}
int main(void) {
  Shell shell;

  signal(SIGINT, signal_handler);
  signal(SIGTSTP, signal_handler);

  if (getcwd(shell.current_path, PATH_MAX) == NULL) {
    fprintf(stderr, "Failed to get current directory\n");
    return 1;
  }

  shell.process_count = 1;
  shell.history_count = 0;
  shell.history_start = 0;

  char* input = NULL;
  char command[MAX_BUF_SIZE];
  char args[MAX_BUF_SIZE];
  size_t input_capacity = 0;

  // printf("Unix Shell Emulator\n");
  // printf("Type 'exit' to quit\n\n");

  while (1) {
    if (getcwd(shell.current_path, PATH_MAX) == NULL) {
      fprintf(stderr, "Failed to get current directory\n");
      break;
    }

    fflush(stdout);

    ssize_t read_result = readline(&input, &input_capacity);
    if (read_result == -1) {
      printf("\n");
      free(input);
      shell_exit();
    }

    if (read_result > 0 && input[read_result - 1] == '\n') {
      input[read_result - 1] = '\0';
    }

    trim(input);

    if (strlen(input) == 0) {
      continue;
    }

    add_to_history(&shell, input);

    execute_command_list(&shell, input);
  }

  free(input);
  return 0;
}
