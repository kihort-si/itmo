//
// transform-map.c
// Created by Nikita on 22.11.2024.
//

#include <string.h>

#include "image.h"
#include "transform-map.h"
#include "transform.h"

static const struct {
    const char *name;
    transform_func_t func;
} transformations[] = {
    {"none", copy_image},
    {"cw90", rotate90_cw},
    {"ccw90", rotate90_ccw},
    {"fliph", flip_horizontal},
    {"flipv", flip_vertical},
};

transform_func_t find_transformation(const char *name) {
    size_t count = sizeof(transformations) / sizeof(transformations[0]);
    for (size_t i = 0; i < count; i++) {
        if (strcmp(transformations[i].name, name) == 0) {
            return transformations[i].func;
        }
    }
    return NULL;
}
