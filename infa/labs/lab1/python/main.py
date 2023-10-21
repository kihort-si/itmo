ent = int(input("Введите число в десятеричной СС: "))

# заполняем ряд Фибоначчи до необходимого числа
fib_numbers = [1, 2]
while fib_numbers[-1] < ent:
    i = len(fib_numbers) - 1
    if (fib_numbers[i] + fib_numbers[i - 1]) not in fib_numbers:
        fib_numbers.append(fib_numbers[i] + fib_numbers[i - 1])

if fib_numbers[-1] > ent:
    fib_numbers.remove(fib_numbers[-1])

# находим числа Фибоначчи, из которых получается сумма
n = len(fib_numbers)
combination = []

for i in range(1, 2**n):
    subset = []
    for j in range(n):
        if (i >> j) & 1:
            subset.append(fib_numbers[j])
        combination.append(subset)

# проверяем, чтобы сумма элементов равнялась введённому числу
temp_sum = 0
good_comb = set()
for i in combination:
    temp_sum = sum(i)
    if temp_sum == ent:
        good_comb.add(tuple(i))

good_comb_list = []

for i in good_comb:
    i_list = list(i)
    good_comb_list.append(i_list)

#перевод в бинарное представление
temp_res_array = []

for i in good_comb_list:
    temp_res = ''
    for j in fib_numbers:
        if j in i:
            temp_res += '1'
        else:
            temp_res += '0'
    temp_res_array.append(temp_res[::-1])

for i in temp_res_array:
    if '11' not in i:
        print('Результат:', i)