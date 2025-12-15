//
// Created by Никита Васильев on 30.11.2025.
//

#include <stdio.h>
#include <stdlib.h>
#include <time.h>

int main(int argc, char **argv) {
  if (argc < 3) {
    fprintf(stderr, "usage: %s output_file count\n", argv[0]);
    return 1;
  }
  const char *output = argv[1];
  long n = atol(argv[2]);

  FILE *f = fopen(output, "wb");
  if (!f) {
    perror("fopen");
    return 1;
  }
  srand(42);
  for (long i = 0; i < n; i++) {
    int x = rand();
    fwrite(&x, sizeof(int), 1, f);
  }
  fclose(f);
  return 0;
}
