import matplotlib.pyplot as plt
import numpy as np
import calculation


def plot_interpolation_results(xi, yi, x, func_values, func_names):
    line_styles = {
        "Лагранж": {'color': 'blue', 'linestyle': '-', 'linewidth': 2},
        "Ньютон (раздел)": {'color': 'green', 'linestyle': '-', 'linewidth': 2},
        "Ньютон (вперед)": {'color': 'purple', 'linestyle': '-', 'linewidth': 2},
        "Ньютон (назад)": {'color': 'orange', 'linestyle': '-', 'linewidth': 2}
    }

    plt.figure(figsize=(15, 10))
    x_plot = np.linspace(min(xi), max(xi), 500)

    for i, (name, value) in enumerate(zip(func_names, func_values)):
        plt.subplot(2, 2, i + 1)

        if isinstance(value, str):
            plt.text(0.5, 0.5, value, ha='center', va='center', fontsize=10,
                     bbox=dict(facecolor='red', alpha=0.2))
            plt.title(name)
            plt.axis('off')
            continue

        y_plot = []
        valid_points = 0
        for xp in x_plot:
            try:
                if name == "Лагранж":
                    y_val = calculation.lagrange_interpolation(xi, yi, xp)
                elif name == "Ньютон (раздел)":
                    y_val = calculation.newton_divided_differences(xi, yi, xp)
                elif name == "Ньютон (вперед)":
                    y_val = calculation.newton_forward_difference(xi, yi, xp)
                elif name == "Ньютон (назад)":
                    y_val = calculation.newton_backward_difference(xi, yi, xp)

                if y_val is not None:
                    y_plot.append(y_val)
                    valid_points += 1
                else:
                    y_plot.append(np.nan)
            except:
                y_plot.append(np.nan)

        style = line_styles.get(name, {'color': 'black', 'linestyle': '-'})

        if valid_points > 0:
            plt.plot(x_plot, y_plot,
                     label=f'{name}',
                     **style)

            plt.scatter(xi, yi, color='red', s=50, zorder=5,
                        label='Узлы интерполяции', alpha=0.7)

            if isinstance(value, (int, float)):
                plt.scatter([x], [value], color='green', s=100, zorder=5,
                            label=f'f({x:.2f}) = {value:.6f}', marker='x')

            plt.title(f"{name} (n={len(xi)})", fontsize=12)
            plt.grid(True, linestyle=':', alpha=0.7)
            plt.legend(fontsize=9, loc='upper left' if x > np.mean(xi) else 'upper right')
        else:
            plt.text(0.5, 0.5, "Не удалось построить график",
                     ha='center', va='center', fontsize=10,
                     bbox=dict(facecolor='yellow', alpha=0.3))
            plt.title(name, fontsize=12)
            plt.axis('off')

    plt.tight_layout(pad=3.0)
    plt.show()