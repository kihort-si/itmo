struct maybe_int64 maybe_read_int64() {
    struct maybe_int64 result;
    if (scanf("%" SCNd64, &result.value) > 0) {
        result.valid = true;
        return result;
    } else {
        result.valid = false;
        return result;
    }
}