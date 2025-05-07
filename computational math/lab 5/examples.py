import math

func_names = [
    "Лагранж",
    "Ньютон (раздел)",
    "Ньютон (вперед)",
    "Ньютон (назад)",
    "Стирлинг",
    "Бессель"
]

available_functions = {
    1: ("sin(x)", lambda x: math.sin(x)),
    2: ("cos(x)", lambda x: math.cos(x)),
    3: ("exp(x)", lambda x: math.exp(x)),
    4: ("x^2", lambda x: x**2),
    5: ("ln(x)", lambda x: math.log(x)),
}
