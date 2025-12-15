//
// Created by Nikita on 05.10.2025.
//

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/time.h>
#include <unistd.h>
#include <pthread.h>
#include <fcntl.h>

#include "vtpc.h"

#define CHUNK_SIZE 1000000

int compare_int(const void* a, const void* b) {
  return (*(int*)a - *(int*)b);
}

double getCurrentTime(void) {
  struct timeval tv;
  gettimeofday(&tv, NULL);
  return tv.tv_sec + tv.tv_usec / 1000000.0;
}

void merge_files(const char* file1, const char* file2, const char* output) {
  int f1  = vtpc_open(file1, O_RDONLY, 0);
  int f2  = vtpc_open(file2, O_RDONLY, 0);
  int out = vtpc_open(output, O_RDWR | O_CREAT | O_TRUNC, 0644);

  if (f1 < 0 || f2 < 0 || out < 0) {
    perror("merge_files vtpc_open");
    if (f1 >= 0) vtpc_close(f1);
    if (f2 >= 0) vtpc_close(f2);
    if (out >= 0) vtpc_close(out);
    return;
  }

  int v1, v2;
  ssize_t r1 = vtpc_read(f1, &v1, sizeof(int));
  ssize_t r2 = vtpc_read(f2, &v2, sizeof(int));
  int has1 = (r1 == sizeof(int));
  int has2 = (r2 == sizeof(int));

  while (has1 && has2) {
    if (v1 <= v2) {
      if (vtpc_write(out, &v1, sizeof(int)) != sizeof(int)) {
        perror("merge_files vtpc_write");
        break;
      }
      r1 = vtpc_read(f1, &v1, sizeof(int));
      has1 = (r1 == sizeof(int));
    } else {
      if (vtpc_write(out, &v2, sizeof(int)) != sizeof(int)) {
        perror("merge_files vtpc_write");
        break;
      }
      r2 = vtpc_read(f2, &v2, sizeof(int));
      has2 = (r2 == sizeof(int));
    }
  }

  while (has1) {
    if (vtpc_write(out, &v1, sizeof(int)) != sizeof(int)) {
      perror("merge_files vtpc_write tail1");
      break;
    }
    r1 = vtpc_read(f1, &v1, sizeof(int));
    has1 = (r1 == sizeof(int));
  }

  while (has2) {
    if (vtpc_write(out, &v2, sizeof(int)) != sizeof(int)) {
      perror("merge_files vtpc_write tail2");
      break;
    }
    r2 = vtpc_read(f2, &v2, sizeof(int));
    has2 = (r2 == sizeof(int));
  }

  vtpc_fsync(out);
  vtpc_close(f1);
  vtpc_close(f2);
  vtpc_close(out);
}

void external_sort(const char* input_file, const char* output_file) {
  int input = vtpc_open(input_file, O_RDONLY, 0);
  if (input < 0) {
    perror("Cannot open input file");
    return;
  }

  int* buffer = malloc(CHUNK_SIZE * sizeof(int));
  if (!buffer) {
    perror("malloc");
    vtpc_close(input);
    return;
  }

  int  chunk_id   = 0;
  char temp_files[100][256];
  int  num_chunks = 0;

  pid_t         pid = getpid();
  unsigned long tid = (unsigned long)pthread_self();

  while (1) {
    ssize_t bytes_read = vtpc_read(input, buffer, CHUNK_SIZE * sizeof(int));
    if (bytes_read < 0) {
      perror("vtpc_read");
      free(buffer);
      vtpc_close(input);
      return;
    }
    if (bytes_read == 0) {
      break;
    }

    size_t read_elems = (size_t)bytes_read / sizeof(int);
    if (read_elems == 0) {
      break;
    }

    qsort(buffer, read_elems, sizeof(int), compare_int);

    sprintf(
        temp_files[num_chunks],
        "temp_%d_%lu_chunk_%d.dat",
        (int)pid,
        tid,
        chunk_id++
    );

    int temp = vtpc_open(temp_files[num_chunks],
                     O_RDWR | O_CREAT | O_TRUNC,
                     0644);

    if (temp < 0) {
      perror("temp vtpc_open");
      free(buffer);
      vtpc_close(input);
      return;
    }

    size_t to_write   = read_elems * sizeof(int);
    size_t written    = 0;
    while (written < to_write) {
      ssize_t w = vtpc_write(temp,
                             (char*)buffer + written,
                             to_write - written);
      if (w < 0) {
        perror("temp vtpc_write");
        vtpc_close(temp);
        free(buffer);
        vtpc_close(input);
        return;
      }
      if (w == 0) {
        fprintf(stderr, "temp vtpc_write: wrote 0 bytes\n");
        break;
      }
      written += (size_t)w;
    }

    vtpc_fsync(temp);
    vtpc_close(temp);
    num_chunks++;
  }

  vtpc_close(input);

  if (num_chunks == 0) {
    int out = vtpc_open(output_file,
                        O_WRONLY | O_CREAT | O_TRUNC,
                        0644);
    if (out >= 0) {
      vtpc_close(out);
    }
    free(buffer);
    return;
  }

  while (num_chunks > 1) {
    char merged[256];
    sprintf(
        merged,
        "temp_%d_%lu_merged_%d.dat",
        (int)pid,
        tid,
        chunk_id++
    );

    merge_files(temp_files[0], temp_files[1], merged);

    remove(temp_files[0]);
    remove(temp_files[1]);

    strcpy(temp_files[0], merged);
    for (int i = 2; i < num_chunks; i++) {
      strcpy(temp_files[i - 1], temp_files[i]);
    }
    num_chunks--;
  }

  rename(temp_files[0], output_file);
  free(buffer);
}

