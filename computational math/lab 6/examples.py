import math

ODU_strings = [
    "y′ = y + (1 + x)·y²",
    "y′ = x² − y",
    "y′ = eˣ − y"
]

ODU = [
    lambda x, y: y + (1 + x) * y**2,
    lambda x, y: x**2 - y,
    lambda x, y: math.exp(x) - y
]
