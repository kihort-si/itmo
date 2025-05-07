import IO
import calculation
import plots
import examples


def main():
    io = IO
    print("Вы можете вести данные в одном из трех форматов:")
    print("1. Ввод через консоль в виде набора данных")
    print("2. Ввод из файла")
    print("3. На основе выбранной функции, из тех, которые предлагает программа")
    type = io.get_int("Введите тип ввода: ", [1, 2, 3])
    if type == 1:
        x = io.get_float("Введите точку интерполяции: ")
        xi = []
        yi = []
        print("\nВведите узлы интерполяции в формате 'x y' (например, '1.0 2.0')")
        print("Для завершения ввода введите 'q' или оставьте строку пустой")
        while True:
            user_input = input("> ").strip()

            if user_input.lower() in ('q', ''):
                if len(xi) < 2:
                    print("Нужно ввести хотя бы 2 узла!")
                    continue
                break

            parts = user_input.split()
            if len(parts) != 2:
                print("Ошибка! Нужно ввести ровно 2 числа через пробел")
                continue

            try:
                x_val = float(parts[0])
                y_val = float(parts[1])
                if x_val in xi:
                    print(f"Ошибка! Узел с x = {x_val} уже существует. Введите уникальное значение x.")
                    continue
                xi.append(x_val)
                yi.append(y_val)
            except ValueError:
                print("Ошибка! Введите числа в формате 'x y'")
    elif type == 2:
        filename = input("Введите имя файла (например, data.txt): ").strip()
        x, xi, yi = io.read_data_from_file(filename)
        if len(xi) < 2:
            print("Недостаточно узлов для интерполяции.")
            return
        if len(set(xi)) != len(xi):
            print("Ошибка! В файле содержатся одинаковые значения x. Убедитесь, что x уникальны.")
            return
    elif type == 3:
        print("\nВыберите функцию:")
        for key, (name, _) in examples.available_functions.items():
            print(f"{key}. {name}")

        func_choice = io.get_int("Введите номер функции: ", list(examples.available_functions.keys()))
        func_name, func = examples.available_functions[func_choice]

        a = io.get_float("Введите левую границу интервала a: ")
        if func_choice == 5 and a <= 0:
            print("Ошибка: Левая граница не может быть меньше 0.")
            return
        b = io.get_float("Введите правую границу интервала b: ")

        if a >= b:
            print("Ошибка: Левая граница должна быть меньше правой.")
            return

        n = io.get_int("Введите количество точек (n >= 2): ", range(2, 1000))
        x = io.get_float("Введите точку интерполяции: ")

        if not (a <= x <= b):
            print("Ошибка: Точка интерполяции должна быть внутри интервала.")
            return

        xi = [a + i * (b - a) / (n - 1) for i in range(n)]
        yi = [func(x_val) for x_val in xi]
    if not (min(xi) <= x <= max(xi)):
        print("Ошибка: Точка интерполяции должна быть внутри диапазона известных точек")
        return
    print("\nВы ввели узлы интерполяции:")
    for x_val, y_val in zip(xi, yi):
        print(f"x = {x_val}, y = {y_val}")
    print("\nТаблица разностей:")
    io.print_difference_table(calculation.compute_differences(xi, yi))
    print("\nРезультат интерполяции:")
    func_values = [
        calculation.lagrange_interpolation(xi, yi, x),
        calculation.newton_divided_differences(xi, yi, x),
        calculation.newton_forward_difference(xi, yi, x),
        calculation.newton_backward_difference(xi, yi, x),
    ]
    print("\nМногочлен Лагранжа:", func_values[0])
    print("\nМногочлен Ньютона с разделенными разностями:", func_values[1])
    print("\nМногочлен Ньютона с прямыми разностями (первая формула):", func_values[2])
    print("\nМногочлен Ньютона с прямыми разностями (вторая формула):", func_values[3])
    plots.plot_interpolation_results(xi, yi, x, func_values, examples.func_names)


if __name__ == "__main__":
    main()