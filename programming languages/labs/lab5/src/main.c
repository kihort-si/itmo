/* main.c */

#include <stdio.h>
#include <string.h>

#include "bmp.h"
#include "filter.h"
#include "filter_sse.h"
#include "image.h"
#include "io.h"

int main(int argc, char** argv) {
    if (argc != 4) {
        fprintf(stderr, "Usage: %s <source> <destination> <c|asm>\n", argv[0]);
        return 1;
    }

    const char *source = argv[1];
    const char *destination = argv[2];
    const char *mode = argv[3];

    if (strcmp(mode, "c") != 0 && strcmp(mode, "asm") != 0) {
        fprintf(stderr, "Invalid mode: %s. Use 'c' or 'asm'.\n", mode);
        return 1;
    }

    FILE *in = open_file_read(source);
    if (!in) {
        return 2;
    }

    struct image *img = NULL;
    if (from_bmp(in, &img) != READ_OK) {
        fprintf(stderr, "Failed to read BMP file\n");
        fclose(in);
        return 12;
    }
    fclose(in);

    if (strcmp(mode, "c") == 0) {
        struct image *sepia_image = sepia_filter(img);
        free_image(img);

        if (!sepia_image) {
            fprintf(stderr, "Failed to apply filter\n");
            return 1;
        }

        img = sepia_image;
    } else if (strcmp(mode, "asm") == 0) {
        sepia_filter_sse((uint8_t *)img->data, img->width * img->height);
    }

    FILE *out = open_file_write(destination);
    if (!out) {
        fclose(out);
        return 1;
    }

    if (to_bmp(out, img) != WRITE_OK) {
        fprintf(stderr, "Failed to write BMP file\n");
        fclose(out);
        free_image(img);
        return 1;
    }

    fclose(out);
    free_image(img);

    printf("Filter applied successfully!\n");
    return 0;
}
