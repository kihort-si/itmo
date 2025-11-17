//
// Created by Nikita on 03.10.2025.
//

#ifndef VTSH_UTIL_H
#define VTSH_UTIL_H
#include <stddef.h>
#include <sys/types.h>

void trim(char* str);
double getCurrentTime(void);
ssize_t readline(char** line, size_t* capacity);
#endif  // VTSH_UTIL_H
