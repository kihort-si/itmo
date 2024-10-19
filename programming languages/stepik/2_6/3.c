enum move_dir { MD_UP, MD_RIGHT, MD_DOWN, MD_LEFT, MD_NONE };

// Определите тип обработчика событий move_callback с помощью typedef
typedef void (*move_callback)(enum move_dir);

struct callback_node {
    move_callback cb;
    struct callback_node* next;
};

// Робот и его callback'и
// callback'ов может быть неограниченное количество
struct robot {
    const char* name;
    struct callback_node* callbacks;
};

// Добавить callback к роботу, чтобы он вызывался при движении
// В callback будет передаваться направление движения
void register_callback(struct robot* robot, move_callback new_cb) {
    struct callback_node* new_node = malloc(sizeof(struct callback_node));
    if (!new_node) {
        perror("Unable to allocate memory for new callback");
        exit(1);
    }
    new_node->cb = new_cb;
    new_node->next = robot->callbacks;
    robot->callbacks = new_node;
}

// Отменить все подписки на события.
// Это нужно чтобы освободить зарезервированные ресурсы
// например, выделенную в куче память
void unregister_all_callbacks(struct robot* robot) {
    struct callback_node* current = robot->callbacks;
    while (current != NULL) {
        struct callback_node* next = current->next;
        free(current);
        current = next;
    }
    robot->callbacks = NULL;
}

// Вызывается когда робот движется
// Эта функция должна вызвать все обработчики событий
void move(struct robot* robot, enum move_dir dir) {
    struct callback_node* current = robot->callbacks;
    while (current != NULL) {
        current->cb(dir);
        current = current->next;
    }
}