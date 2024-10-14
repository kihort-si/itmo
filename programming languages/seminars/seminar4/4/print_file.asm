; print_file.asm
%define O_RDONLY 0
%define PROT_READ 0x1
%define MAP_PRIVATE 0x2
%define SYS_WRITE 1
%define SYS_OPEN 2
%define SYS_MMAP 9
%define FD_STDOUT 1

section .text
global print_file
global print_string

; use exit system call to shut down correctly
exit:
    mov  rax, 60
    xor  rdi, rdi
    syscall

; These functions are used to print a null terminated string
; rdi holds a string pointer
print_string:
    push rdi
    call string_length
    pop  rsi
    mov  rdx, rax
    mov  rax, SYS_WRITE
    mov  rdi, FD_STDOUT
    syscall
    ret

string_length:
    xor  rax, rax
    .loop:
        cmp  byte [rdi+rax], 0
        je   .end
        inc  rax
        jmp .loop
    .end:
        ret

; This function is used to print a substring with given length
; rdi holds a string pointer
; rsi holds a substring length
print_substring:
    mov  rdx, rsi
    mov  rsi, rdi
    mov  rax, SYS_WRITE
    mov  rdi, FD_STDOUT
    syscall
    ret

print_file:
    ; Вызовите open и откройте fname в режиме read only.
    mov  rax, SYS_OPEN
    mov  rsi, O_RDONLY    ; Open file read only
    mov  rdx, 0 	  ; We are not creating a file
                          ; so this argument has no meaning
    syscall
    ; rax holds the opened file descriptor now

    mov rdi, rax
    sub rsp, 144
    mov rsi, rsp
    mov rax, 5
    syscall

    mov r8, rax
    mov r9, 0
    mov r10, MAP_PRIVATE
    mov rdi, 0
    mov rax, SYS_MMAP
    mov rdx, PROT_READ
    mov rsi, [rsp + 48]
    syscall

    mov rdi, rax
    mov rsi, [rsp + 48]
    call print_substring

    mov rax, 11
    mov rdi, 0
    mov rsi, [rsp + 48]
    add rsp, 144
    syscall

    mov rax, 3
    mov rdi, rbx
    syscall

    jmp exit