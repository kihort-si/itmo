import numpy as np
import matplotlib.pyplot as plt
from numba import jit


@jit(nopython=True)
def burning_ship(xmin, xmax, ymin, ymax, width, height, max_iter):
    real = np.linspace(xmin, xmax, width)
    imag = np.linspace(ymin, ymax, height)
    div_time = np.zeros((height, width), dtype=np.float64)

    for i in range(height):
        for j in range(width):
            c = complex(real[j], imag[i])
            z = complex(0, 0)
            n = 0
            while abs(z) <= 2 and n < max_iter:
                z = complex(abs(z.real), abs(z.imag))
                z = z * z + c
                n += 1
            if n < max_iter:
                div_time[i, j] = n + 1 - np.log(np.log2(abs(z)))
            else:
                div_time[i, j] = n
    div_time = div_time / div_time.max()
    return div_time


def plot_burning_ship(fractal, xmin, xmax, ymin, ymax, max_iter, cmap='inferno'):
    plt.figure(figsize=(10, 10))
    plt.imshow(fractal, extent=[xmin, xmax, ymin, ymax], cmap=cmap, interpolation='bilinear')
    plt.colorbar(label='Iterations')
    plt.title(f"Burning ship with max_iter = {max_iter}")
    plt.xlabel("Re(z)")
    plt.ylabel("Im(z)")
    plt.show()


def main():
    xmin, xmax = -2.0, -1.7
    ymin, ymax = -0.08, 0.025
    width, height = 1000, 1000
    max_iter = 1000

    fractal = burning_ship(xmin, xmax, ymin, ymax, width, height, max_iter)
    plot_burning_ship(fractal, xmin, xmax, ymin, ymax, max_iter)


if __name__ == "__main__":
    main()
