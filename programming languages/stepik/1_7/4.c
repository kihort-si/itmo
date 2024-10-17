void array_reverse(int* array, int size) {
    for (int i = 0; i < size / 2; i++) {
        int temp = array[i];
        array[i] = array[size - 1 - i];
        array[size - 1 - i] = temp;
    }
}

void array_reverse_ptr(int* array, int* limit) {
    limit--;
    for (int* i = array; i < limit; i++, limit--) {
        int temp = *i;
        *i = *limit;
        *limit = temp;
    }
}