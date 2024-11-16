/* bad.c */

#include "vector.h"
#include <stdio.h>

int main() {
    vector_t* vec = vector_create(5);

    for (size_t i = 0; i <= 100; i++) {
        vector_push_back(vec, i * i);
    }

    vector_print(vec, stdout);
    vector_destroy(vec);

    return 0;
}
