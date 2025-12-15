#pragma once

#include <sys/types.h>
#include <unistd.h>

int vtpc_open(const char* path, int mode, int access);
int vtpc_close(int fd);
ssize_t vtpc_read(int fd, void* buf, size_t count);
ssize_t vtpc_write(int fd, const void* buf, size_t count);
off_t vtpc_lseek(int fd, off_t offset, int whence);
int vtpc_fsync(int fd);

typedef struct {
  unsigned long long hits;
  unsigned long long misses;
  unsigned long long evictions;
  double hit_rate;
} vtpc_cache_stats_t;

void vtpc_get_cache_stats(vtpc_cache_stats_t* stats);
void vtpc_reset_cache_stats(void);
