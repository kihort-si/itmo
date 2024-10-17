void fizzbuzz(int arg) {
    if (arg > 0) {
        if (arg % 5 == 0 && arg % 3 == 0) {
            printf("fizzbuzz");
        }
        else if (arg % 5 == 0) {
            printf("buzz");
        }
        else if (arg % 3 == 0) {
            printf("fizz");
        }
    }
    else {
        printf("no");
    }
}
