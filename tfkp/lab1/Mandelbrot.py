import numpy as np
import matplotlib.pyplot as plt


def main():
    center = (0.0, 0.0)
    scale = 2.0
    resolution = (800, 800)
    max_iters = [50, 100, 150]

    plot_mandelbrot_set(center, scale, resolution, max_iters)


def mandelbrot_set(center, scale, resolution, max_iter):
    width, height = resolution
    x_min = center[0] - scale
    x_max = center[0] + scale
    y_min = center[1] - scale
    y_max = center[1] + scale

    x_vals = np.linspace(x_min, x_max, width)
    y_vals = np.linspace(y_min, y_max, height)

    image = np.zeros((height, width))

    for x_idx, x in enumerate(x_vals):
        for y_idx, y in enumerate(y_vals):
            c = x + 1j * y
            z = 0
            iteration = 0
            while abs(z) <= 2 and iteration < max_iter:
                z = z ** 2 + c
                iteration += 1
            image[y_idx, x_idx] = iteration

    return image


def plot_mandelbrot_set(center, scale, resolution, max_iters):
    for max_iter in max_iters:
        mandelbrot_image = mandelbrot_set(center, scale, resolution, max_iter)
        plt.imshow(mandelbrot_image, cmap="inferno", extent=(
            center[0] - scale, center[0] + scale, center[1] - scale, center[1] + scale))
        plt.colorbar(label='Iterations')
        plt.title(f"Mandelbrot Set with max_iter = {max_iter}")
        plt.xlabel("Re(c)")
        plt.ylabel("Im(c)")
        plt.show()


if __name__ == "__main__":
    main()
