// используйте typedef чтобы определить ftype
typedef size_t (*func_ptr)(struct array, int64_t);

typedef char ftype(const float*, func_ptr);
