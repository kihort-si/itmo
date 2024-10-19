/*
struct list {
    int64_t value;
    struct list* next;
};
*/
/* Вы можете пользоваться следующими функциями */
void print_int64(int64_t i);
struct list* node_create( int64_t value );

int64_t identity(int64_t x) {
    return x;
}

int64_t abs_int64(int64_t x) {
    return (x < 0) ? -x : x;
}

/*  Создать новый список, в котором каждый элемент получен из соответствующего
    элемента списка l путём применения функции f */
struct list* list_map( const struct list*  l, int64_t (f) (int64_t))  {
    if (l == NULL) {
        return NULL;
    }

    struct list* new_list = node_create(f(l->value));
    struct list* current_new = new_list;
    const struct list* current_old = l->next;

    while (current_old != NULL) {
        current_new->next = node_create(f(current_old->value));
        current_new = current_new->next;
        current_old = current_old->next;
    }

    return new_list;
}

struct list* list_copy( const struct list * l ) {
    return list_map(l, identity);
}

struct list* list_abs( const struct list* l ) {
    return list_map(l, abs_int64);
}