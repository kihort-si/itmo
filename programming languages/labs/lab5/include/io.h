/* io.h */

#ifndef IO_H
#define IO_H
#include <stdio.h>

enum read_status {
    READ_OK = 0,
    READ_INVALID_SIGNATURE,
    READ_INVALID_BITS,
    READ_INVALID_HEADER
};

enum write_status {
    WRITE_OK = 0,
    WRITE_ERROR
};

FILE *open_file_read(const char *filename);

FILE *open_file_write(const char *filename);

#endif //IO_H

