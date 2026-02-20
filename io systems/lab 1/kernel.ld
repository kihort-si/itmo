OUTPUT_ARCH(riscv)
ENTRY(boot)

SECTIONS
{
  . = 0x80200000;

  .text.boot : {
    KEEP(*(.text.boot))
  }

  .text : {
    *(.text .text.*)
  }

  .rodata : {
    *(.rodata .rodata.*)
  }

  .data : {
    *(.data .data.*)
  }

  .bss : {
    __bss = .;
    *(.bss .bss.*)
    *(COMMON)
    __bss_end = .;
  }

  . = ALIGN(16);
  __stack_top = . + 128K;
}