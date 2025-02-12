import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
from sklearn.linear_model import LinearRegression
from sklearn.metrics import r2_score

df = pd.read_csv('data/task5_447502.csv')

y = df['y'].values

alpha = 0.2
ema = np.zeros_like(y, dtype=float)
ema[0] = y[0]

for i in range(1, len(y)):
    ema[i] = alpha * y[i] + (1 - alpha) * ema[i - 1]

print(f"Сглаженное значение для 38-й точки: {ema[37]:.4f}")
print(f"Сглаженное значение для 100-й точки: {ema[99]:.4f}")

# Linear Trend

X = np.arange(1, len(y) + 1).reshape(-1, 1)
model = LinearRegression().fit(X, y)

a = model.coef_[0]
b = model.intercept_

y_pred = model.predict(X)
r2 = r2_score(y, y_pred)

y_101 = a * 101 + b

print(f"Коэффициент a линейного тренда: {a:.4f}")
print(f"Свободный член b линейного тренда: {b:.4f}")
print(f"Коэффициент детерминации R²: {r2:.4f}")
print(f"Прогноз 101-го значения ряда: {y_101:.4f}")

# Plot

plt.figure(figsize=(12, 6))

plt.plot(X, y, label='Исходный ряд', marker='o', linestyle='--')
plt.plot(X, ema, label='Экспоненциальное сглаживание', color='red')
plt.plot(X, y_pred, label='Линейный тренд', color='green', linewidth=2)

plt.xlabel("Время (индексы)")
plt.ylabel("Значения ряда")
plt.title("Экспоненциальное сглаживание и линейный тренд")
plt.legend()
plt.grid(True)
plt.show()