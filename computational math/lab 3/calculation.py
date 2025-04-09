class Calculation:
    def __init__(self, function, lower_limit, upper_limit, accuracy, method):
        self.function = function
        self.lower_limit = lower_limit
        self.upper_limit = upper_limit
        self.accuracy = accuracy
        self.method = method

    def left_rectangle(self, n):
        h = (self.upper_limit - self.lower_limit) / n
        return h * sum(self.function(self.lower_limit + i * h) for i in range(n))

    def right_rectangle(self, n):
        h = (self.upper_limit - self.lower_limit) / n
        return h * sum(self.function(self.lower_limit + (i + 1) * h) for i in range(n))

    def middle_rectangle(self, n):
        h = (self.upper_limit - self.lower_limit) / n
        return h * sum(self.function(self.lower_limit + (i + 0.5) * h) for i in range(n))

    def trapezoid(self, n):
        h = (self.upper_limit - self.lower_limit) / n
        return h * (0.5 * self.function(self.lower_limit) + 0.5 * self.function(self.upper_limit) + sum(
            self.function(self.lower_limit + i * h) for i in range(1, n)))

    def simpson(self, n):
        h = (self.upper_limit - self.lower_limit) / n
        result = self.function(self.lower_limit) + self.function(self.upper_limit)

        odd_sum = sum(self.function(self.lower_limit + i * h) for i in range(1, n, 2))
        result += 4 * odd_sum

        even_sum = sum(self.function(self.lower_limit + i * h) for i in range(2, n - 1, 2))
        result += 2 * even_sum

        return (h / 3) * result

    def runge(self, I1, I2, k):
        return abs(I1 - I2) / (2 ** k - 1)

    def calculate(self):
        n = 2
        max_n = 10000000
        max_iterations = 1000
        I_old = 0
        iteration = 0
        while iteration < max_iterations:
            if self.method == 1:
                I_new = self.left_rectangle(n)
            elif self.method == 2:
                I_new = self.right_rectangle(n)
            elif self.method == 3:
                I_new = self.middle_rectangle(n)
            elif self.method == 4:
                I_new = self.trapezoid(n)
            elif self.method == 5:
                I_new = self.simpson(n)

            if iteration > 0:
                k = 1 if self.method in [1, 2] else 2 if self.method == 3 else 4
                error = self.runge(I_old, I_new, k)
                if error < self.accuracy:
                    return I_new, n

            I_old = I_new
            n *= 2
            iteration += 1

            if n > max_n:
                return None

        return I_old, n
