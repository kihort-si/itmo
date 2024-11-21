; main.asm
%include "src/lib.inc"
%include "src/words.inc"
%include "src/dict.inc"

%define MAX_WORD_LENGTH 256

section .data
    big_error: db "Error: the string is too big", 0
    not_found_error: db "Error: not found", 0

section .text

global _start
_start:
    sub rsp, MAX_WORD_LENGTH
	mov rdi, rsp
	mov rsi, MAX_WORD_LENGTH
	call read_string

	test rax, rax
	jz .buff_err

	mov rdi, rax
	mov rsi, first_word
	call find_word

	.find:
        call find_word
        test rax, rax
        jz .nf_err
        mov rdi, rax
        call get_word_by_key
        call print_string
        call print_newline

	add rsp, MAX_WORD_LENGTH
	xor rdi, rdi
	call exit

    .buff_err:
        mov rdi, big_error
        call print_error
        mov rdi, 1
        call exit

    .nf_err:
        mov rdi, not_found_error
        call print_error
        mov rdi, 1
        call exit
