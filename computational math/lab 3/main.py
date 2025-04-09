import examples
from improperCalculation import ImproperIntegralCalculator
from calculation import Calculation


def get_int(prompt, valid_range=None):
    while True:
        try:
            value = int(input(prompt))
            if valid_range and value not in valid_range:
                print(f"Введите значение из диапазона: {valid_range}")
                continue
            return value
        except ValueError:
            print("Ошибка: введите целое число.")


def get_float(prompt):
    while True:
        try:
            return float(input(prompt))
        except ValueError:
            print("Ошибка: введите число с плавающей точкой.")


def main():
    while True:
        print("Введите номер функции, интеграл которой нужно решить:")
        for i, s in enumerate(examples.functions_strings, 1):
            print(f"{i}. {s}")
        fun = get_int("Ваш выбор: ", range(1, len(examples.functions_strings) + 1))

        while True:
            lower_limit = get_int("Введите нижний предел интегрирования: ")
            upper_limit = get_int("Введите верхний предел интегрирования: ")

            if lower_limit >= upper_limit:
                print("Ошибка: нижний предел должен быть меньше верхнего предела.")
            else:
                break

        improper = ImproperIntegralCalculator(examples.functions[fun - 1], lower_limit, upper_limit)
        breakpoints = improper.get_breakpoints()
        limits = None

        if breakpoints:
            if not improper.get_coverage():
                print("Интеграл расходится и не существует")
                break
            else:
                print(f"Точки разрыва: {breakpoints}")
                print("Интеграл сходится")
                limits = improper.find_limits()

        if limits:
            lower_limit = limits[0]
            upper_limit = limits[1]

        if 0 in breakpoints:
            limits.append(lower_limit)
            limits.append(-improper.eps)
            limits.append(improper.eps)
            limits.append(upper_limit)

        if improper.try_to_evaluate(lower_limit) is None or improper.try_to_evaluate(upper_limit) is None:
            print("Интеграл невозможно вычислить в пространстве действительных чисел")
            break

        print("Введите номер метода, которым нужно решить интеграл:")
        for i, s in enumerate(examples.methods_strings, 1):
            print(f"{i}. {s}")
        method = get_int("Ваш выбор: ", range(1, len(examples.methods_strings) + 1))

        accuracy = get_float("Введите точность: ")

        if limits and len(limits) == 4:
            result1 = Calculation(examples.functions[fun - 1], limits[0], limits[1], accuracy, method)
            result2 = Calculation(examples.functions[fun - 1], limits[2], limits[3], accuracy, method)
            result = result1.calculate()[0] + result2.calculate()[0]
            if result1 is None or result2 is None:
                print("Достигнуто максимальное количество разбиений (1000000), но ответ не найден.")
                break
            else:
                print(f'Результат интегрирования: {result.__round__(2) if result < 1e-5 else result}')
                print(f'Количество разбиений: {result1.calculate()[1] + result2.calculate()[1]}')
                break
        else:
            result = Calculation(examples.functions[fun - 1], lower_limit, upper_limit, accuracy, method)
            if result.calculate() is None:
                print("Достигнуто максимальное количество разбиений (1000000), но ответ не найден.")
                break
            else:
                print(f'Результат интегрирования: {result.calculate()[0]}')
                print(f'Количество разбиений: {result.calculate()[1]}')
                break


if __name__ == "__main__":
    main()
