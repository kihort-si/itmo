int is_prime(int n) {
    if (n < 2) {
        return 0;
    } else {
        for (int i = 2; i < n; i++) {
            if (n % i == 0) {
                return 0;
            }
        }
        return 1;
    }
}