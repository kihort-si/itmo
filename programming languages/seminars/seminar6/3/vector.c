#include "vector.h"
#include <stdlib.h>
#include <string.h>
#include <assert.h>
#include <inttypes.h>

struct vector {
    int64_t* data;
    size_t size;
    size_t capacity;
};

vector_t* vector_create(size_t initial_capacity) {
    vector_t* vec = malloc(sizeof(vector_t));
    assert(vec != NULL);

    vec->data = malloc(sizeof(int64_t) * initial_capacity);
    assert(vec->data != NULL);

    vec->size = 0;
    vec->capacity = initial_capacity;
    return vec;
}

void vector_destroy(vector_t* vec) {
    if (vec) {
        free(vec->data);
        free(vec);
    }
}

static void vector_grow(vector_t* vec) {
    vec->capacity = vec->capacity == 0 ? 1 : vec->capacity * 2;
    vec->data = realloc(vec->data, sizeof(int64_t) * vec->capacity);
    assert(vec->data != NULL);
}

void vector_push_back(vector_t* vec, int64_t value) {
    if (vec->size == vec->capacity) {
        vector_grow(vec);
    }
    vec->data[vec->size++] = value;
}

void vector_extend(vector_t* vec, const int64_t* values, size_t count) {
    while (vec->size + count > vec->capacity) {
        vector_grow(vec);
    }
    memcpy(vec->data + vec->size, values, sizeof(int64_t) * count);
    vec->size += count;
}

int64_t vector_get(const vector_t* vec, size_t index) {
    assert(index < vec->size);
    return vec->data[index];
}

void vector_set(vector_t* vec, size_t index, int64_t value) {
    assert(index < vec->size);
    vec->data[index] = value;
}

void vector_resize(vector_t* vec, size_t new_size) {
    if (new_size > vec->capacity) {
        while (vec->capacity < new_size) {
            vector_grow(vec);
        }
    }
    vec->size = new_size;
}

void vector_shrink_to_fit(vector_t* vec) {
    vec->data = realloc(vec->data, sizeof(int64_t) * vec->size);
    assert(vec->data != NULL);
    vec->capacity = vec->size;
}

size_t vector_size(const vector_t* vec) {
    return vec->size;
}

size_t vector_capacity(const vector_t* vec) {
    return vec->capacity;
}

static void vector_print_element(int64_t value, FILE* stream) {
    fprintf(stream, "%" PRId64 " ", value);
}

void vector_print(const vector_t* vec, FILE* stream) {
    for (size_t i = 0; i < vec->size; i++) {
        vector_print_element(vec->data[i], stream);
    }
    fprintf(stream, "\n");
}
