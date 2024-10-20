%include "src/lib.inc"

section .text

global find_word

find_word:
    .loop:
        test rsi, rsi
        jz .error
        push rdi
        push rsi
        sub rsp, 8
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