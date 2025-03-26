import numpy as np

functions = [
    lambda x: 2.74 * x**3 - 1.93 * x**2 - 15.28 * x - 3.72,
    lambda x: np.sin(x) - 0.5 * x**2 + 3,
    lambda x: np.cos(x) + 3 * x**4 - 2,
    lambda x: x**5 - 10 * x**3 + 4 * x**2 - 7,
    lambda x: np.exp(x) - x**10 + 1
]

function_strings = [
    "2.74 * x**3 - 1.93 * x**2 - 15.28 * x - 3.72",
    "sin(x) - 0.5 * x^2 + 3",
    "cos(x) + 3 * x^4 - 2",
    "x^5 - 10 * x^3 + 4 * x^2 - 7",
    "exp(x) - x^10 + 1"
]

methods_strings = [
    "Метод хорд",
    "Метод Ньютона",
    "Метод простой итерации"
]

system_strings = [
    "f1(x, y) = tan(xy + 0.3) - x^2, f2(x, y) = 0.9x^2 + 2y^2 - 1",
    "f1(x, y) = sin(x + y) - 1.4x, f2(x, y) = x^2 + y ^2 - 1"
]

system_variables = [
    ['x', 'y'],
    ['x', 'y']
]

systems = [
    [
        lambda x: np.tan(x[0] * x[1] + 0.3) - x[0]**2,
        lambda x: 0.9 * x[0]**2 + 2 * x[1]**2 - 1
    ],
    [
        lambda x: np.sin(x[0] + x[1]) - x[0]*1.4,
        lambda x: x[0]**2 + x[1]**2 - 1
    ]
]