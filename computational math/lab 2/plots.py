import numpy as np
import matplotlib.pyplot as plt

def plot_function(f, a, b, x2, step=0.01):
    x_vals = np.arange(a, b, step)
    y_vals = np.array([f(x) for x in x_vals])


    plt.plot(x_vals, y_vals, label="f(x)")
    plt.plot(x2, 0, 'bo')
    plt.axhline(0, color='black',linewidth=1)
    plt.axvline(0, color='black',linewidth=1)
    plt.xlabel("x")
    plt.ylabel("f(x)")
    plt.title("График функции")
    plt.legend()
    plt.grid(True)
    plt.show()

def plot_function_without_points(f, x2, x0):

    if isinstance(x0, np.ndarray):
        x0 = x0[0]

    delta_x = 2
    a = x0 - delta_x
    b = x0 + delta_x

    step = 0.01
    x_vals = np.arange(a, b, step)
    y_vals = np.array([f(x) for x in x_vals])

    plt.plot(x_vals, y_vals, label="f(x)")
    plt.plot(x2, 0, 'bo')
    plt.axhline(0, color='black', linewidth=1)
    plt.axvline(0, color='black', linewidth=1)
    plt.xlabel("x")
    plt.ylabel("f(x)")
    plt.title("График функции")
    plt.legend()
    plt.grid(True)
    plt.show()

def plot_functions_system(f1, f2, x_next, x0, step=0.01):
    if isinstance(x0, (list, np.ndarray)):
        x0 = np.array(x0)

    if isinstance(x_next, (list, np.ndarray)):
        x_next = np.array(x_next)

    delta_x = 4
    a = x0[0] - delta_x
    b = x0[0] + delta_x

    x = x_next[0]
    y = x_next[1]

    x_vals = np.arange(a, b, step)
    y_vals1 = np.array([f1(np.array([x, x])) for x in x_vals])
    y_vals2 = np.array([f2(np.array([x, x])) for x in x_vals])

    plt.plot(x_vals, y_vals1, label="f1(x)", color='blue')
    plt.plot(x_vals, y_vals2, label="f2(x)", color='red')
    plt.plot(x, y, 'bo')
    plt.axhline(0, color='black', linewidth=1)
    plt.axvline(0, color='black', linewidth=1)
    plt.xlabel("x")
    plt.ylabel("f(x)")
    plt.title("Графики функций f1(x) и f2(x)")
    plt.legend()
    plt.grid(True)
    plt.show()