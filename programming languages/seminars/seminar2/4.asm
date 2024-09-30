section .data
    message: db 'abcdefg', 0

section .text
global _start

exit:
    mov  rax, 60
    xor  rdi, rdi
    syscall

string_length:
    xor rax, rax
    .loop:
        cmp byte [rdi + rax], 0
        je  .end
        inc rax
        jmp .loop
    .end:
        ret

print_string:
    call string_length
    mov  rdx, rax
    mov  rsi, rdi
    mov  rax, 1
    mov  rdi, 1
    syscall
    ret

parse_uint:
    xor rdx, rdx
    xor rax, rax
    .loop:
        movzx rbx, byte [rdi + rdx]
        sub rbx, '0'

        cmp rbx, 9
        ja  .return
        js  .return

        imul rax, rax, 10
        add  rax, rbx

        inc  rdx
        jmp  .loop

    .return:
        ret

_start:
    mov  rdi, message
    call print_string
    call exit
