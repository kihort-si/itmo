int is_whitespace(char c) { return c == ' ' || c == '\t' || c == '\n'; }

int string_count(char* str) {
    int length = 0;
    while (str[length] != '\0') {
        length++;
    }
    return length;
}

int string_words(char* str)  {
    int count = 0;
    int in_word = 0;

    while (*str != '\0') {
        if (is_whitespace(*str)) {
            in_word = 0;
        } else if (!in_word) {
            in_word = 1;
            count++;
        }
        str++;
    }

    return count;
}