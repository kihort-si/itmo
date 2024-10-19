/* Вы можете пользоваться следующими определениями:

struct maybe_int64 {int64_t value; bool valid;};
struct maybe_int64 some_int64(int64_t i);
const struct maybe_int64 none_int64 = { 0, false };
void maybe_int64_print( struct maybe_int64 i );


struct array_int {int64_t* data; size_t size;};
struct array_int array_int_create( size_t sz );

struct array_int array_int_create( size_t sz ) {
    return (struct array_int) { .data = malloc( sizeof( int64_t ) * sz ), .size = sz };
}

int64_t* array_int_last( struct array_int a );
struct maybe_int64 array_int_get( struct array_int a, size_t i );
bool array_int_set( struct array_int a, size_t i, int64_t value );
void array_int_print( struct array_int array );
void array_int_free( struct array_int* a );

*/

struct stack {
    size_t count;
    struct array_int data;
};

// Количество элементов в стеке
size_t stack_count(const struct stack* s) {
    return s->count;
}

// Создаем и деинициализируем стек
struct stack stack_create(size_t size) {
    struct stack s;
    s.data = array_int_create(size);
    s.count = 0;
    return s;
}

void stack_destroy(struct stack* s) {
    array_int_free(&s->data);
    s->count = 0;
}

// Стек полный
bool stack_is_full(const struct stack* s) {
    return s->count == s->data.size;
}

// Стек пустой
bool stack_is_empty(const struct stack* s) {
    return s->count == 0;
}

// Поместить значение в стек
bool stack_push( struct stack* s, int64_t value ) {
    if (stack_is_full(s)) {
        return false;
    } else {
        s->data.data[s->count] = value;
        s->count++;
        return true;
    }
}

// Вынуть значение с вершины стека. Может вернуть none_int64
struct maybe_int64 stack_pop(struct stack* s) {
    if (stack_is_empty(s)) {
        return none_int64;
    } else {
        s->count--;
        return some_int64(s->data.data[s->count]);
    }
}

void stack_print( const struct stack* s ) {
    for (size_t i = 0; i < stack_count( s ); i = i + 1 ) {
        print_int64( array_int_get( s->data, i).value );
        printf(" ");
    }
}