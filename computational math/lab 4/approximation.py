import numpy as np


def linear_approximation(x, y, n):
    x = np.array(x)
    y = np.array(y)

    sx = np.sum(x)
    sxx = np.sum(x ** 2)
    sy = np.sum(y)
    sxy = np.sum(x * y)

    a, b = np.linalg.solve(
        [
            [n, sx],
            [sx, sxx]
        ],
        [sy, sxy])
    return lambda xi: a + b * xi, a.item(), b.item()


def quadratic_approximation(x, y, n):
    x = np.array(x)
    y = np.array(y)

    sx = np.sum(x)
    sxx = np.sum(x ** 2)
    sxxx = np.sum(x ** 3)
    sxxxx = np.sum(x ** 4)
    sy = np.sum(y)
    sxy = np.sum(x * y)
    sxxy = np.sum(x ** 2 * y)

    a, b, c = np.linalg.solve(
        [
            [n, sx, sxx],
            [sx, sxx, sxxx],
            [sxx, sxxx, sxxxx]
        ],
        [sy, sxy, sxxy]
    )
    return lambda xi: a + b * xi + c * xi ** 2, a.item(), b.item(), c.item()


def cubic_approximation(x, y, n):
    x = np.array(x)
    y = np.array(y)

    sx = np.sum(x)
    sy = np.sum(y)
    sxy = np.sum(x * y)
    sxx = np.sum(x ** 2)
    sxxx = np.sum(x ** 3)
    sxxxx = np.sum(x ** 4)
    sxxxxx = np.sum(x ** 5)
    sxxxxxx = np.sum(x ** 6)
    sxxy = np.sum(x ** 2 * y)
    sxxxy = np.sum(x ** 3 * y)

    a, b, c, d = np.linalg.solve(
        [
            [n, sx, sxx, sxxx],
            [sx, sxx, sxxx, sxxxx],
            [sxx, sxxx, sxxxx, sxxxxx],
            [sxxx, sxxxx, sxxxxx, sxxxxxx]
        ],
        [sy, sxy, sxxy, sxxxy]
    )
    return lambda xi: a + b * xi + c * xi ** 2 + d * xi ** 3, a.item(), b.item(), c.item(), d.item()

def exponential_approximation(x, y, n):
    y_log = np.log(y)
    _, a_log, b_log = linear_approximation(x, y_log, n)

    a = np.exp(a_log)
    b = b_log

    return lambda xi: a * np.exp(b * xi), a.item(), b


def logarithmic_approximation(x, y, n):
    x_log = np.log(x)
    _, a, b = linear_approximation(x_log, y, n)

    return lambda xi: a + b * np.log(xi), a, b


def power_approximation(x, y, n):
    x_log = np.log(x)
    y_log = np.log(y)

    _, a_log, b_log = linear_approximation(x_log, y_log, n)

    a = np.exp(a_log)
    b = b_log

    return lambda xi: a * xi ** b, a.item(), b
