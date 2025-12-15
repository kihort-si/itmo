add_library(
    vtpc
    STATIC
    vtpc.c
        ../test/ema-sort-int.c
)

target_include_directories(
    vtpc
    PUBLIC
    .
)
