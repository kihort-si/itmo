import matplotlib.pyplot as plt

def plot_solutions(calc, method_results, title="Сравнение решений"):
    xs = [x for (_, x, _, *_) in method_results]
    ys_approx = [y for (_, _, y, *_) in method_results]
    ys_exact = [calc.exact_solution(x) for x in xs]

    plt.figure(figsize=(10, 6))

    plt.plot(xs, ys_exact, label="Точное решение", color="orange", linestyle="--", linewidth=2)

    plt.scatter(xs, ys_approx, label="Численное решение", color="blue", zorder=5)

    plt.title(title)
    plt.xlabel("x")
    plt.ylabel("y")
    plt.grid(True)
    plt.legend()
    plt.tight_layout()
    plt.show()
