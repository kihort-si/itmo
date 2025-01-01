cmake_minimum_required(VERSION 3.25)
project(lab5 C ASM)

set(CMAKE_C_STANDARD 99)

include_directories(${CMAKE_SOURCE_DIR}/include)

set(sources
        src/main.c
        src/image.c
        src/filter.c
        src/io.c
)

set(asm_sources
        asm/sepia.asm
)

enable_language(ASM_NASM)
set(CMAKE_ASM_NASM_OBJECT_FORMAT elf64)
set(CMAKE_ASM_NASM_FLAGS "-f elf64")

set(CMAKE_POSITION_INDEPENDENT_CODE OFF)
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -no-pie")
set(CMAKE_EXE_LINKER_FLAGS "${CMAKE_EXE_LINKER_FLAGS} -no-pie")

add_executable(lab5 ${sources} ${asm_sources})

target_link_libraries(lab5 PRIVATE m)