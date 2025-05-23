import examples
from IO import *
from calculation import *
from plots import plot_solutions


def main():
    while True:
        print("Выберите ОДУ:")
        for i, s in enumerate(examples.ODU_strings, 1):
            print(f"{i}. {s}")
        system_choice = get_int("Ваш выбор: ", range(1, len(examples.ODU_strings) + 1))

        initial_conditions = get_float("Введите начальный y₀=y(x₀): ")
        while True:
            start = get_float("Введите начальную границу интервала: ")
            finish = get_float("Введите конечную границу интервала: ")
            if start >= finish:
                print("Ошибка: Начальная граница должна быть меньше конечной. Попробуйте снова.")
            else:
                break
        while True:
            h = get_float("Введите шаг: ")
            if h > (finish - start):
                print("Ошибка: Шаг больше, чем введенный интервал. Попробуйте снова.")
            else:
                break
        while True:
            accuracy = get_float("Введите точность: ")
            if accuracy <= 0:
                print("Ошибка: Точность должна быть положительным числом. Попробуйте снова.")
            else:
                break
        results = Calculation(examples.ODU[system_choice - 1], initial_conditions, start, finish, h, accuracy)
        print("\nРешение методом Эйлера:")
        print_euler_table(results.euler())
        print("\nПогрешность метода Эйлера (правило Рунге):")
        print(results.error_runge_rule_euler())
        plot_solutions(results, results.runge_kutt(), "Метод Эйлера vs точное решение")
        print("\nРешение методом Рунге-Кутта 4-го порядка:")
        print_rk4_table(results.runge_kutt())
        print("\nПогрешность метода Рунге-Кутта (правило Рунге):")
        print(results.error_runge_rule_rk4())
        plot_solutions(results, results.runge_kutt(), "Метод Рунге-Кутта 4 порядка vs точное решение")
        print("\nРешение методом Адамса:")
        print_adams_table(results.adams())
        print("\nПогрешность метода Адамса (сравнение с точным решением):")
        print(results.error_adams_vs_exact())
        plot_solutions(results, results.adams(), "Метод Адамса vs точное решение")

        break



if __name__ == '__main__':
    main()
