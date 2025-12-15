#include "vtpc.h"

#include <errno.h>
#include <fcntl.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/stat.h>
#include <sys/types.h>

#ifndef O_DIRECT
#define O_DIRECT 0
#endif

#ifdef __APPLE__
#include <sys/fcntl.h>
#endif

#ifndef BLOCK_SIZE
#define BLOCK_SIZE 4096
#endif

#ifndef CACHE_CAPACITY
#define CACHE_CAPACITY 64
#endif

#define MAX_OPEN_FILES   1024
#define MAX_CACHE_PAGES  CACHE_CAPACITY
#define HASH_SIZE        2048

typedef struct FileCtx {
  int   used;
  int   fd;
  off_t pos;
  off_t size;
} FileCtx;

typedef struct CachePage CachePage;

struct CachePage {
  int   used;
  int   fd;
  off_t page_index;

  char *data;
  int   dirty;

  CachePage *lru_prev;
  CachePage *lru_next;

  CachePage *hash_next;
};

static size_t     g_page_size = 0;
static FileCtx    g_files[MAX_OPEN_FILES];
static CachePage  g_pages[MAX_CACHE_PAGES];

static CachePage *g_lru_head = NULL;
static CachePage *g_lru_tail = NULL;
static CachePage *g_hash[HASH_SIZE];

static unsigned long long g_cache_hits = 0;
static unsigned long long g_cache_misses = 0;
static unsigned long long g_cache_evictions = 0;

static void vtpc_init(void) {
  if (g_page_size != 0)
    return;

  g_page_size = BLOCK_SIZE;
}

static FileCtx* find_file_ctx(int fd) {
  for (int i = 0; i < MAX_OPEN_FILES; ++i) {
    if (g_files[i].used && g_files[i].fd == fd)
      return &g_files[i];
  }
  return NULL;
}

static FileCtx* alloc_file_ctx(int fd) {
  for (int i = 0; i < MAX_OPEN_FILES; ++i) {
    if (!g_files[i].used) {
      g_files[i].used = 1;
      g_files[i].fd   = fd;
      g_files[i].pos  = 0;

      struct stat st;
      if (fstat(fd, &st) == 0)
        g_files[i].size = st.st_size;
      else
        g_files[i].size = 0;

      return &g_files[i];
    }
  }
  return NULL;
}

static void free_file_ctx(int fd) {
  for (int i = 0; i < MAX_OPEN_FILES; ++i) {
    if (g_files[i].used && g_files[i].fd == fd) {
      g_files[i].used = 0;
      return;
    }
  }
}

static unsigned long hash_key(int fd, off_t page_index) {
  unsigned long x = (unsigned long)fd;
  unsigned long y = (unsigned long)page_index;
  return (x * 1315423911u ^ y * 2654435761u) % HASH_SIZE;
}

static CachePage* hash_lookup(int fd, off_t page_index) {
  unsigned long idx = hash_key(fd, page_index);
  CachePage* p = g_hash[idx];
  while (p) {
    if (p->fd == fd && p->page_index == page_index)
      return p;
    p = p->hash_next;
  }
  return NULL;
}

static void hash_insert(CachePage* page) {
  unsigned long idx = hash_key(page->fd, page->page_index);
  page->hash_next = g_hash[idx];
  g_hash[idx] = page;
}

static void hash_remove(CachePage* page) {
  unsigned long idx = hash_key(page->fd, page->page_index);
  CachePage** pp = &g_hash[idx];
  while (*pp) {
    if (*pp == page) {
      *pp = page->hash_next;
      page->hash_next = NULL;
      return;
    }
    pp = &(*pp)->hash_next;
  }
}

static void lru_insert_front(CachePage* page) {
  page->lru_prev = NULL;
  page->lru_next = g_lru_head;

  if (g_lru_head)
    g_lru_head->lru_prev = page;
  g_lru_head = page;
  if (!g_lru_tail)
    g_lru_tail = page;
}

static void lru_remove(CachePage* page) {
  if (page->lru_prev)
    page->lru_prev->lru_next = page->lru_next;
  else
    g_lru_head = page->lru_next;

  if (page->lru_next)
    page->lru_next->lru_prev = page->lru_prev;
  else
    g_lru_tail = page->lru_prev;

  page->lru_prev = page->lru_next = NULL;
}

