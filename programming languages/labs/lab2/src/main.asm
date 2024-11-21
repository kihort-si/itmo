; main.asm
%include "src/lib.inc"
%include "src/words.inc"
%include "src/dict.inc"

%define MAX_WORD_LENGTH 255

section .data
    big_error: db "Error: the string is too big", 0
    not_found_error: db "Error: not found", 0

section .text

global _start

_start:
    sub rsp, MAX_WORD_LENGTH
    mov rdi, rsp
    mov rsi, MAX_WORD_LENGTH
    call read_word

    test rax, rax
    mov rdi, big_error
    jz .print_error

    mov rdi, rax
    mov rsi, first_word
    call find_word

    test rax, rax
    mov rdi, not_found_error
    jz .print_error

    mov rdi, rax
    push rdi
    call string_length
    pop rdi
    add rdi, rax
    inc rdi

    call print_string
    call print_newline

    xor rdi, rdi
    jmp exit

    .print_error:
        call print_string
        call print_newline
        mov rdi, 1
        jmp exit