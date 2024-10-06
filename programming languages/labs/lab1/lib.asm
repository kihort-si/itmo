section .text

; Принимает код возврата и завершает текущий процесс
exit:
    mov rax, 60
    syscall

; Принимает указатель на нуль-терминированную строку, возвращает её длину
string_length:
    xor rax, rax

    .loop:
        cmp byte [rdi + rax], 0 ; проверяем закончилась ли строка
        je .return
        inc rax
        jmp .loop

    .return:
        ret

; Принимает указатель на нуль-терминированную строку, выводит её в stdout
print_string:
    push rdi
    call string_length
    pop rsi
    mov rdi, 1
    mov rdx, rax
    mov rax, 1
    syscall
    ret

; Принимает код символа и выводит его в stdout
print_char:
    push rdi
    mov rsi, rsp
    mov rdx, 1
    mov rdi, 1
    mov rax, 1
    syscall
    pop rdi
    ret

; Переводит строку (выводит символ с кодом 0xA)
print_newline:
    mov rdi, 1
    mov rsi, 0xA
    mov rdx, 1
    mov rax, 1
    syscall
    ret

; Выводит беззнаковое 8-байтовое число в десятичном формате
; Совет: выделите место в стеке и храните там результаты деления
; Не забудьте перевести цифры в их ASCII коды.
print_uint:
    mov rax, rdi
    mov rdi, rsp
    sub rsp, 24
    dec rdi
    mov byte [rdi], 0
    mov r10, 10

    .loop:
        xor rdx, rdx
        div r10
        add dl, '0'
        dec rdi
        mov byte [rdi], dl
        test rax, rax
        jnz .loop

    call print_string
    add rsp, 24
    ret

; Выводит знаковое 8-байтовое число в десятичном формате
print_int:
    cmp rdi, 0
    jns .positive
    push rdi
    mov rdi, '-'
    call print_char
    pop rdi
    neg rdi

    .positive:
        sub rsp, 8
        call print_uint
        add rsp, 8
        ret


; Принимает два указателя на нуль-терминированные строки, возвращает 1 если они равны, 0 иначе
string_equals:
    xor rax, rax

    push rdi
    push rsi

    .loop:
        mov r11b, byte [rdi]
        cmp r11b, byte [rsi]
        jne .return

        test r11b, r11b
        je .equal

        inc rdi
        inc rsi
        jmp .loop

    .equal:
        mov rax, 1

    .return:
        pop rdi
        pop rsi
        ret

; Читает один символ из stdin и возвращает его. Возвращает 0 если достигнут конец потока
read_char:
    xor rax, rax
    push 0
    mov rsi, rsp
    mov rdi, 0
    mov rdx, 1
    syscall
    pop rax
    ret

; Принимает: адрес начала буфера, размер буфера
; Читает в буфер слово из stdin, пропуская пробельные символы в начале, .
; Пробельные символы это пробел 0x20, табуляция 0x9 и перевод строки 0xA.
; Останавливается и возвращает 0 если слово слишком большое для буфера
; При успехе возвращает адрес буфера в rax, длину слова в rdx.
; При неудаче возвращает 0 в rax
; Эта функция должна дописывать к слову нуль-терминатор

read_word:
    push r12
    push r13
    push r15
    mov r12, rdi
    mov r13, rsi
    xor r15, r15

    .skip_whitespace:
        call read_char
        cmp al, 0x20
        je .skip_whitespace
        cmp al, 0x9
        je .skip_whitespace
        cmp al, 0xA
        je .skip_whitespace
        test al, al
        jz .add_null_term_string
        test r13, r13
        je .fail

    .read_loop:
        cmp r15, r13
        je .fail
        mov byte [r12 + r15], al
        inc r15
        call read_char
        test al, al
        jz .add_null_term_string
        cmp al, 0x20
        je .add_null_term_string
        cmp al, 0x9
        je .add_null_term_string
        cmp al, 0xA
        je .add_null_term_string
        jmp .read_loop

    .add_null_term_string:
        mov byte [r12 + r15], 0
        mov rax, r12
        mov rdx, r15
        pop r15
        pop r13
        pop r12
        ret

    .fail:
        xor rax, rax
        pop r15
        pop r13
        pop r12
        ret

; Принимает указатель на строку, пытается
; прочитать из её начала беззнаковое число.
; Возвращает в rax: число, rdx : его длину в символах
; rdx = 0 если число прочитать не удалось
parse_uint:
    xor rax, rax
    xor rdx, rdx
    mov r11, 10

    .loop:
        movzx rcx, byte [rdi+rdx]
        sub rcx, '0'
        cmp rcx, 9
        ja .return
        imul rax, r11
        add rax, rcx
        inc rdx
        jmp .loop

    .return:
        ret


; Принимает указатель на строку, пытается
; прочитать из её начала знаковое число.
; Если есть знак, пробелы между ним и числом не разрешены.
; Возвращает в rax: число, rdx : его длину в символах (включая знак, если он был)
; rdx = 0 если число прочитать не удалось
parse_int:
    mov cl, byte [rdi]
    cmp cl, '-'
    je .parse_number
    cmp cl, '+'
    jne parse_uint

    .parse_number:
        push rdi
        inc rdi
        call parse_uint
        pop rdi
        inc rdx
        cmp byte[rdi], '+'
        je .return
        neg rax

    .return:
        ret

; Принимает указатель на строку, указатель на буфер и длину буфера
; Копирует строку в буфер
; Возвращает длину строки если она умещается в буфер, иначе 0
string_copy:
    xor rax, rax

    .loop:
        cmp rax, rdx
        jae .fail_return
        mov r11b, byte [rdi + rax]
        mov byte [rsi + rax], r11b
        cmp r11b, 0
        je .return
        inc rax
        jmp .loop

    .fail_return:
        xor rax, rax
        ret

    .return:
        ret
