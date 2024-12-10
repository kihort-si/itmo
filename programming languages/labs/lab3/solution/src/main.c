// main.c

#include <stdio.h>
#include <string.h>

#include "image.h"
#include "io.h"
#include "transform-map.h"

int main(int argc, char *argv[]) {
    if (argc != 4) {
        fprintf(stderr, "Usage: %s <source> <destination> <transformation>\n", argv[0]);
        fprintf(stderr, "Transformations: none, cw90, ccw90, fliph, flipv\n");
        return 1;
    }

    const char *source = argv[1];
    const char *destination = argv[2];
    const char *transformation = argv[3];

    // Open the original BMP file
    FILE *in = open_file_read(source);
    if (!in) {
        return 2;
    }

    // Uploading an image
    struct image *img = NULL;
    if (from_bmp(in, &img) != READ_OK) {
        fprintf(stderr, "Failed to read BMP file\n");
        fclose(in);
        return 12;
    }
    fclose(in);

    // Apply the transformation
    transform_func_t transform_func = find_transformation(transformation);
    if (!transform_func) {
        fprintf(stderr, "Unknown transformation: %s\n", transformation);
        free_image(img);
        return 1;
    }

    struct image *transformed = transform_func(img);
    free_image(img);

    // Check whether the transformation was successful or not
    if (!transformed) {
        fprintf(stderr, "Failed to apply transformation: %s\n", transformation);
        return 1;
    }

    // Save the converted image to a file
    FILE *out = open_file_write(destination);
    if (!out) {
        fclose(in);
        return 1;
    }

    if (to_bmp(out, transformed) != WRITE_OK) {
        fprintf(stderr, "Failed to write BMP file\n");
        fclose(out);
        free_image(transformed);
        return 1;
    }

    fclose(out);
    free_image(transformed);

    printf("Transformation '%s' applied successfully!\n", transformation);
    return 0;
}
