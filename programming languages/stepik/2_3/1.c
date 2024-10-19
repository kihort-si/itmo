// упакованное число 42 выводится как "Some 42"
// Ошибка выводится как None
void maybe_int64_print( struct maybe_int64 i ) {
    if (i.valid == false) {
        printf("None");
    } else {
        printf("Some %" PRId64, i.value);
    }
}

// Если обе упаковки содержат ошибку, то результат ошибка
// Если ровно одна упаковка содержит ошибку, то результат -- вторая
// Если обе упаковки содержат число, то результат это минимум из двух чисел.
struct maybe_int64 maybe_int64_min(struct maybe_int64 a, struct maybe_int64 b) {
    if (a.valid == false && b.valid == false) {
        return a;
    } if (a.valid == true && b.valid == false) {
        return a;
    } if (a.valid == false && b.valid == true) {
        return b;
    } else {
        if (a.value < b.value) {
            return a;
        } else {
            return b;
        }
    }
}