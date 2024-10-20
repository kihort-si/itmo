/* Вам доступны:


struct maybe_int64 {
    int64_t value;
    bool valid;
};

struct maybe_int64 some_int64(int64_t i);

extern const struct maybe_int64 none_int64;

void maybe_int64_print( struct maybe_int64 i );
struct maybe_int64 maybe_read_int64();
void print_int64(int64_t i)
*/

void interpret_push(struct vm_state* state) {
    stack_push(& state->data_stack, state->ip->as_arg64.arg);
    state->ip++;
}

void interpret_iread(struct vm_state* state ) {
    struct maybe_int64 input = maybe_read_int64();
    if (input.valid) {
        stack_push(&state->data_stack, input.value);
    } else {
        printf("Ошибка ввода.\n");
    }
    state->ip++;
}
void interpret_iadd(struct vm_state* state ) {
    struct maybe_int64 a = stack_pop(&state->data_stack);
    struct maybe_int64 b = stack_pop(&state->data_stack);
    if (a.valid && b.valid) {
        stack_push(&state->data_stack, a.value + b.value);
    } else {
        printf("Ошибка: Недостаточно данных в стеке для сложения.\n");
    }
    state->ip++;
}
void interpret_iprint(struct vm_state* state ) {
    struct maybe_int64 value = stack_pop(&state->data_stack);
    if (value.valid) {
        print_int64(value.value);
    } else {
        printf("Ошибка: Стек пуст.\n");
    }
    state->ip++;
}

/* Подсказка: можно выполнять программу пока ip != NULL,
    тогда чтобы её остановить достаточно обнулить ip */
void interpret_stop(struct vm_state* state ) {
    state->ip = NULL;
}


void (*interpreters[])(struct vm_state*) = {
    [BC_PUSH] = interpret_push,
    [BC_IPRINT] = interpret_iprint,
    [BC_IREAD] = interpret_iread,
    [BC_IADD] = interpret_iadd,
    [BC_STOP] = interpret_stop
};

void interpret(struct vm_state* state) {
    while (state->ip != NULL) {
        interpreters[state->ip->opcode](state);
    }
}