; sepia.asm

section .rodata
    blue_coeffs: dd 0.131, 0.168, 0.189, 0.0
    green_coeffs: dd 0.534, 0.686, 0.769, 0.0
    red_coeffs: dd 0.272, 0.349, 0.393, 0.0
    max_value: dd 255.0, 255.0, 255.0, 255.0
    min_value: dd 0.0, 0.0, 0.0, 0.0

section .text
    global sepia_filter_sse

sepia_filter_sse:
    push rbp
    mov rbp, rsp

    mov r8, rsi             ; r8 = количество пикселей
    test r8, r8
    jz .exit                ; Если нет пикселей, выходим

.loop:
    ; Загрузка текущего пикселя
    movzx eax, byte [rdi]   ; Загрузка синего канала
    cvtsi2ss xmm0, eax      ; Преобразование в float
    shufps xmm0, xmm0, 0    ; Дублируем значение

    movzx eax, byte [rdi+1] ; Загрузка зелёного канала
    cvtsi2ss xmm1, eax
    shufps xmm1, xmm1, 0

    movzx eax, byte [rdi+2] ; Загрузка красного канала
    cvtsi2ss xmm2, eax
    shufps xmm2, xmm2, 0

    ; Применение коэффициентов
    movups xmm3, [blue_coeffs]
    mulps xmm0, xmm3        ; Умножение синего

    movups xmm4, [green_coeffs]
    mulps xmm1, xmm4        ; Умножение зелёного
    addps xmm0, xmm1        ; Сложение

    movups xmm5, [red_coeffs]
    mulps xmm2, xmm5        ; Умножение красного
    addps xmm0, xmm2        ; Сложение

    ; Ограничение результата
    minps xmm0, [max_value]
    maxps xmm0, [min_value]

    ; Преобразование float -> int
    cvtps2dq xmm0, xmm0     ; Преобразование в 32-битные целые числа

    ; Упаковка без `packusdw`
    pshufd xmm1, xmm0, 0b11101110 ; Перестановка, чтобы объединить 32-битные
    packssdw xmm0, xmm1     ; Упаковка в 16-битные

    ; Упаковка в 8-битные
    packuswb xmm0, xmm0     ; Упаковка в 8-битные
    movd eax, xmm0          ; Запись результата

    mov [rdi], al           ; Сохранение синего
    shr eax, 8
    mov [rdi+1], al         ; Сохранение зелёного
    shr eax, 8
    mov [rdi+2], al         ; Сохранение красного

    ; Переход к следующему пикселю
    add rdi, 3
    dec r8
    jnz .loop

.exit:
    pop rbp
    ret
