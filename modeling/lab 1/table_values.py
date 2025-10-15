import pandas as pd
import numpy as np
import scipy.stats as stats
import matplotlib.pyplot as plt
from math import sqrt
from typing import List, Tuple, Dict


class DataAnalyzer:
    def __init__(self, csv_file: str, column_name: str):
        self.csv_file = csv_file
        self.column_name = column_name
        self.sample_sizes = [10, 20, 50, 100, 200, 300]
        self.confidence_levels = [0.90, 0.95, 0.99]
        self.data = self._load_data()

    def _load_data(self) -> np.ndarray:
        data = pd.read_csv(self.csv_file)

        if self.column_name not in data.columns:
            raise ValueError(f"Column '{self.column_name}' not found in the CSV file.")

        values = data[self.column_name].dropna().values

        if len(values) < max(self.sample_sizes):
            raise ValueError(f"Not enough data points in column '{self.column_name}' for the largest sample size.")

        return values

    def calculate_statistics(self, data: np.ndarray) -> Dict[str, float]:
        return {
            'mean': np.mean(data),
            'std': np.std(data, ddof=1),
            'var': np.var(data, ddof=1),
            'cv': np.std(data, ddof=1) / np.mean(data) if np.mean(data) != 0 else np.nan
        }

    def calculate_confidence_intervals(self, sample: np.ndarray) -> Dict[float, Tuple[float, float]]:
        sample_mean = np.mean(sample)
        sample_std = np.std(sample, ddof=1)
        n = len(sample)

        intervals = {}
        for conf_level in self.confidence_levels:
            alpha = 1 - conf_level
            t_crit = stats.t.ppf(1 - alpha / 2, df=n - 1)
            margin_of_error = t_crit * (sample_std / np.sqrt(n))
            ci_lower = sample_mean - margin_of_error
            ci_upper = sample_mean + margin_of_error
            intervals[conf_level] = (ci_lower, ci_upper)

        return intervals

    def calculate_relative_deviations(self, sample_stats: Dict[str, float],
                                      reference_stats: Dict[str, float]) -> Dict[str, float]:
        deviations = {}
        for key in ['mean', 'std', 'var']:
            if reference_stats[key] != 0:
                deviations[key] = abs(sample_stats[key]) / abs(reference_stats[key]) * 100
            else:
                deviations[key] = np.nan
        return deviations

    def print_sample_analysis(self, n: int, sample_stats: Dict[str, float],
                              confidence_intervals: Dict[float, Tuple[float, float]],
                              relative_deviations: Dict[str, float]):
        print(f"Выборка (n={n}):")
        print(f"  Мат. ожидание = {sample_stats['mean']:.4f}")
        print(f"  Дисперсия = {sample_stats['var']:.4f}")
        print(f"  СКО = {sample_stats['std']:.4f}")

        for conf_level, (ci_lower, ci_upper) in confidence_intervals.items():
            print(f"  Доверительный интервал {conf_level * 100:.0f}%: ({ci_lower:.4f}, {ci_upper:.4f})")

        print(f"  Относительное отклонение от эталона:")
        print(f"    Мат. ожидание: {relative_deviations['mean']:.2f}%")
        print(f"    СКО: {relative_deviations['std']:.2f}%")
        print(f"    Дисперсия: {relative_deviations['var']:.2f}%")
        print()

    def analyze_samples(self):
        reference_stats = self.calculate_statistics(self.data[:max(self.sample_sizes)])

        print(f"\nЭталон (n={max(self.sample_sizes)}):")
        print(f"  Мат. ожидание = {reference_stats['mean']:.4f}")
        print(f"  Дисперсия = {reference_stats['var']:.4f}")
        print(f"  СКО = {reference_stats['std']:.4f}\n")

        for n in self.sample_sizes:
            sample = self.data[:n]
            sample_stats = self.calculate_statistics(sample)
            confidence_intervals = self.calculate_confidence_intervals(sample)
            relative_deviations = self.calculate_relative_deviations(sample_stats, reference_stats)

            self.print_sample_analysis(n, sample_stats, confidence_intervals, relative_deviations)

    def plot_values(self, data: np.ndarray, title: str = 'График значений числовой последовательности'):
        plt.figure(figsize=(10, 6))
        plt.plot(data, marker='o', linestyle='-', color='blue', label='Значения')
        plt.title(title)
        plt.xlabel('Индекс')
        plt.ylabel('Значение')
        plt.grid(True)
        plt.legend()
        plt.tight_layout()
        plt.show()

    def plot_histogram(self, data: np.ndarray, title: str = 'Гистограмма распределения частот'):
        plt.figure(figsize=(10, 6))
        plt.hist(data, bins='auto', color='skyblue', edgecolor='black')
        plt.title(title)
        plt.xlabel('Значение')
        plt.ylabel('Частота')
        plt.grid(True)
        plt.tight_layout()
        plt.show()

    def autocorrelation(self, x: np.ndarray) -> np.ndarray:
        n = len(x)
        mean = np.mean(x)
        var = np.var(x, ddof=1)
        result = np.correlate(x - mean, x - mean, mode='full')
        return result[result.size // 2:] / (var * n)

    def analyze_autocorrelation(self):
        acf_values = self.autocorrelation(self.data)

        print("Коэффициенты автокорреляции (лаги 1–10):")
        for lag in range(1, 11):
            print(f"Lag {lag}: {acf_values[lag]:.4f}")

        plt.figure(figsize=(12, 6))
        plt.stem(range(1, 11), acf_values[1:11])
        plt.title('Автокорреляция (лаги 1–10)')
        plt.xlabel('Лаг')
        plt.ylabel('Коэффициент автокорреляции')
        plt.axhline(y=0, color='gray', linestyle='--')
        plt.grid(True)
        plt.tight_layout()
        plt.show()

        threshold = 2 / np.sqrt(len(self.data))
        significant = any(abs(acf_values[1:11]) > threshold)

        print(f"\nПорог для значимости автокорреляции: ±{threshold:.4f}")

        if significant:
            print("Обнаружена значимая автокорреляция — последовательность не случайна.")
        else:
            print("Значимой автокорреляции не обнаружено — последовательность можно считать случайной.")

    def classify_distribution(self) -> str:
        stats = self.calculate_statistics(self.data)
        cv = stats['cv']

        print(f"Математическое ожидание (μ): {stats['mean']:.4f}")
        print(f"СКО (σ): {stats['std']:.4f}")
        print(f"Коэффициент вариации (CV): {cv:.4f}")

        if cv < 0.3:
            print("\nАппроксимирующее распределение: РАВНОМЕРНОЕ")
            a = stats['mean'] - np.sqrt(3 * stats['var'])
            b = stats['mean'] + np.sqrt(3 * stats['var'])
            print(f"Оценка границ: a = {a:.4f}, b = {b:.4f}")
            return "uniform"

        elif 0.3 <= cv < 1.0:
            print("\nАппроксимирующее распределение: ЭРЛАНГА (или гипоэкспоненциальное)")
            k = int(round(1 / (cv ** 2)))
            lambda_erlang = k / stats['mean']
            print(f"Порядок k = {k}")
            print(f"Интенсивность λ = {lambda_erlang:.4f}")
            return "erlang"

        elif abs(cv - 1.0) < 0.05:
            print("\nАппроксимирующее распределение: ЭКСПОНЕНЦИАЛЬНОЕ")
            lambda_exp = 1 / stats['mean']
            print(f"Интенсивность λ = {lambda_exp:.4f}")
            return "exponential"

        elif cv > 1.0:
            print("\nАппроксимирующее распределение: ГИПЕРЭКСПОНЕНЦИАЛЬНОЕ")
            cv2 = cv ** 2
            if cv2 <= 1:
                print("CV слишком мал для гиперэкспоненциального распределения")
            else:
                print(f"CV^2 = {cv2:.4f} > 1 → необходимо подобрать гиперэкспоненциальную модель")
                print("Для точного моделирования необходимо численное приближение λ1, λ2 и p.")
            return "hyperexponential"

        else:
            print("Не удалось классифицировать распределение по CV.")
            return "unknown"

    def calculate_correlation_manual(self, data1: np.ndarray, data2: np.ndarray) -> Dict[str, float]:
        min_len = min(len(data1), len(data2))
        x = data1[:min_len]
        y = data2[:min_len]

        x_mean = np.mean(x)
        y_mean = np.mean(y)

        numerator = np.sum((x - x_mean) * (y - y_mean))

        sum_x_squared = np.sum((x - x_mean) ** 2)
        sum_y_squared = np.sum((y - y_mean) ** 2)
        denominator = np.sqrt(sum_x_squared * sum_y_squared)

        correlation_manual = numerator / denominator if denominator != 0 else 0

        correlation_numpy = np.corrcoef(x, y)[0, 1]

        return {
            'manual': correlation_manual,
            'numpy': correlation_numpy,
            'sample_size': min_len,
            'x_mean': x_mean,
            'y_mean': y_mean
        }

    def print_correlation_analysis(self, correlation_results: Dict[str, float]):
        print(f"=== КОРРЕЛЯЦИОННЫЙ АНАЛИЗ (n={correlation_results['sample_size']}) ===")
        print(f"Коэффициент корреляции (ручной расчет): {correlation_results['manual']:.6f}")
        print(f"Коэффициент корреляции (NumPy): {correlation_results['numpy']:.6f}")
        print(f"Разность между методами: {abs(correlation_results['manual'] - correlation_results['numpy']):.8f}")

        abs_corr = abs(correlation_results['manual'])
        if abs_corr < 0.1:
            strength = "очень слабая"
        elif abs_corr < 0.3:
            strength = "слабая"
        elif abs_corr < 0.5:
            strength = "умеренная"
        elif abs_corr < 0.7:
            strength = "заметная"
        elif abs_corr < 0.9:
            strength = "сильная"
        else:
            strength = "очень сильная"

        direction = "положительная" if correlation_results['manual'] > 0 else "отрицательная"

        print(f"\nИнтерпретация: {strength} {direction} корреляция")
        print(f"Среднее значение X: {correlation_results['x_mean']:.4f}")
        print(f"Среднее значение Y: {correlation_results['y_mean']:.4f}")
        print()

    def plot_correlation_scatter(self, data1: np.ndarray, data2: np.ndarray,
                                 correlation: float,
                                 title: str = 'Диаграмма рассеяния исходных и сгенерированных данных'):
        min_len = min(len(data1), len(data2))
        x = data1[:min_len]
        y = data2[:min_len]

        plt.figure(figsize=(12, 8))
        plt.scatter(x, y, alpha=0.6, color='steelblue', s=50, edgecolors='white', linewidth=0.5)

        z = np.polyfit(x, y, 1)
        p = np.poly1d(z)
        x_trend = np.linspace(min(x), max(x), 100)
        plt.plot(x_trend, p(x_trend), "red", alpha=0.8, linewidth=2,
                 label=f'Линия тренда: y = {z[0]:.3f}x + {z[1]:.3f}')

        plt.axvline(np.mean(x), color='gray', linestyle='--', alpha=0.7, label=f'Среднее X = {np.mean(x):.3f}')
        plt.axhline(np.mean(y), color='gray', linestyle='--', alpha=0.7, label=f'Среднее Y = {np.mean(y):.3f}')

        plt.xlabel('Исходные данные', fontsize=12)
        plt.ylabel('Сгенерированные данные', fontsize=12)
        plt.title(f'{title}\nКоэффициент корреляции: r = {correlation:.6f}', fontsize=14)
        plt.grid(True, alpha=0.3)
        plt.legend(fontsize=10)
        plt.tight_layout()
        plt.show()


class HyperexponentialGenerator:
    def __init__(self, ref_mean: float, ref_std: float, q: float = 0.2):
        self.ref_mean = ref_mean
        self.ref_std = ref_std
        self.q = q
        self.t1, self.t2 = self._estimate_params()

    def _estimate_params(self) -> Tuple[float, float]:
        var_coeff = self.ref_std / self.ref_mean
        var_term = (var_coeff ** 2 - 1)

        if var_term < 0:
            print("Warning: Invalid value for sqrt, setting default values for t1 and t2.")
            return self.ref_mean, self.ref_mean

        t1 = (1 + sqrt(((1 - self.q) / (2 * self.q)) * var_term)) * self.ref_mean
        t2 = (1 - sqrt((self.q / (2 * (1 - self.q))) * var_term)) * self.ref_mean

        print(f'q = {self.q}\nt1 = {t1}\nt2 = {t2}\n')
        return t1, t2

    def generate_sample(self, n_samples: int = 300) -> List[float]:
        np.random.seed(42)

        choices = np.random.uniform(0, 1, n_samples)
        r_values = np.random.uniform(0, 1, n_samples)

        rates = np.where(choices < self.q, self.t1, self.t2)
        samples = -rates * np.log(1 - r_values)

        return samples.tolist()

    def save_to_file(self, samples: List[float], filename: str = 'hyperexp_params.txt'):
        with open(filename, 'w') as f:
            for sample in samples:
                f.write(f"{sample}\n")


def main():
    analyzer = DataAnalyzer('data.csv', 'values')

    print("=== АНАЛИЗ ИСХОДНЫХ ДАННЫХ ===")
    analyzer.analyze_samples()
    analyzer.plot_values(analyzer.data)
    analyzer.analyze_autocorrelation()
    analyzer.plot_histogram(analyzer.data)
    analyzer.classify_distribution()

    stats = analyzer.calculate_statistics(analyzer.data)
    generator = HyperexponentialGenerator(stats['mean'], stats['std'])
    generated_samples = generator.generate_sample(300)
    generator.save_to_file(generated_samples)

    print("\n=== АНАЛИЗ СГЕНЕРИРОВАННЫХ ДАННЫХ ===")
    generated_data = np.array(generated_samples)

    generated_analyzer = DataAnalyzer.__new__(DataAnalyzer)
    generated_analyzer.sample_sizes = [10, 20, 50, 100, 200, 300]
    generated_analyzer.confidence_levels = [0.90, 0.95, 0.99]
    generated_analyzer.data = generated_data

    generated_analyzer.analyze_samples()
    generated_analyzer.plot_values(generated_data, 'График сгенерированных значений')
    generated_analyzer.plot_histogram(generated_data, 'Гистограмма сгенерированных значений')
    generated_analyzer.analyze_autocorrelation()

    correlation_results = analyzer.calculate_correlation_manual(analyzer.data, generated_data)
    analyzer.print_correlation_analysis(correlation_results)
    analyzer.plot_correlation_scatter(analyzer.data, generated_data, correlation_results['manual'])


if __name__ == "__main__":
    main()
