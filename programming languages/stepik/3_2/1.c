#ifndef STACK_H
#define STACK_H
#include <stdbool.h>
#include <stdint.h>
#include <stddef.h>

typedef struct {
    int64_t value;
} item;

#define STACK_ITEM_PRI PRId64

struct maybe_item {
    bool valid;
    item value;
};

static const struct maybe_item none_int = {0, {0}};

static struct maybe_item some_int(int64_t value) {
    return (struct maybe_item){true, {value}};
}

struct struct_int;

struct stack_int *stack_int_create();

void stack_int_destroy(struct stack_int *s);

bool stack_int_empty(struct stack_int const *s);
bool stack_int_full(struct stack_int const *s);

bool stack_int_push(struct stack_int *s, item item);

struct maybe_item stack_int_pop(struct stack_int *s);

void stack_int_print(struct stack_int const*);
#endif