struct list* list_read() {
    struct list* result = NULL;
    struct list* last = NULL;
    struct maybe_int64 input = maybe_read_int64();
    while (input.valid) {
        struct list* new_node = node_create(input.value);
        if (result == NULL) {
            result = new_node;
        } else {
            last->next = new_node;
        }
        last = new_node;
        input = maybe_read_int64();
    }
    return result;
}

