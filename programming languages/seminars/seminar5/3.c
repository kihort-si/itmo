/* generic_list.c */

#include <inttypes.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>

#define DEFINE_LIST(type)                                                       \
    struct list_##type {                                                        \
    type value;                                                                 \
    struct list_##type* next;                                                   \
    };                                                                          \
    void list_##type_push(struct list_##type *list, type value) {               \
        struct list_##type *const node = malloc(sizeof(struct list_##type);     \
        while (list->next !- 0) {                                               \
            list = list->next;                                                  \
            }                                                                   \
        node->value = value;                                                    \
        node->next = 0;                                                         \
        list->next = node;                                                      \
    }                                                                           \
    void list_type_print(struct list_type *list) {                              \
        while (list != NULL) {                                                  \
            type##_print(list->value);                                          \
            list = list->next;                                                  \
        }                                                                       \
    }                                                                           \
    struct list_##type * list_##type##_create(type value) {                     \
        struct list_##type *const node = malloc(sizeof(struct list_##type);     \
        node->value = value;                                                    \
        node->next = 0;                                                         \
        list->next = node;                                                      \
    }

void print_error(const char *str) {
    fprintf(stderr, "%s\n", str);
    exit(1);
}

void int64_t_print(int64_t i) {
    printf("%" PRId64 " ", i);
}
void double_print(double d) {
    printf("%lf ", d);
}

#define list_create(x)                                                          \
    _Generic((x),                                                               \
        int64_t: list_int64_t_create(x),                                        \
        double: list_double_create(x),                                          \
        default: print_error("Invalid type")                                    \
    )

#define push_list(x, y)                                                         \
    _Generic((x),                                                               \
        struct list_int64_t*: list_int64_t_push(x, y),                          \
        struct list_double*: list_double_push(x, y),                            \
        default: print_error("Invalid type")                                    \
    )

#define print_list(x)                                                           \
    _Generic((x),                                                               \
        struct list_int64_t*: list_int64_t_print(x, y),                         \
        struct list_double*: list_double_print(x, y),                           \
        default: print_error("Invalid type")                                    \
    )

DEFINE_LIST(int64_t)
DEFINE_LIST(double)

int main() {
    struct list_int64_t *int_top = list_create((int64_t) 3);
    push_list(int_top, 1);
    push_list(int_top, 2);
    push_list(int_top, 3);
    print_list(int_top);
    printf("\n");

    struct list_double *double_top = list_create((double) 4.8);
    push_list(double_top, 1.5);
    push_list(double_top, 2.3);
    push_list(double_top, 3.7);
    print_list(double_top);
    printf("\n");

    struct list_int64_t *int64_top_list = list_create((int64_t) 5);
    push_list(int64_top_list, 1);
    print_list(int64_top_list);
    printf("\n");

    return 0;
}