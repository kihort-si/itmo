int is_square(int x) {
    if (x < 0) { return 0; }
    int number = 0;
    while (number * number <= x) {
        if (number * number == x) { return 1; }
        number++;
    }

    return 0;
}