.data
input_addr:     .word 0x80
output_addr:    .word 0x84
main_mask:      .word 0x80000001
counter:        .word 0x10
low_mask:       .word 0x0000FFFF
up_mask:        .word 0xFFFF0000
ones_mask:      .word 0x7FFFFFFF
temp_addr:      .word 0x90

.org 0x200
.text
_start:
    @p input_addr a! @

    dup
    @p up_mask and
    a!                              \ старшие 16 бит -> A

    dup
    @p low_mask and
    @p temp_addr b!                 \ загрузка адреса во временную область -> B
    !b                              \ младшие 16 бит -> память по адресу в B

    loop
    halt

loop:
    @p counter
    dup
    if palindrome                   \ если счётчик == 0 -> палиндром

    lit -1 +
    !p counter

    load_parts
    +                               \ складываем младшие + старшие

    @p main_mask and                \ применяем маску и оставляем только крайние биты

    dup
    if do_shift                     \ проверяем, что получили 0...0
    dup
    @p ones_mask +
    if do_shift                     \ проверяем, что получили 1...1
    lit 0 @p output_addr a! !
    ;

do_shift:
    shift_parts
    loop
    ;

load_parts:
    @b                              \ загружаем младшие биты в стек
    a                               \ загружаем старшие биты в стек
    ;

shift_parts:
    @b
    2/
    !b

    a 2*
    a!
    ;
    
palindrome:
    lit 1 @p output_addr a! !
    halt
