   .data

input_addr:      .word  0x80
output_addr:     .word  0x84

    .text

.org 0x200

_start:
    addi    sp, zero, 512

    lui     t3, %hi(input_addr)
    addi    t3, t3, %lo(input_addr)
    lw      t3, 0(t3)        ; t3 = 0x80
    lw      t1, 0(t3)        ; t1 = M[0x80] = n

    ble     t1, zero, ret_neg

    addi    t0, zero, 1      ; t0 = i
    addi    t2, zero, 0      ; t2 = count

loop:
    bgt     t0, t1, end_loop ; если i > n — выход
    mv      a0, t0           ; a0 = i
    mv      a1, t1           ; a1 = n
    jal     ra, check_div    ; вложенный вызов
    addi    t0, t0, 1        ; i++
    j       loop

end_loop:
    lui     t3, %hi(output_addr)
    addi    t3, t3, %lo(output_addr)
    lw      t3, 0(t3)        ; t3 = 0x84
    sw      t2, 0(t3)        ; M[0x84] = count
    halt

ret_neg:
    ; n <= 0 -> запишем −1
    lui     t3, %hi(output_addr)
    addi    t3, t3, %lo(output_addr)
    lw      t3, 0(t3)
    addi    t4, zero, -1
    sw      t4, 0(t3)
    halt


; если (n % i) == 0 -> ++t2
check_div:
    addi    sp, sp, -4       ; зарезервировать место
    sw      ra, 0(sp)        ; сохранить ra

    rem     t3, a1, a0       ; t3 = n % i
    bnez    t3, cd_done      ; если != 0 — пропустить ++
    addi    t2, t2, 1        ; иначе ++count

cd_done:
    lw      ra, 0(sp)        ; восстановить ra
    addi    sp, sp, 4        ; освободить место
    jr      ra               ; возврат в loop