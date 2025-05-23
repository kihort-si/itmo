import math

from examples import ODU


class Calculation:
    def __init__(self, equation, initial, start, finish, h, accuracy):
        self.equation = equation
        self.initial = initial
        self.start = start
        self.finish = finish
        self.h = h
        self.accuracy = accuracy

    def euler(self):
        x = self.start
        y = self.initial
        results = [(0, x, y, self.equation(x, y))]
        n = int((self.finish - self.start) / self.h)

        for i in range(1, n + 1):
            y += self.h * self.equation(x, y)
            x = self.start + i * self.h
            results.append((i, x, y, self.equation(x, y)))

        return results

    def runge_kutt(self):
        n = int((self.finish - self.start) / self.h)
        x = self.start
        y = self.initial
        k1 = self.h * self.equation(x, y)
        k2 = self.h * self.equation(x + self.h / 2, y + k1 / 2)
        k3 = self.h * self.equation(x + self.h / 2, y + k2 / 2)
        k4 = self.h * self.equation(x + self.h, y + k3)
        results = [(0, x, y, k1, k2, k3, k4)]

        for i in range(1, n + 1):
            k1 = self.h * self.equation(x, y)
            k2 = self.h * self.equation(x + self.h / 2, y + k1 / 2)
            k3 = self.h * self.equation(x + self.h / 2, y + k2 / 2)
            k4 = self.h * self.equation(x + self.h, y + k3)

            results.append((i, x + self.h, y + (k1 + 2 * k2 + 2 * k3 + k4) / 6, k1, k2, k3, k4))
            y += (k1 + 2 * k2 + 2 * k3 + k4) / 6
            x += self.h

        return results

    def adams(self):
        n = int((self.finish - self.start) / self.h)
        x_vals = [self.start + i * self.h for i in range(n + 1)]
        y_vals = [0.0] * (n + 1)

        rk = self.runge_kutt()
        for i in range(4):
            _, xi, yi, *_ = rk[i]
            x_vals[i] = xi
            y_vals[i] = yi

        for i in range(3, n):
            f0 = self.equation(x_vals[i], y_vals[i])
            f1 = self.equation(x_vals[i - 1], y_vals[i - 1])
            f2 = self.equation(x_vals[i - 2], y_vals[i - 2])
            f3 = self.equation(x_vals[i - 3], y_vals[i - 3])

            y_vals[i + 1] = (
                    y_vals[i]
                    + self.h * (55 * f0 - 59 * f1 + 37 * f2 - 9 * f3) / 24
            )

        results = []
        for i in range(n + 1):
            fxy = self.equation(x_vals[i], y_vals[i])
            results.append((i, x_vals[i], y_vals[i], fxy))

        return results

    def exact_solution(self, x):
        if self.equation.__code__.co_code == ODU[0].__code__.co_code:
            return -math.exp(x) / (x * math.exp(x) - (
                    self.start * math.exp(self.start) * self.initial + math.exp(self.start)) / self.initial)
        elif self.equation.__code__.co_code == ODU[1].__code__.co_code:
            return (math.exp(self.start) * self.initial + (-self.start ** 2 + 2 * self.start - 2) * math.exp(
                self.start)) / (math.exp(x)) + x ** 2 - 2 * x + 2
        elif self.equation.__code__.co_code == ODU[2].__code__.co_code:
            return (2 * math.exp(self.start) * self.initial - math.exp(2 * self.start)) / (2 * math.exp(x)) + (
                        math.exp(x) / 2)
        else:
            raise NotImplementedError("Точное решение не определено для этого уравнения.")

    def error_adams_vs_exact(self):
        adams_results = self.adams()
        return max(abs(self.exact_solution(x) - y) for (_, x, y, *_rest) in adams_results)

    def error_runge_rule_euler(self):
        def method(step):
            calc = Calculation(self.equation, self.initial, self.start, self.finish, step, self.accuracy)
            return calc.euler()

        coarse = method(self.h)
        fine = method(self.h / 2)

        errors = [
            abs(y1 - y2)
            for (_, x1, y1, *_), (_, x2, y2, *_) in zip(coarse, fine[::2])
            if abs(x1 - x2) < 1e-8
        ]
        return max(errors) / (2 ** 1 - 1)

    def error_runge_rule_rk4(self):
        def method(step):
            calc = Calculation(self.equation, self.initial, self.start, self.finish, step, self.accuracy)
            return calc.runge_kutt()

        coarse = method(self.h)
        fine = method(self.h / 2)

        errors = [
            abs(y1 - y2)
            for (_, x1, y1, *_), (_, x2, y2, *_) in zip(coarse, fine[::2])
            if abs(x1 - x2) < 1e-8
        ]
        return max(errors) / (2 ** 4 - 1)
