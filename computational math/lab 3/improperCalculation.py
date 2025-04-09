import math


class ImproperIntegralCalculator:
    def __init__(self, function, lower_limit, upper_limit):
        self.function = function
        self.lower_limit = lower_limit
        self.upper_limit = upper_limit
        self.check_points = 1000
        self.eps = 1e-6
        self.n = math.ceil(self.upper_limit - self.lower_limit) * self.check_points

    def get_breakpoints(self):
        breakpoints = []

        try:
            self.function(self.lower_limit)
        except (ZeroDivisionError, ValueError, Exception):
            breakpoints.append(self.lower_limit)

        try:
            self.function(self.upper_limit)
        except (ZeroDivisionError, ValueError, Exception):
            breakpoints.append(self.upper_limit)

        step = (self.upper_limit - self.lower_limit) / self.n
        for i in range(self.n):
            x = self.lower_limit + i * step
            try:
                self.function(x)
            except (ZeroDivisionError, ValueError, Exception):
                breakpoints.append(x)

        return list(set(breakpoints))

    def try_to_evaluate(self, x):
        try:
            return self.function(x)
        except (ZeroDivisionError, ValueError, Exception):
            return None

    def get_coverage(self):
        threshold = 1e6 - 1
        coverage = True
        for point in self.get_breakpoints():
            a = self.try_to_evaluate(point - self.eps)
            b = self.try_to_evaluate(point + self.eps)
            if a is None or b is None:
                return False
            if abs(a) > threshold or abs(b) > threshold:
                if self.lower_limit < 0 < self.upper_limit:
                    if abs(a) == abs(b):
                        return True
                return False
        return coverage

    def find_limits(self):
        limits = []
        breakpoints = self.get_breakpoints()
        if len(breakpoints) == 1:
            if breakpoints[0] == self.lower_limit:
                limits.append(self.lower_limit + self.eps)
                limits.append(self.upper_limit)
            elif breakpoints[0] == self.upper_limit:
                limits.append(self.lower_limit)
                limits.append(self.upper_limit - self.eps)
            return limits

        if not (self.try_to_evaluate(self.lower_limit) is None or self.try_to_evaluate(
                breakpoints[0] + self.eps) is None):
            limits.append(self.lower_limit)
            limits.append(breakpoints[0] + self.eps)
            return limits

        if not (self.try_to_evaluate(self.upper_limit) is None or self.try_to_evaluate(
                breakpoints[0] - self.eps) is None):
            limits.append(self.upper_limit)
            limits.append(breakpoints[0] - self.eps)
            return limits

        for point in range(len(breakpoints) - 1):
            cur = breakpoints[point]
            next = breakpoints[point + 1]

            if not (self.try_to_evaluate(cur + self.eps) is None or self.try_to_evaluate(next - self.eps) is None):
                limits.append(cur + self.eps)
                limits.append(next - self.eps)
                return limits

        if not breakpoints or self.lower_limit - self.eps in breakpoints or self.upper_limit + self.eps in breakpoints:
            limits.append(self.lower_limit)
            limits.append(self.upper_limit)

        return limits