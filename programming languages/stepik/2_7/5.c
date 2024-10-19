/*
struct list {
    int64_t value;
    struct list* next;
};
*/
void print_int64(int64_t i);

struct list* node_create( int64_t value );
void list_destroy( struct list* list );


/*  Сгенерировать список длины sz с помощью значения init и функции f
 Результат: init, f(init), f(f(init)), ... */
struct list* list_iterate( int64_t init, size_t sz, int64_t(f)(int64_t)) {
    if (sz == 0) return NULL;

    struct list* head = node_create(init);
    struct list* current = head;

    for (size_t i = 1; i < sz; i++) {
        int64_t next_value = f(current->value);
        current->next = node_create(next_value);
        current = current->next;
    }

    return head;
}