static void lru_move_to_front(CachePage* page) {
  if (g_lru_head == page)
    return;
  lru_remove(page);
  lru_insert_front(page);
}

static int flush_page(CachePage* page) {
  if (!page->dirty)
    return 0;

  off_t  offset    = page->page_index * (off_t)g_page_size;
  size_t to_write  = g_page_size;
  size_t written   = 0;

  while (written < to_write) {
    ssize_t n = pwrite(page->fd,
                       page->data + written,
                       to_write - written,
                       offset + (off_t)written);
    if (n < 0) {
      if (errno == EINTR)
        continue;
      return -1;
    }
    if (n == 0) {
      errno = EIO;
      return -1;
    }
    written += (size_t)n;
  }

  page->dirty = 0;
  return 0;
}

static CachePage* alloc_page_struct(void) {
  for (int i = 0; i < MAX_CACHE_PAGES; ++i) {
    CachePage* p = &g_pages[i];
    if (!p->used) {
      p->used      = 1;
      p->fd        = -1;
      p->page_index = 0;
      p->dirty     = 0;
      p->lru_prev  = p->lru_next = NULL;
      p->hash_next = NULL;

      if (!p->data) {
        if (posix_memalign((void**)&p->data, g_page_size, g_page_size) != 0) {
          p->used = 0;
          return NULL;
        }
      }

      return p;
    }
  }

  CachePage* victim = g_lru_tail;
  if (!victim)
    return NULL;

  if (victim->dirty) {
    if (flush_page(victim) != 0)
      return NULL;
  }

  g_cache_evictions++;
  hash_remove(victim);
  lru_remove(victim);

  victim->fd         = -1;
  victim->page_index = 0;
  victim->dirty      = 0;
  victim->hash_next  = NULL;

  return victim;
}

static CachePage* get_page(int fd, off_t page_index) {
  vtpc_init();

  CachePage* page = hash_lookup(fd, page_index);
  if (page) {
    lru_move_to_front(page);
    g_cache_hits++;
    return page;
  }
  
  g_cache_misses++;

  page = alloc_page_struct();
  if (!page)
    return NULL;

  page->fd         = fd;
  page->page_index = page_index;
  page->dirty      = 0;

  off_t  offset   = page_index * (off_t)g_page_size;
  size_t to_read  = g_page_size;
  size_t done     = 0;

  while (done < to_read) {
    ssize_t n = pread(fd,
                      page->data + done,
                      to_read - done,
                      offset + (off_t)done);
    if (n < 0) {
      if (errno == EINTR)
        continue;
      page->used = 0;
      return NULL;
    }
    if (n == 0)
      break; /* EOF */
    done += (size_t)n;
  }

  if (done < to_read)
    memset(page->data + done, 0, to_read - done);

  hash_insert(page);
  lru_insert_front(page);

  return page;
}

int vtpc_open(const char* path, int flags, int mode) {
  vtpc_init();

  int real_flags = flags | O_DIRECT;
  int fd = open(path, real_flags, mode);
  if (fd == -1 && errno == EINVAL) {
    fd = open(path, flags, mode);
  }
  if (fd == -1)
    return -1;

  #ifdef __APPLE__
    int one = 1;
    if (fcntl(fd, F_NOCACHE, &one) == -1) {
    }
  #endif

  if (!alloc_file_ctx(fd)) {
    int saved = errno;
    close(fd);
    errno = saved;
    return -1;
  }

  return fd;
}

int vtpc_close(int fd) {
  int rc = 0;
  int saved_errno = 0;

  for (int i = 0; i < MAX_CACHE_PAGES; ++i) {
    CachePage* p = &g_pages[i];
    if (p->used && p->fd == fd) {
      if (p->dirty) {
        if (flush_page(p) != 0 && rc == 0) {
          rc = -1;
          saved_errno = errno;
        }
      }
      hash_remove(p);
      lru_remove(p);
      p->used      = 0;
      p->fd        = -1;
      p->page_index = 0;
      p->dirty     = 0;
    }
  }

  free_file_ctx(fd);

  if (close(fd) != 0 && rc == 0) {
    rc = -1;
    saved_errno = errno;
  }

  if (rc != 0)
    errno = saved_errno;
  return rc;
}

