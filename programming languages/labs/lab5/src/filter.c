/* filter.c */

#include <math.h>
#include <stddef.h>

#include "filter.h"

#include <stdio.h>

struct pixel calculate_sepia(struct pixel p) {
    struct pixel result;

    uint8_t originalR = p.r;
    uint8_t originalG = p.g;
    uint8_t originalB = p.b;

    int sepiaR = round(0.393 * originalR + 0.769 * originalG + 0.189 * originalB);
    int sepiaG = round(0.349 * originalR + 0.686 * originalG + 0.168 * originalB);
    int sepiaB = round(0.272 * originalR + 0.534 * originalG + 0.131 * originalB);

    // Limit values to 255
    result.r = sepiaR > 255 ? 255 : (sepiaR < 0 ? 0 : sepiaR);
    result.g = sepiaG > 255 ? 255 : (sepiaG < 0 ? 0 : sepiaG);
    result.b = sepiaB > 255 ? 255 : (sepiaB < 0 ? 0 : sepiaB);

    return result;
}

struct image *sepia_filter(struct image* image) {
    struct image *filtered = create_image(image->width, image->height);
    if (!filtered) return NULL;
    for (size_t i = 0; i < image->height * image->width; i++) {
        filtered->data[i] = calculate_sepia(image->data[i]);
    }
    return filtered;
}
