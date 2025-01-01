/* io.c */

#include <stdint.h>
#include <stdio.h>

#include "bmp.h"
#include "image.h"
#include "io.h"

static inline uint32_t get_padding_size(const struct image *img) {
    return (4 - ((img->width * sizeof(struct pixel)) % 4)) % 4;
}

struct bmp_header create_bmp_header(const struct image *img) {
    return (struct bmp_header) {
        .bfType = BMP_TYPE,
        .bfileSize = sizeof(struct bmp_header) + (img->width * sizeof(struct pixel) + get_padding_size(img)) * img->height,
        .bfReserved = BMP_RESERVED,
        .bOffBits = sizeof(struct bmp_header),
        .biSize = DIB_HEADER_SIZE,
        .biWidth = img->width,
        .biHeight = img->height,
        .biPlanes = BMP_PLANES,
        .biBitCount = BMP_BIT_COUNT,
        .biCompression = BMP_COMPRESSION,
        .biSizeImage = (img->width * sizeof(struct pixel) + get_padding_size(img)) * img->height,
        .biXPelsPerMeter = BMP_X_PELS_PER_METER,
        .biYPelsPerMeter = BMP_Y_PELS_PER_METER,
        .biClrUsed = BMP_CLR_USED,
        .biClrImportant = BMP_CLR_IMPORTANT
    };
}

enum read_status from_bmp(FILE *in, struct image **img) {
    struct bmp_header header;
    if (fread(&header, sizeof(struct bmp_header), 1, in) != 1) {
        return READ_INVALID_HEADER;
    }

    if (header.bfType != BMP_TYPE) {
        return READ_INVALID_SIGNATURE;
    }

    if (header.biBitCount != BMP_BIT_COUNT || header.biCompression != BMP_COMPRESSION) {
        return READ_INVALID_BITS;
    }

    *img = create_image(header.biWidth, header.biHeight);
    if (!*img) {
        return READ_INVALID_HEADER;
    }

    uint32_t padding_size = get_padding_size(*img);

    fseek(in, (long)header.bOffBits, SEEK_SET);
    for (size_t y = 0; y < header.biHeight; ++y) {
        if (fread((*img)->data + y * header.biWidth, sizeof(struct pixel), header.biWidth, in) != header.biWidth) {
            return READ_INVALID_HEADER;
        }
        fseek(in, (long)padding_size, SEEK_CUR);
    }

    return READ_OK;
}

enum write_status to_bmp(FILE *out, const struct image *img) {
    uint32_t padding_size = get_padding_size(img);

    struct bmp_header header = create_bmp_header(img);

    if (fwrite(&header, sizeof(header), 1, out) != 1) {
        return WRITE_ERROR;
    }

    for (size_t y = 0; y < img->height; ++y) {
        if (fwrite(img->data + y * img->width, sizeof(struct pixel), img->width, out) != img->width) {
            return WRITE_ERROR;
        }

        uint8_t padding[3] = {0};
        if (fwrite(padding, padding_size, 1, out) != 1) {
            return WRITE_ERROR;
        }
    }

    return WRITE_OK;
}

FILE *open_file_read(const char *filename) {
    FILE *file = fopen(filename, "rb");
    if (!file) {
        perror("Failed to open input file");
    }
    return file;
}

FILE *open_file_write(const char *filename) {
    FILE *file = fopen(filename, "wb");
    if (!file) {
        perror("Failed to open output file");
    }
    return file;
}
