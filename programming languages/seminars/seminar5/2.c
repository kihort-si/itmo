/* generic_list.c */

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


DEFINE_LIST(int64_t)
DEFINE_LIST(double)

int main() {
    struct list_int64_t *int_top = list_int64_t_create(3);
    push(int64_t, int_top, 1);
    push(int64_t, int_top, 2);
    push(int64_t, int_top, 3);
    print_list(int64_t, int_top);
    printf("\n");

    struct list_double *double_top = list_double_create(4.8);
    push(double_top, double_top, 1);
    push(double_top, double_top, 2);
    push(double_top, double_top, 3);
    print_list(double, double_top);
    printf("\n");

    struct list_int64_t *int64_top_list = list_int64_t_create(5);
    push(int64_top_list, int64_top_list, 1);
    print_list(int64_top_list, int64_top_list);
    printf("\n");

    return 0;
}