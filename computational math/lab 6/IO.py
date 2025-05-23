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


def print_euler_table(results):
    col_widths = [4, 10, 10, 12]
    header = ["i", "x", "y", "f(x, y)"]

    line = "-" * (sum(col_widths) + 5 * 3 + 1)

    print(line)
    print("|", end="")
    for h, w in zip(header, col_widths):
        print(f" {h:^{w}} |", end="")
    print("\n" + line)

    for row in results:
        print("|", end="")
        for i, (value, w) in enumerate(zip(row, col_widths)):
            if i == 0:
                print(f" {int(value):^{w}} |", end="")
            else:
                print(f" {value:^{w}.6f} |", end="")
        print("\n" + line)


def print_rk4_table(results):
    col_widths = [4, 10, 10, 10, 10, 10, 10]
    headers = ["i", "x", "y", "k1", "k2", "k3", "k4"]

    line = "-" * (sum(col_widths) + 7 * 3 + 1)

    print(line)
    print("|", end="")
    for h, w in zip(headers, col_widths):
        print(f" {h:^{w}} |", end="")
    print("\n" + line)

    for row in results:
        print("|", end="")
        for i, (value, w) in enumerate(zip(row, col_widths)):
            if i == 0:
                print(f" {int(value):^{w}} |", end="")
            else:
                print(f" {value:^{w}.6f} |", end="")
        print("\n" + line)


def print_adams_table(results):
    col_widths = [4, 10, 14, 14]
    headers = ["i", "x", "y (corrector)", "f(x,y)"]
    line = "-" * (sum(col_widths) + len(col_widths) * 3 + 1)

    print(line)
    print("|", end="")
    for h, w in zip(headers, col_widths):
        print(f" {h:^{w}} |", end="")
    print("\n" + line)

    for row in results:
        full_row = list(row) + [None] * (len(col_widths) - len(row))

        print("|", end="")
        for i, (value, w) in enumerate(zip(full_row, col_widths)):
            if i == 0:
                print(f" {int(value):^{w}} |", end="")
            elif value is None:
                print(f" {'---':^{w}} |", end="")
            else:
                print(f" {value:^{w}.6f} |", end="")
        print("\n" + line)
