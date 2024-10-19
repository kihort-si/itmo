void list_add_front(struct list** old, int64_t value);

// создать перевернутую копию списка
struct list* list_reverse(const struct list* list) {
    struct list* new_list = NULL;
    int64_t value = 0;
    while (list != NULL) {
        value = list->value;
        list_add_front(&new_list, value);
        list = list->next;
    }
    return new_list;
}