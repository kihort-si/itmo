/* filter_sse.h */

#ifndef FILTER_SSE_H
#define FILTER_SSE_H

#include <stddef.h>
#include <stdint.h>

void sepia_filter_sse(uint8_t *data, size_t pixel_count);

#endif //FILTER_SSE_H
