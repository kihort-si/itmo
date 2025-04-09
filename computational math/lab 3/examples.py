import math

functions_strings = [
    "f(x) = 2x³-4x²+6x-25",
    "f(x) = x³-5x²+3x-16",
    "f(x) = -x³-x²-2x+1",
    "f(x) = sin(x)",
    "f(x) = 1/(x+1)",
    "f(x) = e^(-x)",
    "f(x) = 1/x",
    "f(x) = 1/(√x)"
]

methods_strings = [
    "Метод левых прямоугольников",
    "Метод правых прямоугольников",
    "Метод центральных прямоугольников",
    "Метод трапеций",
    "Метод Симпсона"
]

functions = [
    lambda x: 2 * x ** 3 - 4 * x ** 2 + 6 * x - 25,
    lambda x: x ** 3 - 5 * x ** 2 + 3 * x - 16,
    lambda x: -x ** 3 - x ** 2 - 2 * x + 1,
    lambda x: math.sin(x),
    lambda x: 1 / (x + 1),
    lambda x: math.exp(-x),
    lambda x: 1 / x,
    lambda x: 1 / math.sqrt(x)
]