ssize_t vtpc_read(int fd, void* buf, size_t count) {
  FileCtx* ctx = find_file_ctx(fd);
  if (!ctx) {
    errno = EBADF;
    return -1;
  }

  if (count == 0)
    return 0;

  if (ctx->pos >= ctx->size)
    return 0;

  off_t remaining = ctx->size - ctx->pos;
  if ((off_t)count > remaining)
    count = (size_t)remaining;

  size_t total = 0;

  while (total < count) {
    off_t  page_index    = ctx->pos / (off_t)g_page_size;
    size_t offset_in_page = (size_t)(ctx->pos % (off_t)g_page_size);
    size_t chunk         = g_page_size - offset_in_page;
    if (chunk > count - total)
      chunk = count - total;

    CachePage* page = get_page(fd, page_index);
    if (!page) {
      if (total == 0)
        return -1;
      break;
    }

    memcpy((char*)buf + total, page->data + offset_in_page, chunk);

    ctx->pos += (off_t)chunk;
    total    += chunk;
  }

  return (ssize_t)total;
}

ssize_t vtpc_write(int fd, const void* buf, size_t count) {
  FileCtx* ctx = find_file_ctx(fd);
  if (!ctx) {
    errno = EBADF;
    return -1;
  }

  if (count == 0)
    return 0;

  size_t total = 0;

  while (total < count) {
    off_t  page_index    = ctx->pos / (off_t)g_page_size;
    size_t offset_in_page = (size_t)(ctx->pos % (off_t)g_page_size);
    size_t chunk         = g_page_size - offset_in_page;
    if (chunk > count - total)
      chunk = count - total;

    CachePage* page = get_page(fd, page_index);
    if (!page) {
      if (total == 0)
        return -1;
      break;
    }

    memcpy(page->data + offset_in_page,
           (const char*)buf + total,
           chunk);

    page->dirty = 1;
    lru_move_to_front(page);

    ctx->pos += (off_t)chunk;
    total    += chunk;

    if (ctx->pos > ctx->size)
      ctx->size = ctx->pos;
  }

  return (ssize_t)total;
}

off_t vtpc_lseek(int fd, off_t offset, int whence) {
  FileCtx* ctx = find_file_ctx(fd);
  if (!ctx) {
    errno = EBADF;
    return (off_t)-1;
  }

  off_t newpos;

  switch (whence) {
    case SEEK_SET:
      newpos = offset;
      break;
    case SEEK_CUR:
      newpos = ctx->pos + offset;
      break;
    case SEEK_END: {
      struct stat st;
      if (fstat(fd, &st) != 0)
        return (off_t)-1;
      newpos = st.st_size + offset;
      ctx->size = st.st_size;
      break;
    }
    default:
      errno = EINVAL;
      return (off_t)-1;
  }

  if (newpos < 0) {
    errno = EINVAL;
    return (off_t)-1;
  }

  ctx->pos = newpos;
  return newpos;
}

int vtpc_fsync(int fd) {
  int rc = 0;
  int saved_errno = 0;

  for (int i = 0; i < MAX_CACHE_PAGES; ++i) {
    CachePage* p = &g_pages[i];
    if (p->used && p->fd == fd && p->dirty) {
      if (flush_page(p) != 0 && rc == 0) {
        rc = -1;
        saved_errno = errno;
      }
    }
  }

  if (fsync(fd) != 0 && rc == 0) {
    rc = -1;
    saved_errno = errno;
  }

  if (rc != 0)
    errno = saved_errno;
  return rc;
}

void vtpc_get_cache_stats(vtpc_cache_stats_t* stats) {
  if (!stats)
    return;
  
  stats->hits = g_cache_hits;
  stats->misses = g_cache_misses;
  stats->evictions = g_cache_evictions;
  
  unsigned long long total = g_cache_hits + g_cache_misses;
  if (total > 0)
    stats->hit_rate = (double)g_cache_hits / (double)total;
  else
    stats->hit_rate = 0.0;
}

void vtpc_reset_cache_stats(void) {
  g_cache_hits = 0;
  g_cache_misses = 0;
  g_cache_evictions = 0;
}
