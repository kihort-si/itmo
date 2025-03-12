import csv
import numpy as np
import random


def generate_random_matrix(min_val=-25, max_val=25):
    size = random.randint(2, 20)
    matrix = [[random.uniform(min_val, max_val) for _ in range(size)] for _ in range(size)]
    vector = [random.uniform(min_val, max_val) for _ in range(size)]

    for i in range(size):
        matrix[i].append(vector[i])

    return matrix


def read_matrix_from_file(filename):
    matrix = []
    try:
        with open(filename, newline='') as file:
            if filename.endswith('.csv'):
                reader = csv.reader(file)
                rows = [row for row in reader]
            else:
                rows = [line.split() for line in file]

            for row in rows:
                if not all(num.replace('.', '', 1).replace('-', '', 1).isdigit() for num in row):
                    raise ValueError("Ошибка: все элементы должны быть вещественными числами")
                matrix.append([float(num) for num in row])
        return matrix
    except FileNotFoundError:
        print("Такой файл не найден. Попробуйте снова.")
        return read_matrix_from_file(input())


def validate_matrix(matrix):
    n = len(matrix)
    for row in matrix:
        if len(row) != n + 1:
            raise ValueError("Ошибка: каждая строка должна содержать n элементов матрицы и один элемент вектора")


def diagonal_dominance(matrix):
    matrix = np.array(matrix)
    n = len(matrix)
    for i in range(n):
        row_sum = sum(abs(matrix[i, j]) for j in range(n) if j != i)
        if abs(matrix[i, i]) <= row_sum:
            return False
    return True


def rearrange_matrix(matrix, vector):
    n = len(matrix)
    matrix = np.array(matrix, dtype=float)
    vector = np.array(vector, dtype=float)
    for i in range(n):
        max_index = max(range(i, n),
                        key=lambda k: abs(matrix[k, i]) - sum(abs(matrix[k, j]) for j in range(n) if j != i))
        if max_index != i:
            matrix[[i, max_index]] = matrix[[max_index, i]]
            vector[i], vector[max_index] = vector[max_index], vector[i]
    return matrix, vector


def norm_matrix(matrix):
    return max(sum(abs(element) for element in row) for row in matrix)


def iteration(matrix, x, vector):
    n = len(matrix)
    x_new = np.zeros(n)
    for i in range(n):
        sum_ax = sum(matrix[i][j] * x[j] for j in range(n) if j != i)
        x_new[i] = (vector[i] - sum_ax) / matrix[i][i]
    return x_new


def iterative_method(matrix, vector, precision, max_iterations=1000):
    n = len(matrix)
    x = np.zeros(n)
    iterations = 0
    errors = []

    while iterations <= max_iterations:
        x_new = iteration(matrix, x, vector)
        errors.append(np.abs(x_new - x))

        iterations += 1

        if np.max(np.abs(x_new - x)) < precision:
            return x_new, iterations, errors

        x = x_new

    print("Метод не сошёлся за", max_iterations, "итераций")
    return x, max_iterations, errors


def calculate_matrix(matrix, precision):
    validate_matrix(matrix)
    matrix = np.array(matrix, dtype=float)
    vector = matrix[:, -1]
    matrix = matrix[:, :-1]

    if not diagonal_dominance(matrix):
        matrix, vector = rearrange_matrix(matrix, vector)
        if not diagonal_dominance(matrix):
            print("Ошибка: матрица не является диагонально доминирующей и не может быть преобразована")
            return

    norm = norm_matrix(matrix)
    x, iterations, errors = iterative_method(matrix, vector, precision)

    print("Матрица после преобразований: ")
    for i in range(len(matrix)):
        print([float('%.2f' % elem) for elem in matrix[i]])
    print("Вектор свободных членов после преобразований: ", vector)
    print("Норма матрицы: ", norm)
    print("Вектор неизвестных: ", x)
    print("Количество итераций: ", iterations)
    print("Погрешность: ", errors[-1] if errors else None)


def is_valid_number(value):
    try:
        float(value)
        return True
    except ValueError:
        return False


def main():
    print("Решение системы линейных алгебраических уравнений СЛАУ")
    while True:
        try:
            print("Введите точность:")
            precision = float(input())
            if precision <= 0:
                raise ValueError("Ошибка: точность должна быть положительным числом.")
            break
        except ValueError:
            print("Ошибка: введите корректное положительное число для точности.")
    print(
        "Введите матрицу в формате 'a11 a12 a13 ... a1n b1; a21 a22 a23 ... a2n b2; ... a3n1 an2 an3 ... ann bn' или введите имя файла с матрицей (.txt или .csv)")
    print("Матрица должна быть квадратной. n <= 20")
    print("Введите имя файла или первую строку матрицы: (Для генерации случайной матрицы введите RANDOM)")
    while True:
        try:
            input_data = input().strip()
            if input_data.endswith('.csv') or input_data.endswith('.txt'):
                matrix = read_matrix_from_file(input_data)
            elif input_data == 'RANDOM':
                matrix = generate_random_matrix()
                print("Сгенерированная матрица:")
                for i in range(len(matrix)):
                    print([float('%.2f' % elem) for elem in matrix[i]])
            else:
                matrix = [[float(num) for num in input_data.split()]]
                if len(matrix[0]) > 21:
                    raise ValueError("Ошибка: размерность матрицы должна быть не больше 20.")
                current_row = 1
                while current_row < len(matrix[0]) - 1:
                    input_data = input().strip()
                    row = input_data.split()
                    if len(row) != len(matrix[0]) or not all(is_valid_number(num) for num in row):
                        print(f"Ошибка: строка должна содержать {len(matrix[0])} вещественных чисел.")
                        continue
                    matrix.append([float(num) for num in row])
                    current_row += 1
            break
        except ValueError as e:
            print(f"Ошибка: {e}. Попробуйте снова.")
    try:
        calculate_matrix(matrix, precision)
    except ValueError as e:
        print(e)


if __name__ == '__main__':
    main()
