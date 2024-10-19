void list_destroy( struct list* list ) {
    while (list) {
        struct list* next = list -> next;
        free(list);
        list = next;
    }
}