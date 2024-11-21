%include "src/lib.inc"

section .text

global find_word
global get_word_by_key

find_word:
	.loop:
        test rsi, rsi
        jz .error
        push rdi
        push rsi
        add rsi, 8
        call string_equals
        pop rsi
        pop rdi
        test rax, rax
        jnz .found
        mov rsi, [rsi]
        jmp .loop

    .error:
        xor rax, rax
        ret

    .found:
        mov rax, rsi
        add rax, 8
        ret

get_word_by_key:
    push rdi
    call string_length
    pop rdi
    add rdi, rax
    inc rdi
    ret