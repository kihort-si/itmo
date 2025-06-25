    .data

input_addr:     .word  0x80
output_addr:    .word  0x84
stack_top:      .word  0x200

    .text
    .org 0x100

_start:
    movea.l  stack_top, A7
    movea.l  (A7), A7

    movea.l  input_addr, A0
    movea.l  (A0), A0

    move.l   (A0), D0

count_ones:
    link A6, -8
    move.l D1, -4(A6)
    move.l D2, -8(A6)
    
    move.l 0, D2            ; счётчик (D2 = 0)
    
count_loop:
    cmp.l 0, D0
    beq count_done          ; ноль -> завершение
    
    move.l D0, -(A7)
    jsr save_data
    add.l D0, D2
    move.l (A7)+, D0
    
    lsr.l 1, D0
    jmp count_loop

count_done:
    move.l D2, D0
    move.l -8(A6), D2
    move.l -4(A6), D1
    unlk A6

    movea.l  output_addr, A1
    movea.l  (A1), A1

    move.l   D0, (A1)
    halt

save_data:
    move.l D1, -(A7)
    jsr get_lsb
    move.l (A7)+, D1
    rts

get_lsb:
    move.l D0, D1
    and.l 1, D1             ; выделение младшего бита
    move.l D1, D0
    rts