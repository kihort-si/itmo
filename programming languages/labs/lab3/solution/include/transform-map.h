//
// transform.h
// Created by Nikita on 22.11.2024.
//

#ifndef TRANSFORM_MAP_H
#define TRANSFORM_MAP_H

#include "image.h"

typedef struct image *(*transform_func_t)(const struct image *);

transform_func_t find_transformation(const char *name);

#endif //TRANSFORM_MAP_H
