section .data
    message: db 'Variables output:', 0xA, 0

section .text
global _start

print_string:
    call string_length
    mov  rdx, rax
    mov  rsi, rdi
    mov  rax, 1
    mov  rdi, 1
    syscall
    ret

string_length:
    mov  rax, rdi
    .counter:
        cmp  byte [rdi], 0
        je   .end
        inc  rdi
        jmp  .counter
    .end:
        sub  rdi, rax
        mov  rax, rdi
        ret

print_hex:
    mov  rcx, 64
        .loop:
            sub  rcx, 4
            mov  rax, rdi
            sar  rax, cl
            and  rax, 0xf

            lea  rsi, [codes + rax]
            mov  rax, 1
            mov  rdi, 1
            mov  rdx, 1
            syscall

            test rcx, rcx
            jnz  .loop

            ret

process_variables:
    push rbp
    mov  rbp, rsp
    sub  rsp, 32
    and rsp, -16

    mov qword [rbp-8], 0xAA
    mov qword [rbp-16], 0xBB
    mov qword [rbp-24], 0xCC
    mov qword [rbp-32], 0xDD

    mov rdi, message
    call print_string

    mov rdi, [rbp-8]
    call print_hex

    mov rdi, [rbp-16]
    call print_hex

    mov rdi, [rbp-24]
    call print_hex

    mov rdi, [rbp-32]
    call print_hex

    mov rsp, rbp
    pop rbp
    ret

_start:
    call process_variables
    call exit

exit:
    mov  rax, 60
    xor  rdi, rdi
    syscall