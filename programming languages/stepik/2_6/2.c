// Мы хотим, чтобы в структуре user хранились ссылки только на строчки из кучи.
typedef struct { char* addr; } string_heap ;

/*  Тип для идентификаторов пользователей
    и его спецификаторы ввода и вывода для printf */
typedef uint64_t uid;
#define PRI_uid PRIu64
#define SCN_uid SCNu64

enum city {C_SARATOV, C_MOSCOW, C_PARIS, C_LOS_ANGELES, C_OTHER};

/*  Массив, где элементам перечисления сопоставляются их текстовые представления */
const char* city_string[] = {
    [C_SARATOV] = "Saratov",
    [C_MOSCOW] = "Moscow",
    [C_PARIS] = "Paris",
    [C_LOS_ANGELES] = "Los Angeles",
    [C_OTHER] = "Other"
  };


struct user {
    const uid id;
    const string_heap name;
    enum city city;
};

int compare_uid(const void* a, const void* b) {
    uid uid_a = ((struct user*)a)->id;
    uid uid_b = ((struct user*)b)->id;
    if (uid_a < uid_b) return -1;
    if (uid_a > uid_b) return 1;
    return 0;
}

int compare_name(const void* a, const void* b) {
    const char* name_a = ((struct user*)a)->name.addr;
    const char* name_b = ((struct user*)b)->name.addr;
    return strcmp(name_a, name_b);
}

int compare_city(const void* a, const void* b) {
    enum city city_a = ((struct user*)a)->city;
    enum city city_b = ((struct user*)b)->city;
    return strcmp(city_string[city_a], city_string[city_b]);
}

/* Сортировать массив пользователей по полю uid по возрастанию */
void users_sort_uid(struct user users[], size_t sz) {
    qsort(users, sz, sizeof(struct user), compare_uid);
}

/* Сортировать массив пользователей по полю name */
/* Порядок строк лексикографический; можно использовать компаратор
   строк -- стандартную функцию strcmp */
void users_sort_name(struct user users[], size_t sz) {
    qsort(users, sz, sizeof(struct user), compare_name);
}

/* Сортировать массив по _текстовому представлению_ города */
void users_sort_city(struct user users[], size_t sz) {
    qsort(users, sz, sizeof(struct user), compare_city);
}