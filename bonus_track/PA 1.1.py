import pandas as pd
import numpy as np
import re

file_path = 'data/lawnmower_var_343286.csv'
df = pd.read_csv(file_path)

df.iloc[:, 1] = pd.to_numeric(df.iloc[:, 1], errors='coerce')

values_30_35 = np.median(df[(df.iloc[:, 1] >= 30) & (df.iloc[:, 1] <= 35)].iloc[:, 1].dropna().values)
values_36_40 = round(np.mean(df[(df.iloc[:, 1] >= 36) & (df.iloc[:, 1] <= 40)].iloc[:, 1].dropna().values))
values_41_45 = np.median(df[(df.iloc[:, 1] >= 41) & (df.iloc[:, 1] <= 45)].iloc[:, 1].dropna().values)


if not np.isnan(values_30_35):
    print(f"Медиана для 30-35: {values_30_35}")
else:
    print("Нет данных в диапазоне 30-35")

if not np.isnan(values_36_40):
    print(f"Среднее арифметическое (округленное) для 36-40: {values_36_40}")
else:
    print("Нет данных в диапазоне 36-40")

if not np.isnan(values_41_45):
    print(f"Медиана для 41-45: {values_41_45}")
else:
    print("Нет данных в диапазоне 41-45")

def replace_null_based_on_column7(row):
    col2_value = row.iloc[1]
    col7_text = str(row.iloc[6])

    if pd.isna(col2_value):
        if re.search(r'\bузк(ая|ие|ую|ой)\b', col7_text, re.IGNORECASE):
            return values_30_35
        elif re.search(r'\bсредняя ширина\b|\bширина средняя\b', col7_text, re.IGNORECASE):
            return values_36_40
        elif re.search(r'\bширокая\b.*\bполоса\b|\bширокие\b.*\bполосы\b', col7_text, re.IGNORECASE):
            return values_41_45
    return col2_value

df.iloc[:, 1] = df.apply(replace_null_based_on_column7, axis=1).astype(float)

df.to_csv("data/lawnmower_var_343286_restored.csv", index=False)

df = pd.read_csv("data/lawnmower_var_343286_restored.csv")

df.iloc[:, 2] = pd.to_numeric(df.iloc[:, 2], errors='coerce')

Q1 = df.iloc[:, 2].quantile(0.25)
Q3 = df.iloc[:, 2].quantile(0.75)
IQR = Q3 - Q1

lower_bound = Q1 - 1.5 * IQR
upper_bound = Q3 + 1.5 * IQR

df_filtered = df[(df.iloc[:, 2] >= lower_bound) & (df.iloc[:, 2] <= upper_bound)]

df_cleaned = df_filtered.dropna(subset=[df.columns[2], df.columns[3], df.columns[4], df.columns[5]])

df_cleaned.to_csv("data/lawnmower_var_343286_final.csv", index=False)

df_cleaned.iloc[:, 2] = pd.to_numeric(df_cleaned.iloc[:, 2], errors='coerce')

num_rows = len(df_cleaned)
print(f"Количество оставшихся к рассмотрению газонокосилок: {num_rows}")

mean_value = df_cleaned.iloc[:, 2].mean()
print(f"Среднее арифметическое для параметра Стоимость полученного после обработки набора данных: {mean_value:.2f}")

for i in range(1, 6):
    df_cleaned.iloc[:, i] = pd.to_numeric(df_cleaned.iloc[:, i], errors='coerce')

for i in range(1, 6):
    min_value = df_cleaned.iloc[:, i].min()
    if min_value > 0:
        df_cleaned.loc[:, f"Norm_{i + 1}"] = 1 - np.exp(1 - (df_cleaned.iloc[:, i] / min_value))
    else:
        df_cleaned.loc[:, f"Norm_{i + 1}"] = np.nan

row = df_cleaned[df_cleaned.iloc[:, 0] == "Wolf NAF225"]

if not row.empty:
    print("\nнормированные значения параметров газнокосилки Wolf NAF225:")
    print(row.iloc[:, -5:])
else:
    print("\nСтрока 'Wolf NAF225' не найдена в данных.")

norm_columns = [f"Norm_{i}" for i in range(2, 7)]

df_cleaned["Final_Score"] = (
    3 * df_cleaned[norm_columns[0]] +
    2 * (1 - df_cleaned[norm_columns[1]]) +
    5 * df_cleaned[norm_columns[2]] +
    8 * df_cleaned[norm_columns[3]] +
    3 * df_cleaned[norm_columns[4]]
)

df_cleaned_sorted = df_cleaned.sort_values(by="Final_Score", ascending=False)

print("\nЛучшие газонокосилки:")
print(df_cleaned_sorted.iloc[:3, 0].tolist())