//
// Created by Nikita on 05.10.2025.
//

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/time.h>
#include <unistd.h>
#include <pthread.h>

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
  FILE* f1 = fopen(file1, "rb");
  FILE* f2 = fopen(file2, "rb");
  FILE* out = fopen(output, "wb");

  if (!f1 || !f2 || !out) {
    perror("merge_files fopen");
    if (f1) fclose(f1);
    if (f2) fclose(f2);
    if (out) fclose(out);
    return;
  }

  int v1, v2;
  int has1 = fread(&v1, sizeof(int), 1, f1);
  int has2 = fread(&v2, sizeof(int), 1, f2);

  while (has1 && has2) {
    if (v1 <= v2) {
      fwrite(&v1, sizeof(int), 1, out);
      has1 = fread(&v1, sizeof(int), 1, f1);
    } else {
      fwrite(&v2, sizeof(int), 1, out);
      has2 = fread(&v2, sizeof(int), 1, f2);
    }
  }

  while (has1) {
    fwrite(&v1, sizeof(int), 1, out);
    has1 = fread(&v1, sizeof(int), 1, f1);
  }

  while (has2) {
    fwrite(&v2, sizeof(int), 1, out);
    has2 = fread(&v2, sizeof(int), 1, f2);
  }

  fclose(f1);
  fclose(f2);
  fclose(out);
}

void external_sort(const char* input_file, const char* output_file) {
  FILE* input = fopen(input_file, "rb");
  if (!input) {
    perror("Cannot open input file");
    return;
  }

  int* buffer = malloc(CHUNK_SIZE * sizeof(int));
  if (!buffer) {
    perror("malloc");
    fclose(input);
    return;
  }

  int chunk_id = 0;
  char temp_files[100][256];
  int num_chunks = 0;

  pid_t pid = getpid();
  unsigned long tid = (unsigned long)pthread_self();

  size_t read;
  while ((read = fread(buffer, sizeof(int), CHUNK_SIZE, input)) > 0) {
    qsort(buffer, read, sizeof(int), compare_int);

    sprintf(
        temp_files[num_chunks],
        "temp_%d_%lu_chunk_%d.dat",
        (int)pid,
        tid,
        chunk_id++
    );

    FILE* temp = fopen(temp_files[num_chunks], "wb");
    if (!temp) {
      perror("temp fopen");
      free(buffer);
      fclose(input);
      return;
    }
    fwrite(buffer, sizeof(int), read, temp);
    fclose(temp);
    num_chunks++;
  }
  fclose(input);

  if (num_chunks == 0) {
    FILE* out = fopen(output_file, "wb");
    if (out) fclose(out);
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

    printf(
        "\033[32mAll %d iterations completed in %.3f seconds (avg: %.3f sec)\033[0m\n",
        iterations,
        total_time,
        total_time / iterations
    );
    return 0;
  }

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

  free(tids);
  free(args);

  return 0;
}
