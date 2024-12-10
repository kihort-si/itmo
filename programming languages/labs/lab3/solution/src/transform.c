//
// transform.c
// Created by Nikita on 17.11.2024.
//

#include <inttypes.h>
#include <stddef.h>
#include <stdio.h>

#include "image.h"
#include "transform.h"

struct image *rotate90_cw(const struct image *src) {
    struct image *rotated = create_image(src->height, src->width);
    if (!rotated) return NULL;

    for (size_t y = 0; y < src->height; ++y) {
        for (size_t x = 0; x < src->width; ++x) {
            rotated->data[x * rotated->width + y] = src->data[y * src->width + (src->width - x - 1)];
        }
    }

    return rotated;
}

struct image *rotate90_ccw(const struct image *src) {
    struct image *rotated = create_image(src->height, src->width);
    if (!rotated) return NULL;

    for (size_t y = 0; y < src->height; ++y) {
        for (size_t x = 0; x < src->width; ++x) {
            rotated->data[x * rotated->width + rotated->width - y - 1] = src->data[y * src->width + x];
        }
    }

    return rotated;
}

struct image *flip_horizontal(const struct image *src) {
    struct image *flipped = create_image(src->width, src->height);
    if (!flipped) return NULL;

    for (size_t y = 0; y < src->height; ++y) {
        for (size_t x = 0; x < src->width; ++x) {
            flipped->data[y * src->width + (src->width - x - 1)] =
                    src->data[y * src->width + x];
        }
    }

    return flipped;
}

struct image *flip_vertical(const struct image *src) {
    struct image *flipped = create_image(src->width, src->height);
    if (!flipped) return NULL;

    for (size_t y = 0; y < src->height; ++y) {
        for (size_t x = 0; x < src->width; ++x) {
            flipped->data[y * src->width + x] = src->data[(src->height - y - 1) * src->width + x];
        }
    }

    return flipped;
}
