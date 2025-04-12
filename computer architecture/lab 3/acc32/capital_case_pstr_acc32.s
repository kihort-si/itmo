    .data

buf:                .byte   '________________________________'
intput_addr:        .word   0x80                    ; Адрес ввода
output_addr:        .word   0x84                    ; Адрес вывода

buff_addr:          .word   0x0                     ; Адрес начала буфера
buffer_ptr:         .word   0x0                     ; Указатель на текущую позицию записи в буфере
char_index:         .word   0x0                     ; Счётчик символов
buffer_max:         .word   0x20                    ; Максимальная длина буфера

a_lower:            .word   'a'                     ; Символ 'a'
z_lower:            .word   'z'                     ; Символ 'z'
a_upper:            .word   'A'                     ; Символ 'A'
z_upper:            .word   'Z'                     ; Символ 'Z'
case_shift:     	.word   32                      ; Разница между 'a' и 'A' в ASCII
newline:            .word   0xA                     ; Символ конца строки ('\n')
space:              .word   0x20                    ; Символ пробела (' ')

one:          		.word   1
zero:            	.word   0
byte_mask:          .word   0xFF                    ; Маска для выделения младшего байта
word_mask:          .word   0xFFFFFF00              ; Маска для очистки младшего байта
overflow_val:     	.word   0xCCCCCCCC              ; Значение при переполнении буфера

capitalize_next:    .word   1                       ; Флаг: нужно ли следующую букву сделать заглавной
char_temp:          .word   0x0                     ; Временное хранилище текущего символа

    .text

_start:
	; Инициализируем указатель на буфер (buff_addr + 1)
    load_addr       buff_addr
    add             one
    store_addr      buffer_ptr

	; Обнуляем счётчик символов
    load_addr       zero
    store_addr      char_index

read_loop:
    ; Проверка: достигнут ли лимит длины буфера
    sub             buffer_max
    beqz            handle_overflow

	; Читаем символ из intput_addr
    load_ind        intput_addr
    and             byte_mask                       ; Отбрасываем старшие байты
    store_addr      char_temp

    ; Проверка на конец строки (newline == tmp)
    load_addr       newline
    sub             char_temp
    beqz            finalize_and_print

    ; Проверка: пробел?
    load_addr       space
    sub             char_temp
    beqz            set_capitalize

    ; Проверка: флаг capitalize_next == 1?
    load_addr       capitalize_next
    beqz            lowercase_convert

uppercase_convert:
	; Проверка: tmp < 'a'? → не буква, выходим
    load_addr       char_temp
    sub             a_lower                     
    ble             clear_capitalize

    ; Проверка: tmp > 'z'? → не буква, выходим
    load_addr       z_lower
    sub             char_temp
    ble             clear_capitalize
	
    ; Преобразование в верхний регистр: tmp - 32
    load_addr       char_temp
    sub             case_shift
    store_addr      char_temp

clear_capitalize:
    ; Сбрасываем флаг capitalize_next = 0
    load_addr       zero
    store_addr      capitalize_next
    jmp             store_char

lowercase_convert:
    ; Проверка: tmp < 'A'? → не буква, не меняем
    load_addr       char_temp
    sub             a_upper
    ble             store_char

    ; Проверка: tmp > 'Z'? → не буква, не меняем
    load_addr       z_upper
    sub             char_temp
    ble             store_char

    ; Преобразование в нижний регистр: tmp + 32
    load_addr       char_temp
    add             case_shift
    store_addr      char_temp

    jmp             store_char

set_capitalize:
    ; Устанавливаем флаг capitalize_next = 1
    load_addr       one
    store_addr      capitalize_next

store_char:
    ; Сохраняем символ в буфер
    load_ind        buffer_ptr
    and             word_mask                       ; Очищаем младший байт
    or              char_temp                       ; Вставляем символ
    store_ind       buffer_ptr

    ; Увеличиваем указатель буфера
    load_addr       buffer_ptr
    add             one
    store_addr      buffer_ptr

    ; Увеличиваем индекс символов
    load_addr       char_index
    add             one
    store_addr      char_index

    jmp             read_loop

finalize_and_print:
    ; Сохраняем длину строки в buffer[0]
    load_ind        buff_addr
    and             word_mask
    or              char_index
    store_ind       buff_addr

    ; Сброс указателя на начало строки (buff_addr + 1)
    load_addr       buff_addr
    add             one
    store_addr      buffer_ptr    
    load_addr       char_index

print_loop:
    ; Если индекс == 0 → конец вывода
    beqz            exit

    ; Выводим символ
    load_ind        buffer_ptr
    and             byte_mask
    store_ind       output_addr
	
    ; Увеличиваем указатель буфера
    load_addr       buffer_ptr
    add             one
    store_addr      buffer_ptr

    ; Уменьшаем индекс символов
    load_addr       char_index
    sub             one                    
    store_addr      char_index

    jmp             print_loop

handle_overflow:
    ; Переполнение — выводим спец. значение
    load_addr       overflow_val
    store_ind       output_addr
    halt
	
exit:
    halt