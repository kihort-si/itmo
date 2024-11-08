import numpy as np
import matplotlib.pyplot as plt


def main():
    center = (0.0, 0.0)
    scale = 2.0
    resolution = (1600, 1600)
    c_values = [0.355 + 0.355j, -0.4 + 0.6j, -0.70176 - 0.3842j, -0.5251993 + 0.5251993j]
    max_iters = [50, 100, 150]

    plot_julia_set(center, scale, resolution, c_values, max_iters)


def julia_set(center, scale, resolution, c, max_iter):
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
            z = x + 1j * y
            iteration = 0
            while abs(z) <= 2 and iteration < max_iter:
                z = z ** 2 + c
                iteration += 1
            image[y_idx, x_idx] = iteration

    return image


def plot_julia_set(center, scale, resolution, c_values, max_iters):
    for c in c_values:
        for max_iter in max_iters:
            julia_image = julia_set(center, scale, resolution, c, max_iter)
            plt.imshow(julia_image, cmap="inferno", extent=(
                center[0] - scale, center[0] + scale, center[1] - scale, center[1] + scale))
            plt.colorbar(label='Iterations')
            plt.title(f"Julia Set for c = {c} with max_iter = {max_iter}")
            plt.xlabel("Re(z)")
            plt.ylabel("Im(z)")
            plt.show()


if __name__ == "__main__":
    main()
