//
// Created by Nikita on 03.10.2025.
//

#include "shell.h"

#include <dirent.h>
#include <errno.h>
#include <fcntl.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/wait.h>
#include <termios.h>
#include <unistd.h>

#include "util.h"

void executeCommand(
    Shell* shell, const char* command, const char* full_command
) {
  if (command == NULL || command[0] == '\0' || full_command == NULL || full_command[0] == '\0') {
    return;
  }

  int has_non_whitespace = 0;
  for (const char* p = command; *p; p++) {
    if (*p != ' ' && *p != '\t' && *p != '\n' && *p != '\r') {
      has_non_whitespace = 1;
      break;
    }
  }

  if (!has_non_whitespace) {
    return;
  }

  char command_copy[MAX_BUF_SIZE];
  strcpy(command_copy, full_command);

  int background = 0;
  int len = strlen(command_copy);
  if (len > 0 && command_copy[len - 1] == '&') {
    background = 1;
    command_copy[len - 1] = '\0';
    trim(command_copy);
  }

  double start_time = getCurrentTime();

  struct termios old_termios;
  tcgetattr(STDIN_FILENO, &old_termios);

  pid_t pid = fork();

  if (pid == 0) {
    char* args[MAX_BUF_SIZE];
    char cmd_copy[MAX_BUF_SIZE];
    strcpy(cmd_copy, command_copy);

    int argc = 0;
    char* token = strtok(cmd_copy, " ");

    int output_redirect = 0;
    int input_redirect = 0;
    char* output_file = NULL;
    char* input_file = NULL;

    while (token != NULL && argc < MAX_BUF_SIZE - 1) {
      if (strcmp(token, ">") == 0) {
        output_redirect = 1;
        token = strtok(NULL, " ");
        if (token)
          output_file = token;
      } else if (strcmp(token, ">>") == 0) {
        output_redirect = 2;
        token = strtok(NULL, " ");
        if (token)
          output_file = token;
      } else if (strcmp(token, "<") == 0) {
        input_redirect = 1;
        token = strtok(NULL, " ");
        if (token)
          input_file = token;
      } else {
        args[argc++] = token;
      }
      token = strtok(NULL, " ");
    }
    args[argc] = NULL;

    if (output_redirect && output_file) {
      int fd;
      if (output_redirect == 1) {
        fd = open(output_file, O_WRONLY | O_CREAT | O_TRUNC, 0644);
      } else {
        fd = open(output_file, O_WRONLY | O_CREAT | O_APPEND, 0644);
      }
      if (fd != -1) {
        dup2(fd, STDOUT_FILENO);
        close(fd);
      }
    }

    if (input_redirect && input_file) {
      int fd = open(input_file, O_RDONLY);
      if (fd != -1) {
        dup2(fd, STDIN_FILENO);
        close(fd);
      }
    }

    execvp(args[0], args);

    int err = errno;

    if (err == ENOENT) {
      if (args[0][0] == '/' || (args[0][0] == '.' && args[0][1] == '/')) {
        fprintf(stderr, "%s: %s\n", args[0], strerror(err));
      } else {
        fprintf(stdout, "Command not found\n");
      }
    } else {
      fprintf(stderr, "%s: %s\n", args[0], strerror(err));
    }
    fflush(stdout);
    fflush(stderr);


    _exit(127);
  } else if (pid > 0) {
    if (!background) {
      int status;
      waitpid(pid, &status, 0);

      tcsetattr(STDIN_FILENO, TCSAFLUSH, &old_termios);


      double end_time = getCurrentTime();
      shell->execution_time = end_time - start_time;

      if (WIFEXITED(status)) {
        fprintf(stderr,
            "\033[33mCommand executed in %.3f seconds (Exit code: %d)\033[0m\n",
            shell->execution_time,
            WEXITSTATUS(status)
        );
      } else if (WIFSIGNALED(status)) {
        fprintf(stderr,
            "\033[33mCommand terminated by signal %d in %.3f seconds\033[0m\n",
            WTERMSIG(status),
            shell->execution_time
        );
      }
    } else {
      printf("[%d] Background process started\n", pid);
    }
  } else {
    perror("fork failed");
  }
}

void execute_command_list(Shell* shell, char* command_list) {
  char* cmd = strtok(command_list, ";");

  while (cmd != NULL) {
    char trimmed_cmd[MAX_BUF_SIZE];
    strcpy(trimmed_cmd, cmd);
    trim(trimmed_cmd);

    if (strlen(trimmed_cmd) > 0) {
      char command[MAX_BUF_SIZE];
      char args[MAX_BUF_SIZE];

      char* space_pos = strchr(trimmed_cmd, ' ');
      if (space_pos != NULL) {
        *space_pos = '\0';
        strcpy(command, trimmed_cmd);
        strcpy(args, space_pos + 1);
      } else {
        strcpy(command, trimmed_cmd);
        args[0] = '\0';
      }

      if (strcmp(command, "exit") == 0) {
        shell_exit();
      } else if (strcmp(command, "cd") == 0) {
        shell_cd(shell, strlen(args) > 0 ? args : NULL);
      } else if (strcmp(command, "history") == 0) {
        shell_history(shell);
      } else {
        char full_command[MAX_BUF_SIZE];
        if (strlen(args) > 0) {
          snprintf(full_command, MAX_BUF_SIZE, "%s %s", command, args);
        } else {
          strcpy(full_command, command);
        }
        executeCommand(shell, command, full_command);
      }
    }

    cmd = strtok(NULL, ";");
  }
}

void shell_cd(Shell* shell, const char* args) {
  if (args == NULL || strlen(args) == 0) {
    fprintf(stderr, "cd: missing argument\n");
    return;
  }

  if (chdir(args) == -1) {
    fprintf(stderr, "cd: cannot change to '%s': %s\n", args, strerror(errno));
  } else {
    if (getcwd(shell->current_path, PATH_MAX) == NULL) {
      fprintf(
          stderr, "cd: failed to get current directory: %s\n", strerror(errno)
      );
    } else {
      printf("Changed directory to: %s\n", shell->current_path);
    }
  }
}

void shell_history(Shell* shell) {
  if (shell->history_count == 0) {
    printf("No commands in history\n");
    return;
  }

  printf("Command history:\n");

  int start = (shell->history_count > MAX_HISTORY_SIZE) ? shell->history_start : 0;
  int count = (shell->history_count > MAX_HISTORY_SIZE) ? MAX_HISTORY_SIZE : shell->history_count;

  for (int i = 0; i < count; i++) {
    int index = (start + i) % MAX_HISTORY_SIZE;
    int cmd_number = (shell->history_count > MAX_HISTORY_SIZE) ?
                     (shell->history_count - MAX_HISTORY_SIZE + i + 1) : (i + 1);
    printf("%4d  %s\n", cmd_number, shell->history[index]);
  }
}

void add_to_history(Shell* shell, const char* command) {
  if (strlen(command) == 0) {
    return;
  }

  if (strcmp(command, "history") == 0) {
    return;
  }

  int index;
  if (shell->history_count < MAX_HISTORY_SIZE) {
    index = shell->history_count;
    shell->history_count++;
  } else {
    index = shell->history_start;
    shell->history_start = (shell->history_start + 1) % MAX_HISTORY_SIZE;
    shell->history_count++;
  }

  strncpy(shell->history[index], command, MAX_BUF_SIZE - 1);
  shell->history[index][MAX_BUF_SIZE - 1] = '\0';
}

void shell_exit(void) {
  exit(0);
}