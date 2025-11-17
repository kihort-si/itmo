//
// Created by Nikita on 05.10.2025.
//

#include <math.h>
#include <pthread.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/time.h>

double getCurrentTime(void) {
  struct timeval tv;
  gettimeofday(&tv, NULL);
  return tv.tv_sec + tv.tv_usec / 1000000.0;
}

void factorize_once(unsigned long long n, int iter_index, int thread_index) {
  unsigned long long num = n;
  unsigned long long factors[128];
  int cnt = 0;

  while (num % 2 == 0) {
    if (cnt < 128) factors[cnt++] = 2;
    num /= 2;
  }

  for (unsigned long long i = 3; i * i <= num; i += 2) {
    while (num % i == 0) {
      if (cnt < 128) factors[cnt++] = i;
      num /= i;
    }
  }

  if (num > 1) {
    if (cnt < 128) factors[cnt++] = num;
  }

  printf("[thread %d] iteration %d: %llu", thread_index, iter_index + 1, n);
  for (int i = 0; i < cnt; i++) {
    printf(" %llu", factors[i]);
  }
  printf("\n");
}

typedef struct {
  unsigned long long number;
  int                iterations;
  int                thread_index;
} ThreadArgs;

void* thread_worker(void* arg) {
  ThreadArgs* t = (ThreadArgs*)arg;

  for (int i = 0; i < t->iterations; i++) {
    factorize_once(t->number, i, t->thread_index);
  }

  return NULL;
}

int main(int argc, char* argv[]) {
  if (argc < 3) {
    fprintf(
        stderr,
        "Usage:\n"
        "  %s <number> <iterations> [threads]\n\n"
        "Examples:\n"
        "  %s 9999999962000 30        # single-thread, 30 iterations\n"
        "  %s 9999999962000 30 3      # 3 threads, each 30 iterations\n",
        argv[0], argv[0], argv[0]
    );
    return 1;
  }

  unsigned long long number = strtoull(argv[1], NULL, 10);
  int iterations = atoi(argv[2]);
  if (iterations < 1)
    iterations = 1;

  int threads = 1;
  if (argc >= 4) {
    threads = atoi(argv[3]);
    if (threads < 1)
      threads = 1;
  }

  long long total_iters = (long long)threads * (long long)iterations;

  double start = getCurrentTime();

  if (threads == 1) {
    for (int i = 0; i < iterations; i++) {
      factorize_once(number, i, 0);
    }
  } else {
    printf(
        "Running in multi-threaded mode: %d threads, %d iterations per thread\n",
        threads,
        iterations
    );

    pthread_t*  tids = malloc(sizeof(pthread_t) * threads);
    ThreadArgs* args = malloc(sizeof(ThreadArgs) * threads);
    if (!tids || !args) {
      perror("malloc");
      free(tids);
      free(args);
      return 1;
    }

    for (int i = 0; i < threads; i++) {
      args[i].number       = number;
      args[i].iterations   = iterations;
      args[i].thread_index = i;

      int rc = pthread_create(&tids[i], NULL, thread_worker, &args[i]);
      if (rc != 0) {
        fprintf(stderr, "pthread_create failed for thread %d: %d\n", i, rc);
        threads = i;
        break;
      }
    }

    for (int i = 0; i < threads; i++) {
      pthread_join(tids[i], NULL);
    }

    free(tids);
    free(args);
  }

  double end = getCurrentTime();
  double total_time = end - start;
  double avg = total_time / (double)total_iters;

  printf(
      "\033[33mTotal iterations: %lld, time: %.3f sec, "
      "avg per iteration: %.6f sec\033[0m\n",
      total_iters,
      total_time,
      avg
  );

  return 0;
}
