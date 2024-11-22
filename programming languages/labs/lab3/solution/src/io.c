#include <stdint.h>
#include <stdio.h>

#include "bmp.h"
#include "image.h"
#include "io.h"

static inline uint32_t get_padding_size(const struct image *img) {
    return (4 - ((img->width * sizeof(struct pixel)) % 4)) % 4;
}

enum read_status from_bmp(FILE *in, struct image **img) {
    struct bmp_header header;
    if (fread(&header, sizeof(struct bmp_header), 1, in) != 1) return READ_INVALID_HEADER;

    if (header.bfType != 0x4D42) return READ_INVALID_SIGNATURE;
    if (header.biBitCount != 24 || header.biCompression != 0) return READ_INVALID_BITS;

    *img = create_image(header.biWidth, header.biHeight);
    if (!*img) return READ_INVALID_HEADER;

    uint32_t padding_size = get_padding_size(*img);

    fseek(in, (long)header.bfOffBits, SEEK_SET);
    for (uint32_t y = 0; y < header.biHeight; ++y) {
        if (fread((*img)->data + y * header.biWidth, sizeof(struct pixel), header.biWidth, in) != header.biWidth) {
            return READ_INVALID_HEADER;
        }
        fseek(in, (long)padding_size, SEEK_CUR);
    }
    return READ_OK;
}

enum write_status to_bmp(FILE *out, const struct image *img) {
    uint32_t padding_size = get_padding_size(img);
    uint32_t row_with_padding = img->width * sizeof(struct pixel) + padding_size;

    struct bmp_header header = {
        .bfType = 0x4D42,
        .bfileSize = sizeof(struct bmp_header) + row_with_padding * img->height,
        .bfReserved = 0,
        .bfOffBits = sizeof(struct bmp_header),
        .biSize = 40,
        .biWidth = img->width,
        .biHeight = img->height,
        .biPlanes = 1,
        .biBitCount = 24,
        .biCompression = 0,
        .biSizeImage = row_with_padding * img->height,
        .biXPelsPerMeter = 2835,
        .biYPelsPerMeter = 2835,
        .biClrUsed = 0,
        .biClrImportant = 0
    };

    if (fwrite(&header, sizeof(header), 1, out) != 1) return WRITE_ERROR;

    for (uint32_t y = 0; y < img->height; ++y) {
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
