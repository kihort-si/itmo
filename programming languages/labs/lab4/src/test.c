#include <stdio.h>

#include "mem.h"
#include "mem_internals.h"
#include "test.h"


// Обычное успешное выделение памяти.
void test_malloc_success() {
    printf("Test: Successful memory allocation\n");

    heap_init(4096);

    void* ptr1 = _malloc(32);
    void* ptr2 = _malloc(64);

    if (ptr1 && ptr2) {
        printf("Memory successfully allocated.\n");
    }

    _free(ptr1);
    _free(ptr2);
}

// Освобождение одного блока из нескольких выделенных.
void test_free_one_block() {
    printf("Test: Free one block from multiple allocations\n");

    void* ptr1 = _malloc(32);
    void* ptr2 = _malloc(64);
    void* ptr3 = _malloc(128);

    if (ptr1 && ptr2 && ptr3) {
        printf("Memory allocated successfully.\n");

        _free(ptr2);
        _free(ptr1);
        _free(ptr3);
    } else {
        printf("Memory allocation failed.\n");
    }
}

// Освобождение двух блоков из нескольких выделенных.
void test_free_two_blocks() {
    printf("Test: Free two blocks from multiple allocations\n");

    void* ptr1 = _malloc(32);
    void* ptr2 = _malloc(64);
    void* ptr3 = _malloc(128);

    if (ptr1 && ptr2 && ptr3) {
        printf("Memory allocated successfully.\n");

        _free(ptr1);
        _free(ptr2);
        _free(ptr3);
    } else {
        printf("Memory allocation failed.\n");
    }
}

// Память закончилась, новый регион памяти расширяет старый.
void test_heap_growth() {
    printf("Test: Heap growth when memory is exhausted\n");

    size_t initial_size = REGION_MIN_SIZE;
    heap_init(initial_size);

    void *ptr1 = _malloc(4096);
    void *ptr2 = _malloc(4096);
    void *ptr3 = _malloc(4096);

    _free(ptr1);
    _free(ptr2);
    _free(ptr3);

    printf("All allocated memory freed successfully.\n");

    heap_term();
    printf("Heap terminated successfully.\n");
}

// Память закончилась, старый регион памяти не расширить из-за другого выделенного диапазона адресов, новый регион выделяется в другом месте.
void test_heap_split() {
    printf("Test: Heap split when memory is exhausted\n");

    size_t initial_size = 1024 * 64;  // 64 KB
    void* heap = heap_init(initial_size);

    if (heap != NULL) {
        printf("Heap initialized.\n");

        void* ptr1 = _malloc(32);
        void* ptr2 = _malloc(32);
        void* ptr3 = _malloc(32);
        void* ptr4 = _malloc(32);

        if (ptr1 && ptr2 && ptr3 && ptr4) {
            printf("Memory allocated successfully.\n");

            _free(ptr1);
            _free(ptr2);
            _free(ptr3);

            void* ptr5 = _malloc(512);

            if (ptr5 != NULL) {
                printf("Memory allocated successfully after heap split.\n");
            } else {
                printf("Memory allocation failed after heap split.\n");
            }
        } else {
            printf("Memory allocation failed.\n");
        }
    } else {
        printf("Heap initialization failed.\n");
    }

    heap_term();
}

void run_tests() {
    test_malloc_success();
    test_free_one_block();
    test_free_two_blocks();
    test_heap_growth();
    test_heap_split();
}