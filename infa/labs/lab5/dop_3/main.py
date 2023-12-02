import pandas as pd
import seaborn as sns
import matplotlib.pyplot as plt

file_path = 'data.csv'
data = pd.read_csv(file_path)

melted_data = pd.melt(data, id_vars=data.columns[0])

sns.set(style="whitegrid")

plt.figure(figsize=(12, 8))
sns.boxplot(x='variable', y='value', data=melted_data)

plt.title('"Ящик с усами"')
plt.xlabel('Названия колонок')
plt.ylabel('Значения')

plt.xticks(rotation=45, ha='right')

plt.show()