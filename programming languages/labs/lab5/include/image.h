/* image.h */

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

#endif // IMAGE_H

