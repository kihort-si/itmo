struct heap_string {
    char* addr;
};

// скопировать в кучу
struct heap_string halloc( const char* s ) {
    struct heap_string h;
    h.addr = malloc(strlen(s) + 1);
    if (h.addr != NULL) {
        strcpy(h.addr, s);
    }

    return h;
}

// освободить память
void heap_string_free( struct heap_string h ) {
    if (h.addr != NULL) {
        free(h.addr);
        h.addr = NULL;
    }
}