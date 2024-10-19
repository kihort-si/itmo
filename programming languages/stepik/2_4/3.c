size_t list_length(const struct list* list) {
    size_t cnt = 0;
    while(list != NULL) {
        cnt++;
        list = list->next;
    }
    return cnt;
}