; print_hex.asm
section .data
codes:
    db      '0123456789ABCDEF'

section .text
global print_hex
global exit
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

exit:
    mov  rax, 60
    xor  rdi, rdi
    syscall
