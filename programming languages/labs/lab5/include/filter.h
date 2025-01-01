/* filter.h */

#ifndef FILTER_H
#define FILTER_H

#include "image.h"

struct pixel calculate_sepia(struct pixel p);

struct image *sepia_filter(struct image* image);

#endif //FILTER_H
