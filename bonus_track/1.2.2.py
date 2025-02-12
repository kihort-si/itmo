import pandas as pd
import numpy as np
import os

file_path = 'data/task4_1_526639.csv'
df = pd.read_csv(file_path, sep=',')

df = df[df['INTERNET'] != 0]
df.to_csv('data/task4_1_526639_filtered.csv', index=False)

df = pd.read_csv('data/task4_1_526639_filtered.csv', sep=',')

for col in df.columns[1:4]:
    min_val = df[col].min()
    df[col] = 1 - np.exp(1 - (df[col] / min_val))

col = df.columns[4]
min_val = df[col].min()
df[col] = 0.2 * (1 - np.exp(1 - (df[col] / min_val)))

col = df.columns[5]
min_val = df[col].min()
df[col] = -0.4 * (1 - np.exp(1 - (df[col] / min_val)))

df['sum'] = df.iloc[:, 1:6].sum(axis=1)

df = df.sort_values(by='sum')

print(df.head(3))

df.to_csv('data/task4_1_526639_transformed.csv', index=False)

os.remove('data/task4_1_526639_filtered.csv')

