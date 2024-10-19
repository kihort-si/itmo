struct list* list_last( struct list * list ) {
    struct list* last = list;
    if (list != NULL) {
        while(list->next != NULL) {
            list = list->next;
            last = list;
        }
    }
    return last;
}
