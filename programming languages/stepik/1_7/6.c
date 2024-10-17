// position -- адрес указателя на первый элемент E в массиве,
// для которого predicate(E) выполняется.
// Функция возвращает 0 если элемент не найден.
int array_contains(int* array, int* limit, int** position) {
    if (array >= limit) return 0;
    *position = array;
    for (int* current = array; current < limit; current++) {
        if (predicate(*current)) {
            *position = current;
            return 1;
        }
    }
    return 0;
}