/* image.c */

#include <stdint.h>
#include <stdlib.h>
#include <string.h>

#include "image.h"

struct image *create_image(uint64_t width, uint64_t height) {
    struct image *img = malloc(sizeof(struct image));
    if (!img) return NULL;

    img->width = width;
    img->height = height;
    img->data = malloc(width * height * sizeof(struct pixel));
    if (!img->data) {
        free(img);
        return NULL;
    }

    return img;
}

void free_image(struct image *img) {
    if (img) {
        free(img->data);
        free(img);
    }
}