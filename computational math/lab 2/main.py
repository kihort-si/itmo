import calculation
import examples
import sideCalculations as sc


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


def get_data_from_file(file_name):
    try:
        with open(file_name, "r") as file:
            lines = [line.strip() for line in file.readlines()]
            accuracy = float(lines[0])
            if len(lines) >= 3:
                return accuracy, float(lines[1]), float(lines[2])
            elif len(lines) >= 2:
                return accuracy, float(lines[1])
            else:
                return accuracy,
    except Exception as e:
        print(f"Ошибка при чтении файла: {e}")
        return None


def solve_nonlinear_equation():
    print("Введите номер уравнения, которое нужно решить:")
    for i, s in enumerate(examples.function_strings, 1):
        print(f"{i}. {s}")
    fun = get_int("Ваш выбор: ", range(1, len(examples.function_strings) + 1))

    print("Введите номер метода решения:")
    for i, s in enumerate(examples.methods_strings, 1):
        print(f"{i}. {s}")
    method = get_int("Ваш выбор: ", range(1, len(examples.methods_strings) + 1))

    input_type = get_int("Для ввода данных с клавиатуры введите 1. Из файла — 2: ", [1, 2])

    accuracy, a, b, x0 = None, None, None, None

    if input_type == 1:
        accuracy = get_float("Введите точность: ")
    else:
        file_name = input("Введите имя файла (txt): ")
        result = get_data_from_file(file_name)
        if result is None:
            return
        accuracy = result[0]
        if method == 1 and len(result) >= 3:
            a, b = result[1], result[2]
        elif method != 1 and len(result) >= 2:
            x0 = result[1]

    if method == 1:
        if input_type == 1:
            print("Введите границы интервала:")
            a = get_float("a: ")
            b = get_float("b: ")
        result = calculation.method_of_chords(examples.functions[fun - 1], a, b, accuracy)
    else:
        if input_type == 1:
            x0 = get_float("Введите начальное приближение: ")
        if method == 2:
            result = calculation.newtons_method(examples.functions[fun - 1], sc.numerical_derivative, x0, accuracy)
        else:
            result = calculation.simple_iterations(examples.functions[fun - 1], sc.iteration_function, x0, accuracy)

    if result is None:
        output = "Корней нет или система не имеет решения."
    elif result == 1000:
        output = "Не удалось найти решение за 1000 итераций."
    else:
        output = (
            f"Корень: {result[0]}\n"
            f"Значение функции: {result[1]}\n"
            f"Количество итераций: {result[2]}"
        )

    print("Выберите способ вывода результата:")
    print("1 — вывести в консоль")
    print("2 — сохранить в файл result.txt")
    choice = input("Ваш выбор: ")

    if choice == "2":
        try:
            with open("result.txt", "w", encoding="utf-8") as file:
                file.write(output)
            print("Результат сохранён в файл result.txt")
        except Exception as e:
            print(f"Ошибка при записи в файл: {e}")
    else:
        print(output)


def solve_system_of_equations():
    print("Введите номер системы уравнений, которую нужно решить:")
    for i, s in enumerate(examples.system_strings, 1):
        print(f"{i}. {s}")
    system_choice = get_int("Ваш выбор: ", range(1, len(examples.system_strings) + 1))

    input_type = get_int("Для ввода данных с клавиатуры введите 1. Из файла — 2: ", [1, 2])
    accuracy = None
    x0 = []

    if input_type == 1:
        accuracy = get_float("Введите точность: ")

        print("Введите начальные приближения для каждой переменной:")
        for i in range(len(examples.system_variables[system_choice - 1])):
            x0.append(get_float(f"Введите начальное приближение для x{i + 1}: "))

    elif input_type == 2:
        file_name = input("Введите имя файла (txt): ")
        result = get_data_from_file(file_name)
        if result is None:
            return
        accuracy = result[0]
        if len(result) >= 3:
            x0 = [result[1], result[2]]
        elif len(result) >= 2:
            x0 = [result[1]]

    result = calculation.newton_system_method(examples.systems[system_choice - 1], sc.numerical_jacobian, x0, accuracy)

    if result is None:
        output = "Корней нет или система не имеет решения."
    elif result == 1000:
        output = "Не удалось найти решение за 1000 итераций."
    else:
        output = (
            f"Решение системы: {result[0]}\n"
            f"Количество итераций: {result[1]}\n"
            f"Погрешности: {result[2]}"
        )

    print("Выберите способ вывода результата:")
    print("1 — вывести в консоль")
    print("2 — сохранить в файл result.txt")
    choice = input("Ваш выбор: ")

    if choice == "2":
        try:
            with open("result.txt", "w", encoding="utf-8") as file:
                file.write(output)
            print("Результат сохранён в файл result.txt")
        except Exception as e:
            print(f"Ошибка при записи в файл: {e}")
    else:
        print(output)


def main():
    while True:
        print("Для решения нелинейного уравнения введите 1. Для системы нелинейных уравнений — 2.")
        type_choice = get_int("Ваш выбор: ", [1, 2])
        if type_choice == 1:
            solve_nonlinear_equation()
            break
        elif type_choice == 2:
            solve_system_of_equations()
            break
        else:
            print("Вы ввели что-то не то. Пожалуйста, введите 1 или 2.")


if __name__ == "__main__":
    main()
