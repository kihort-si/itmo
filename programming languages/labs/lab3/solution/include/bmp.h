//
// bmp.h
// Created by Nikita on 09.11.2024.
//

#ifndef BMP_H
#define BMP_H

#include <stdint.h>

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
