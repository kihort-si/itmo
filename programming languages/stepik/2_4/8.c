struct maybe_int64 list_at(const struct list* list, size_t idx) {
    if (idx >= list_length(list)) {
        return none_int64;
    }

    size_t index = 0;
    const struct list* current = list;

    while (index < idx) {
        current = current->next;
        index++;
    }

    return some_int64(current->value);
}
