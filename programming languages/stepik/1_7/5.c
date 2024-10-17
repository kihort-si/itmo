void array_fib(int* array, int* limit) {
    if (limit - array == 0) {
        return;
    } else if (limit - array == 1) {
        array[0] = 1;
    } else if (limit - array == 2) {
        array[0] = 1;
        array[1] = 1;
    } else {
        array[0] = 1;
        array[1] = 1;
        for (int* i = array + 2; i < limit; i++) {
            *i = *(i - 1) + *(i - 2);
        }
    }
}