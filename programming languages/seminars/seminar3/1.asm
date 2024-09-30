%macro pushMacro 1-*
    %assign i %0
        %rep i
            push %1
            %rotate 1
        %endrep
%endmacro

%macro popMacro 1-*
    %assign i %0
        %rep i
            pop %1
            %rotate 1
        %endrep
%endmacro

section .text
global _start
_start:
    mov rax, 10
    mov rbx, 20
    mov rcx, 30
    mov rdx, 40

    pushMacro rax, rbx, rcx, rdx

    mov rax, 100
    mov rbx, 100
    mov rcx, 100
    mov rdx, 100

    popMacro rax, rbx, rcx, rdx
    call exit

exit:
    mov rax, 60
    syscall