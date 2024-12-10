//
// io.h
// Created by Nikita on 12.11.2024.
//

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

/* Reading BMP into an image */
enum read_status from_bmp(FILE *in, struct image **img);

/* Writing an image to BMP */
enum write_status to_bmp(FILE *out, const struct image *img);

FILE *open_file_read(const char *filename);

FILE *open_file_write(const char *filename);

#endif //IO_H
