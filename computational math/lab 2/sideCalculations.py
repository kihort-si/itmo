import numpy as np


def numerical_derivative(f, x, h=1e-5):
    return (f(x + h) - f(x - h)) / (2 * h)


def iteration_function(f, x, h=1e-5):
    df = (f(x + h) - f(x - h)) / (2 * h)
    return x - f(x) / df


def numerical_jacobian(system, x, h=1e-5):
    n = len(system)
    jacobian = np.zeros((n, n))

    for i in range(n):
        for j in range(n):
            x_plus = x.copy()
            x_minus = x.copy()
            x_plus[j] += h
            x_minus[j] -= h
            jacobian[i, j] = (system[i](x_plus) - system[i](x_minus)) / (2 * h)

    return jacobian
