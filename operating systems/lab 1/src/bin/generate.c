//
// Created by Nikita on 05.10.2025.
//

#include <stdio.h>
#include <stdlib.h>
#include <time.h>

int main() {
  int num_count = 10000000;

  int* numbers = malloc(num_count * sizeof(int));
  if (!numbers) {
    perror("Failed to allocate memory");
    return 1;
  }

  srand(time(NULL));

  for (int i = 0; i < num_count; i++) {
    numbers[i] = rand();
  }

  FILE* file = fopen("input.dat", "wb");
  if (!file) {
    perror("Failed to open file");
    free(numbers);
    return 1;
  }

  fwrite(numbers, sizeof(int), num_count, file);

  fclose(file);

  free(numbers);

  printf("Random data written to 'input_file.dat'\n");
  return 0;
}
