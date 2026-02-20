#!/usr/bin/env bash
set -euo pipefail

export PATH="/opt/homebrew/opt/llvm/bin:$PATH"
CC=/opt/homebrew/opt/llvm/bin/clang

CFLAGS="--target=riscv32-unknown-elf -O2 -g -Wall -Wextra -ffreestanding -nostdlib -fuse-ld=lld"
LDFLAGS="-Wl,-T,kernel.ld -Wl,-Map,kernel.map"

$CC $CFLAGS $LDFLAGS -o kernel.elf kernel.c


qemu-system-riscv32 \
  -machine virt -smp 4 -bios default \
  -nographic -serial stdio -monitor none --no-reboot \
  -kernel kernel.elf
