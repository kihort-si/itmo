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


def print_difference_table(table):
    num_columns = len(table[0])

    headers = ["x", "y"] + [f"Δ^{i}y" for i in range(1, num_columns - 1)]

    column_widths = []
    for i in range(num_columns):
        max_len = max(
            len(headers[i]) if i < len(headers) else 0,
            max(len(f"{row[i]:.3f}") if isinstance(row[i], float) else 0 for row in table)
        )
        column_widths.append(max_len + 2)

    header_line = "|".join(headers[i].center(column_widths[i]) for i in range(len(headers)))
    separator = "-" * len(header_line)
    print(separator)
    print(header_line)
    print(separator)

    for row in table:
        row_str = []
        for i in range(num_columns):
            if i < len(row) and row[i] is not None:
                cell = f"{row[i]:.3f}"
            else:
                cell = ""
            row_str.append(cell.center(column_widths[i]))
        print("|".join(row_str))
    print(separator)


def read_data_from_file(filename):
    xi = []
    yi = []
    try:
        with open(filename, 'r', encoding='utf-8') as f:
            lines = [line.strip() for line in f if line.strip()]

        if len(lines) < 2:
            raise ValueError("Файл должен содержать как минимум 2 строки: точку интерполяции и один узел.")

        x = float(lines[0])

        for line in lines[1:]:
            parts = line.split()
            if len(parts) != 2:
                raise ValueError(f"Неправильный формат строки: '{line}'. Ожидается два числа.")
            xi.append(float(parts[0]))
            yi.append(float(parts[1]))

        return x, xi, yi

    except Exception as e:
        print(f"Ошибка при чтении файла: {e}")
        return None, [], []
