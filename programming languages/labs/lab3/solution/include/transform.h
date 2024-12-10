//
// transform.h
// Created by Nikita on 17.11.2024.
//

#ifndef TRANSFORM_H
#define TRANSFORM_H

#include "image.h"

struct image *rotate90_cw(const struct image *src);
struct image *rotate90_ccw(const struct image *src);
struct image *flip_horizontal(const struct image *src);
struct image *flip_vertical(const struct image *src);

#endif //TRANSFORM_H
