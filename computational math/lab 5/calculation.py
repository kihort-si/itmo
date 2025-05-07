def compute_differences(x, y):
    n = len(x)
    table = [[x[i], y[i]] + [None] * (n - 1) for i in range(n)]

    for order in range(1, n):
        for i in range(n - order):
            if order == 1:
                table[i][order + 1] = round(table[i + 1][order] - table[i][order], 4)
            else:
                table[i][order + 1] = round(table[i + 1][order] - table[i][order], 4)

    return table


def lagrange_interpolation(x_values, y_values, x):
    n = len(x_values)
    result = 0.0

    for i in range(n):
        term = y_values[i]
        for j in range(n):
            if j != i:
                term *= (x - x_values[j]) / (x_values[i] - x_values[j])
        result += term

    return result


def newton_divided_differences(x_values, y_values, x):
    n = len(x_values)
    coefficients = y_values.copy()

    for j in range(1, n):
        for i in range(n - 1, j - 1, -1):
            coefficients[i] = (coefficients[i] - coefficients[i - 1]) / (x_values[i] - x_values[i - j])

    return evaluate_newton_divided(x_values, coefficients, x)


def evaluate_newton_divided(x_values, coefficients, x):
    n = len(coefficients)
    result = coefficients[-1]

    for i in range(n - 2, -1, -1):
        result = result * (x - x_values[i]) + coefficients[i]

    return result


def newton_forward_difference(x_values, y_values, x):
    n = len(x_values)
    h = x_values[1] - x_values[0]

    diff_table = [y_values.copy()]
    for i in range(1, n):
        diff_table.append([])
        for j in range(n - i):
            diff_table[i].append(diff_table[i - 1][j + 1] - diff_table[i - 1][j])

    t = (x - x_values[0]) / h

    result = diff_table[0][0]
    product = 1.0
    for i in range(1, n):
        product *= (t - (i - 1)) / i
        result += product * diff_table[i][0]

    return result


def newton_backward_difference(x_values, y_values, x):
    n = len(x_values)
    h = x_values[1] - x_values[0]

    diff_table = [y_values.copy()]
    for i in range(1, n):
        diff_table.append([])
        for j in range(n - i):
            diff_table[i].append(diff_table[i - 1][j + 1] - diff_table[i - 1][j])

    t = (x - x_values[-1]) / h

    result = diff_table[0][-1]
    product = 1.0

    for i in range(1, n):
        product *= (t + (i - 1)) / i
        if n - i - 1 >= 0 and i < len(diff_table) and n - i - 1 < len(diff_table[i]):
            result += product * diff_table[i][n - i - 1]
        else:
            break

    return result
