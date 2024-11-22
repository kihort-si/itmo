//
// image.h
// Created by Nikita on 09.11.2024.
//

#ifndef IMAGE_H
#define IMAGE_H

#include <stdint.h>

struct pixel {
    uint8_t b, g, r;
};

struct image {
    uint64_t width, height;
    struct pixel *data;
};

/* Creates an image of the specified size, returns NULL on error */
struct image *create_image(uint64_t width, uint64_t height);

/* Frees up image-related resources */
void free_image(struct image *img);

/* Copies image, returns NULL on error */
struct image *copy_image(const struct image *src);

#endif // IMAGE_H
