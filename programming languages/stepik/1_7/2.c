void normalize(int* a) {
    if (*a <= 0) {
        return;
    } else {
        while (*a % 2 == 0) {
            *a = *a / 2;
        }
    }
}