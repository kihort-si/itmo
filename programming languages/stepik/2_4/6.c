void list_add_back(struct list** old, int64_t value) {
    struct list* new = node_create(value);
    new->next = NULL;
    if (*old != NULL) {
        struct list* last = list_last(*old);
        last->next = new;
    } else {
        *old = new;
    }
}