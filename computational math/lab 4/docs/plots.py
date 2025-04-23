import matplotlib.pyplot as plt
import numpy as np


def draw_plot(x, y):
    plt.scatter(x, y, label="Введенные точки", color='red')
    plt.legend()
    plt.xlabel("x")
    plt.ylabel("y")
    plt.title("Аппроксимация функции")
    plt.grid(True)
    plt.show()


def draw_func(func, name, x, dx=0.001):
    x_min, x_max = min(x), max(x)
    x_min -= 0.1 * (x_max - x_min)
    x_max += 0.1 * (x_max - x_min)

    xs = np.arange(x_min, x_max, dx)
    ys = func(xs)

    plt.plot(xs, ys, label=name)
    plt.legend()

