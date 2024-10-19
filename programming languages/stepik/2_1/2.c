size_t count_zeroes(const void* data, size_t sz) {
    size_t count = 0;
    const uint8_t* bytes = (const uint8_t*)data; // Преобразуем указатель к типу байтов

    for (size_t i = 0; i < sz; i++) {
        if (bytes[i] == 0) { // Разыменовываем указатель для доступа к значению байта
            count++;
        }
    }
    return count;
}
