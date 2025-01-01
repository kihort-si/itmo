cmake_minimum_required(VERSION 3.10)
project(FilterPerformanceTests C ASM_NASM)

set(CMAKE_C_STANDARD 99)

include_directories(
        ${CMAKE_SOURCE_DIR}/../include
)

set(SOURCES
        ../src/image.c
        ../src/filter.c
        ../src/io.c
        test.c
)

set(ASM_SOURCES
        ../asm/sepia.asm
)

enable_language(ASM_NASM)
set(CMAKE_ASM_NASM_FLAGS "-g -f elf64")
set(CMAKE_ASM_NASM_OBJECT_FORMAT elf64)

set(CMAKE_POSITION_INDEPENDENT_CODE OFF)
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -no-pie")
set(CMAKE_EXE_LINKER_FLAGS "${CMAKE_EXE_LINKER_FLAGS} -no-pie")

add_executable(test_performance ${SOURCES} ${ASM_SOURCES})

target_link_libraries(test_performance PRIVATE m)
