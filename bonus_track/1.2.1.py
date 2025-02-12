import pandas as pd
import matplotlib.pyplot as plt

file_path = 'data/var123719.csv'
df = pd.read_csv(file_path, sep=';')

# first part

skill_counts = df['Choice_1'].value_counts()

plt.figure(figsize=(10, 5))
skill_counts.plot(kind='bar')

plt.title('Skill Counts')
plt.xlabel('Skills')
plt.ylabel('Counts')
plt.xticks(rotation=30, ha='right')
plt.grid(axis='y', linestyle='--', alpha=0.7)

plt.show()

print(skill_counts)

# second part

all_choices = df.iloc[:, :9].values.flatten()

all_skill_counts = pd.Series(all_choices).value_counts()

plt.figure(figsize=(10, 10))
plt.pie(all_skill_counts, labels=all_skill_counts.index, autopct='%1.1f%%', startangle=140)

plt.title('All Skill Counts')
plt.axis('equal')
plt.show()

print(all_skill_counts)
