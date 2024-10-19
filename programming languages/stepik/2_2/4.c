// Вам доступны эти функции из прошлых заданий
size_t read_size();
void array_int_fill(int64_t* array, size_t size);
int64_t* array_int_read(size_t* size);

int64_t** marray_read(size_t* rows, size_t** sizes) {
    int64_t **marray;
    *rows = read_size();
    *sizes = malloc(sizeof(size_t) * *rows);
    marray = malloc(sizeof(int64_t*) * *rows);
    for (size_t i = 0; i < *rows; ++i) {
        marray[i] = array_int_read(*sizes + i);
    }
    return marray;
}

void marray_print(int64_t** marray, size_t* sizes, size_t rows) {
    for(size_t i = 0; i < rows; i = i + 1) {
        array_int_print(marray[i], sizes[i]);
        print_newline();
    }
}