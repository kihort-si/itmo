file(GLOB_RECURSE sources CONFIGURE_DEPENDS
    src/*.c
    src/*.h
    include/*.h
)

add_executable(image-transform ${sources}
        src/main.c
        include/transform.h
        include/image.h
        src/image.c
        src/transform.c
        include/io.h
        src/io.c
        include/transform-map.h
        src/transform-map.c)
target_include_directories(image-transform PRIVATE src include)
