#ifndef VECTOR_H
#define VECTOR_H

#include <stdio.h>
#include <stdint.h>

typedef struct vector vector_t;

vector_t* vector_create(size_t initial_capacity);
void vector_destroy(vector_t* vec);

void vector_push_back(vector_t* vec, int64_t value);
void vector_extend(vector_t* vec, const int64_t* values, size_t count);

int64_t vector_get(const vector_t* vec, size_t index);
void vector_set(vector_t* vec, size_t index, int64_t value);

void vector_resize(vector_t* vec, size_t new_size);
void vector_shrink_to_fit(vector_t* vec);

size_t vector_size(const vector_t* vec);
size_t vector_capacity(const vector_t* vec);

void vector_print(const vector_t* vec, FILE* stream);

#endif // VECTOR_H
