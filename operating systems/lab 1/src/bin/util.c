//
// Created by Nikita on 03.10.2025.
//

#include "util.h"

#include <errno.h>
#include <stdlib.h>
#include <string.h>
#include <sys/time.h>
#include <unistd.h>

void trim(char* str) {
  if (!str) return;

  char* start = str;
  while (*start && (*start == ' ' || *start == '\t' || *start == '\n' || *start == '\r')) {
    start++;
  }

  if (start != str) {
    memmove(str, start, strlen(start) + 1);
  }

  if (*str == 0) return;

  char* end = str + strlen(str) - 1;
  while (end > str && (*end == ' ' || *end == '\t' || *end == '\n' || *end == '\r')) {
    end--;
  }
  *(end + 1) = 0;
}

double getCurrentTime(void) {
  struct timeval tv;
  gettimeofday(&tv, NULL);
  return tv.tv_sec + tv.tv_usec / 1000000.0;
}

ssize_t readline(char** line, size_t* capacity) {
  if (*line == NULL || *capacity == 0) {
    *capacity = 128;
    *line = malloc(*capacity);
    if (*line == NULL) {
      return -1;
    }
  }

  size_t length = 0;

  for (;;) {
    char ch = 0;
    ssize_t result = read(STDIN_FILENO, &ch, 1);
    if (result == -1) {
      if (errno == EINTR) {
        continue;
      }
      return -1;
    }

    if (result == 0) {
      if (length == 0) {
        return -1;
      }
      break;
    }

    if (length + 1 >= *capacity) {
      size_t new_capacity =
          (*capacity < 256) ? (*capacity * 2) : (*capacity + 256);
      char* resized = realloc(*line, new_capacity);
      if (resized == NULL) {
        return -1;
      }
      *line = resized;
      *capacity = new_capacity;
    }

    (*line)[length++] = ch;
    if (ch == '\n') {
      break;
    }
  }

  (*line)[length] = '\0';
  return (ssize_t)length;
}
