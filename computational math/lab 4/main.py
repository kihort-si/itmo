import sys
from approximation import (linear_approximation, quadratic_approximation, cubic_approximation,
                           exponential_approximation, logarithmic_approximation, power_approximation)
from calculation import (compute_mean_squared_error, compute_measure_of_deviation,
                         compute_coefficient_of_determination, compute_pearson_correlation)
from plots import draw_func, draw_plot


def get_int(prompt, valid_range=None):
    while True:
        try:
            value = int(input(prompt))
            if valid_range and value not in valid_range:
                print(f"Введите значение из диапазона: {valid_range}")
            else:
                return value
        except ValueError:
            print("Ошибка: введите целое число.")


def run(functions, x, y, n):
    best_mse = float("inf")
    mses = []

    for approximation, name in functions:
        try:
            phi, *coeffs = approximation(x, y, n)
            s = compute_measure_of_deviation(x, y, phi)
            mse = compute_mean_squared_error(x, y, phi)
            r2 = compute_coefficient_of_determination(x, y, phi, n)

            mses.append((mse, name))
            if mse <= best_mse:
                best_mse, best_func = mse, name

            draw_func(phi, name, x)
            print_results(name, coeffs, mse, r2, s, approximation, x, y, n)
        except Exception as e:
            print(f"Ошибка приближения {name} функции: {e}\n")

    print_best_functions(mses, best_mse)
    draw_plot(x, y)


def print_results(name, coeffs, mse, r2, s, approximation, x, y, n):
    print(f"{name} аппроксимация:")
    print(f" Коэффициенты: {list(map(lambda cf: round(cf, 4), coeffs))}")
    print(f" Среднеквадратичное отклонение: {mse:.5f}")
    print(f" Коэффициент детерминации: {r2:.5f}")
    print(f" Мера отклонения: S = {s:.5f}")
    if approximation == linear_approximation:
        correlation = compute_pearson_correlation(x, y)
        print(f" Коэффициент корреляции Пирсона: r = {correlation}\n")
    else:
        print("\n")


def print_best_functions(mses, best_mse):
    best_funcs = [name for mse, name in mses if abs(mse - best_mse) < 1e-7]
    if len(best_funcs) == 1:
        print(f"Лучшая функция приближения: {best_funcs[0]}")
    else:
        print("Лучшие функции приближения:")
        for name in best_funcs:
            print(f'  {name}')


def get_data_from_file(filename):
    try:
        with open(filename, 'r') as file:
            x, y = zip(*[(float(line.split()[0]), float(line.split()[1])) for line in file if len(line.split()) == 2])
        return list(x), list(y), None
    except IOError as err:
        return None, None, f"Невозможно прочитать файл {filename}: {err}"


def read_data_from_input():
    x, y = [], []
    while True:
        data = input().strip()
        if data == '/stop':
            break
        try:
            xi, yi = map(float, data.split())
            x.append(xi)
            y.append(yi)
        except ValueError:
            print("Некорректный ввод. Попробуйте еще раз")
    return x, y


def main():
    while True:
        print("Для ввода данных выберите опцию:")
        print("1. Ввод из файла")
        print("2. Ввод с клавиатуры")
        option = get_int("Ваш выбор: ", [1, 2])

        if option == 1:
            while True:
                filename = input("Введите имя файла: ")
                x, y, error = get_data_from_file(filename)
                if error:
                    print(error)
                else:
                    break
            n = len(x)
            break
        elif option == 2:
            print("Введите /stop, чтобы закончить ввод")
            x, y = read_data_from_input()
            n = len(x)
            break
        else:
            print("Некорректный ввод. Попробуйте еще раз.")

    functions = select_functions(x, y)
    output_choice = select_output_choice()

    with open('output.txt', 'w') as output:
        if output_choice == 1:
            print("Выбран вариант вывода в файл 'output.txt'")
            sys.stdout = output

        run(functions, x, y, n)


def select_functions(x, y):
    if all(xi > 0 for xi in x):
        if all(yi > 0 for yi in y):
            return [
                (linear_approximation, "Линейная"),
                (quadratic_approximation, "Полиноминальная 2-й степени"),
                (cubic_approximation, "Полиноминальная 3-й степени"),
                (exponential_approximation, "Экспоненциальная"),
                (logarithmic_approximation, "Логарифмическая"),
                (power_approximation, "Степенная")
            ]
        else:
            return [
                (linear_approximation, "Линейная"),
                (quadratic_approximation, "Полиноминальная 2-й степени"),
                (cubic_approximation, "Полиноминальная 3-й степени"),
                (logarithmic_approximation, "Логарифмическая"),
            ]
    else:
        if all(yi > 0 for yi in y):
            return [
                (linear_approximation, "Линейная"),
                (quadratic_approximation, "Полиноминальная 2-й степени"),
                (cubic_approximation, "Полиноминальная 3-й степени"),
                (exponential_approximation, "Экспоненциальная"),
            ]
        else:
            return [
                (linear_approximation, "Линейная"),
                (quadratic_approximation, "Полиноминальная 2-й степени"),
                (cubic_approximation, "Полиноминальная 3-й степени"),
            ]


def select_output_choice():
    print("Вывод в файл 'output.txt' или в терминал?")
    print("1. Вывод в файл")
    print("2. Вывод в терминал")
    return get_int("Ваш выбор: ", [1, 2])


if __name__ == "__main__":
    main()
