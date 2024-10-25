#define print_var(x) printf(#x " is %d", x )

int main() {
    int x = 10;
    const int y = 20;
    print_var(x);
    print_var(y);
    print_var(100);
    return 0;
}