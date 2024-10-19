/*
struct list {
    int64_t value;
    struct list* next;
};
*/

/* Изменить каждый элемент списка с помощью функции f  */
void list_map_mut(struct list* l, int64_t (f) (int64_t))  {
    struct list* current = l;
    while (current != NULL) {
        current->value = f(current->value);
        current = current->next;
    }
}


static int64_t triple( int64_t x ) { return x * 3; }

/* Используя list_map_mut умножьте все элементы списка на 3 */
void list_triple(struct list*  l) {
    list_map_mut(l, triple);
}