import numpy as np
import verificator
import plots


def method_of_chords(f, a, b, epsilon, max_iter=1000):
    if not verificator.check_root(f, a, b):
        return None

    x0, x1 = a, b
    for _ in range(max_iter):
        f0, f1 = f(x0), f(x1)
        if abs(f1 - f0) < 1e-12:
            break
        x2 = x1 - f1 * (x1 - x0) / (f1 - f0)
        if abs(x2 - x1) < epsilon:
            plots.plot_function(f, a, b, x2)
            return x2, f(x2), _ + 1
        x0, x1 = x1, x2
    plots.plot_function(f, a, b, x2)
    return max_iter


def newtons_method(f, df, x0, epsilon, max_iter=1000):
    for _ in range(max_iter):
        f0, df0 = f(x0), df(f, x0)
        if abs(df0) < 1e-12:
            break
        x1 = x0 - f0 / df0
        if abs(x1 - x0) < epsilon:
            plots.plot_function_without_points(f, x0, x1)
            return x1, f(x1), _ + 1
        x0 = x1
    plots.plot_function_without_points(f, x0, x1)
    return max_iter


def simple_iterations(f, phi, x0, epsilon, max_iter=1000):
    x = x0
    for _ in range(max_iter):
        x_next = phi(f, x)
        if abs(x_next - x) < epsilon:
            plots.plot_function_without_points(f, x0, x_next)
            return x_next, f(x_next), _ + 1
        x = x_next
    plots.plot_function_without_points(f, x0, x_next)
    return max_iter


def newton_system_method(f, J, x0, epsilon, max_iter=1000):
    x = np.array(x0)
    errors = []
    for _ in range(max_iter):
        F = np.array([fi(x) for fi in f])
        J_matrix = J(f, x)
        try:
            J_inv = np.linalg.inv(J_matrix)
        except np.linalg.LinAlgError:
            return None
        delta = np.dot(J_inv, F)
        x_next = x - delta
        error = np.linalg.norm(x_next - x)
        errors.append(float(error))
        if error < epsilon:
            plots.plot_functions_system(f[0], f[1], x_next, x0)
            return x_next, _, errors
        x = x_next
    return max_iter

