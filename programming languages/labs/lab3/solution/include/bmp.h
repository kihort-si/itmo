//
// bmp.h
// Created by Nikita on 09.11.2024.
//

#ifndef BMP_H
#define BMP_H

#include <stdint.h>

#define BMP_TYPE 0x4D42
#define BMP_RESERVED 0
#define DIB_HEADER_SIZE 40
#define BMP_PLANES 1
#define BMP_BIT_COUNT 24
#define BMP_COMPRESSION 0
#define BMP_X_PELS_PER_METER 2835
#define BMP_Y_PELS_PER_METER 2835
#define BMP_CLR_USED 0
#define BMP_CLR_IMPORTANT 0

// Setting the structure alignment by 1 byte
#pragma pack(push, 1)

struct bmp_header
{
    uint16_t bfType;
    uint32_t bfileSize;
    uint32_t bfReserved;
    uint32_t bfOffBits;
    uint32_t biSize;
    uint32_t biWidth;
    uint32_t biHeight;
    uint16_t biPlanes;
    uint16_t biBitCount;
    uint32_t biCompression;
    uint32_t biSizeImage;
    uint32_t biXPelsPerMeter;
    uint32_t biYPelsPerMeter;
    uint32_t biClrUsed;
    uint32_t biClrImportant;
};

#pragma pack(pop)

#endif //BMP_H
