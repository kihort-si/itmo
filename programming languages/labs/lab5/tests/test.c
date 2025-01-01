#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <string.h>
#include <time.h>
#include "bmp.h"
#include "image.h"
#include "io.h"
#include "filter.h"

void sepia_filter_sse(uint8_t *data, size_t pixel_count);

void measure_time(const char *label, void (*filter_func)(struct image *), struct image *img) {
    struct image *img_copy = create_image(img->width, img->height);
    if (!img_copy) {
        fprintf(stderr, "Failed to allocate memory for image copy\n");
        return;
    }
    memcpy(img_copy->data, img->data, img->width * img->height * sizeof(struct pixel));

    clock_t start = clock();
    filter_func(img_copy);
    clock_t end = clock();

    double elapsed_time = (double)(end - start) / CLOCKS_PER_SEC;
    printf("%s: %.6f seconds\n", label, elapsed_time);

    free_image(img_copy);
}

void c_filter_wrapper(struct image *img) {
    struct image *filtered = sepia_filter(img);
    if (filtered) {
        memcpy(img->data, filtered->data, img->width * img->height * sizeof(struct pixel));
        free_image(filtered);
    } else {
        fprintf(stderr, "Failed to apply C filter\n");
    }
}

void asm_filter_wrapper(struct image *img) {
    sepia_filter_sse((uint8_t *)img->data, img->width * img->height);
}

int main(int argc, char **argv) {
    if (argc != 2) {
        fprintf(stderr, "Usage: %s <input_image>\n", argv[0]);
        return 1;
    }

    const char *input_file = argv[1];

    FILE *in = fopen(input_file, "rb");
    if (!in) {
        perror("Failed to open input file");
        return 1;
    }

    struct image *img = NULL;
    if (from_bmp(in, &img) != READ_OK) {
        fprintf(stderr, "Failed to read BMP file\n");
        fclose(in);
        return 1;
    }
    fclose(in);

    printf("Measuring performance...\n");
    measure_time("C implementation", c_filter_wrapper, img);
    measure_time("ASM implementation", asm_filter_wrapper, img);

    free_image(img);
    return 0;
}
