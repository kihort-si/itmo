section .data
    A dd 0x3178
    B dd 0x3178
    C dd 0x3182
    D dd 0xA179
    E dd 0xE183
    output_buffer db 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
    
section .text
    global _start

_start:
    xor eax, eax
    or eax, D
    or eax, B
    mov [A], eax
    mov eax, [C]
    sub eax, [A]
    mov [E], eax
    mov eax, [E]
    mov ecx, 10
    mov ebx, output_buffer + 9
    mov byte [ebx], 0

convert_loop:
    dec ebx
    xor edx, edx
    div ecx
    add dl, '0'
    mov [ebx], dl
    test eax, eax
    jnz convert_loop

    mov eax, 4
    mov ebx, 1
    mov ecx, output_buffer
    sub ecx, ebx
    add ecx, 9
    sub ecx, ebx
    mov edx, 10
    int 0x80

    mov eax, 1
    xor ebx, ebx
    int 0x80