typedef struct {
  const char* input_file;
  char        output_file[256];
  int         iterations;
  int         thread_index;
  double      total_time;
} ThreadArgs;

void* thread_worker(void* arg) {
  ThreadArgs* t = (ThreadArgs*)arg;

  for (int i = 0; i < t->iterations; i++) {
    printf(
        "Thread %d: iteration %d/%d...\n",
        t->thread_index,
        i + 1,
        t->iterations
    );

    double start = getCurrentTime();
    external_sort(t->input_file, t->output_file);
    double end = getCurrentTime();

    double it_time = end - start;
    t->total_time += it_time;

    printf(
        "\033[33mThread %d: iteration %d completed in %.3f seconds\033[0m\n",
        t->thread_index,
        i + 1,
        it_time
    );
  }

  return NULL;
}

int main(int argc, char* argv[]) {
  if (argc < 3) {
    fprintf(
        stderr,
        "Usage:\n"
        "  %s <input_file> <output_file> [iterations] [threads]\n\n"
        "Examples:\n"
        "  %s input.dat output.dat           # single-thread, 1 iteration\n"
        "  %s input.dat output.dat 30        # single-thread, 30 iterations\n"
        "  %s input.dat output.dat 30 3      # 3 threads, each 30 iterations\n",
        argv[0], argv[0], argv[0], argv[0]
    );
    return 1;
  }

  const char* input_file  = argv[1];
  const char* output_file = argv[2];

  int iterations = 1;
  if (argc >= 4) {
    iterations = atoi(argv[3]);
    if (iterations < 1)
      iterations = 1;
  }

  int threads = 1;
  if (argc >= 5) {
    threads = atoi(argv[4]);
    if (threads < 1)
      threads = 1;
  }

  if (threads == 1) {
    vtpc_reset_cache_stats();  // Reset stats before test
    double total_time = 0.0;

    for (int i = 0; i < iterations; i++) {
      printf("Iteration %d/%d...\n", i + 1, iterations);

      double start = getCurrentTime();
      external_sort(input_file, output_file);
      double end = getCurrentTime();

      double iteration_time = end - start;
      total_time += iteration_time;

      printf(
          "\033[33mIteration %d completed in %.3f seconds\033[0m\n",
          i + 1,
          iteration_time
      );
    }

    vtpc_cache_stats_t stats;
    vtpc_get_cache_stats(&stats);
    
    printf(
        "\033[32mAll %d iterations completed in %.3f seconds (avg: %.3f sec)\033[0m\n",
        iterations,
        total_time,
        total_time / iterations
    );
    printf("\033[36mCache Statistics:\033[0m\n");
    printf("  Hits: %llu\n", stats.hits);
    printf("  Misses: %llu\n", stats.misses);
    printf("  Evictions: %llu\n", stats.evictions);
    printf("  Hit Rate: %.2f%%\n", stats.hit_rate * 100.0);
    return 0;
  }

  vtpc_reset_cache_stats();  // Reset stats before test
  
  printf(
      "Running in multi-threaded mode: %d threads, %d iterations per thread\n",
      threads,
      iterations
  );

  pthread_t*  tids  = malloc(sizeof(pthread_t) * threads);
  ThreadArgs* args  = malloc(sizeof(ThreadArgs) * threads);
  if (!tids || !args) {
    perror("malloc for threads");
    free(tids);
    free(args);
    return 1;
  }

  for (int i = 0; i < threads; i++) {
    args[i].input_file  = input_file;
    snprintf(args[i].output_file, sizeof(args[i].output_file),
             "%s.thread%d", output_file, i);
    args[i].iterations   = iterations;
    args[i].thread_index = i;
    args[i].total_time   = 0.0;

    int rc = pthread_create(&tids[i], NULL, thread_worker, &args[i]);
    if (rc != 0) {
      fprintf(stderr, "pthread_create failed for thread %d: %d\n", i, rc);
      threads = i;
      break;
    }
  }

  double global_sum = 0.0;

  for (int i = 0; i < threads; i++) {
    pthread_join(tids[i], NULL);
    global_sum += args[i].total_time;
  }

  printf("\n\033[32mThreads summary:\033[0m\n");
  for (int i = 0; i < threads; i++) {
    double avg = args[i].total_time / args[i].iterations;
    printf(
        "  Thread %d -> file %s : total %.3f sec, avg %.3f sec/iter\n",
        i,
        args[i].output_file,
        args[i].total_time,
        avg
    );
  }
  
  vtpc_cache_stats_t stats;
  vtpc_get_cache_stats(&stats);
  printf("\n\033[36mCache Statistics:\033[0m\n");
  printf("  Hits: %llu\n", stats.hits);
  printf("  Misses: %llu\n", stats.misses);
  printf("  Evictions: %llu\n", stats.evictions);
  printf("  Hit Rate: %.2f%%\n", stats.hit_rate * 100.0);

  free(tids);
  free(args);

  return 0;
}