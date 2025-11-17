add_executable(
    vtsh
    main.c
    shell.c
    shell.h
    util.c
    util.h
)

set_target_properties(vtsh
    PROPERTIES
    RUNTIME_OUTPUT_DIRECTORY "${CMAKE_SOURCE_DIR}/build/bin"
)

target_include_directories(
    vtsh
    PUBLIC
    .
)

target_link_libraries(
    vtsh
    PRIVATE
    libvtsh
)