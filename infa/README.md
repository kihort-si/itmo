# Информатика
## **О предмете**
Курс включает в себя информацию о различных системах счисления, автоматизации обработки данных с использованием Python, включая форматы хранения данных и метаданных, а также базовые понятия языков разметки записи, хранении и обработке информации в современных вычислительных машинах, офисных пакетах, их автоматизации и работы с системой стилей, а также освоение TeX для верстки и публикации научных статей.

Продолжительность: 1 семестр.
## **Лабораторные работы**
### [**Лабораторная работа №1**](https://github.com/kihort-si/itmo/tree/main/infa/labs/lab1)
**Перевод чисел между различными системами счисления**
Перевести число "А", заданное в системе счисления "В", в систему счисления "С".

Используются системы 2-СС - 16СС, симетричные СС, факториальная СС, СС Бергмана и Фибоначива СС.

Дополнительно: написать код на любом языке программирования, осуществляющий перевод из одной СС в другую.
### [**Лабораторная работа №2**](https://github.com/kihort-si/itmo/tree/main/infa/labs/lab2)
![Python](https://github.com/kihort-si/itmo/blob/main/common/python.svg)

**Синтез помехоустойчивого кода**
Построить схему декодирования классического кода Хэмминга (7;4) и (15;11).

Дополнительно: написать код на любом языке программирования, в котором на вход подаётся набор из 7 цифр «0» и «1», записанных подряд, анализирует это сообщение на основе классического кода Хэмминга (7,4), а затем выдает правильное сообщение и указывает бит с ошибкой при его наличии.
### [**Лабораторная работа №3**](https://github.com/kihort-si/itmo/tree/main/infa/labs/lab3)
![Python](https://github.com/kihort-si/itmo/blob/main/common/python.svg)

**Регулярные выражения**

Написать код на языке программирования Python, который выведет количество повторений заданного выражения в строке.

Дополнительно:
- С помощью регулярного выражения найти в тексте все слова, в которых две гласные стоят подряд, а после этого слова идёт слово, в котором не больше 3 согласных.
- С помощью регулярного выражения найти все слова, в которых встречаются заданные буквы в заданной последовательности и расстояние (например, через один друг от друга).

### [**Лабораторная работа №4**](https://github.com/kihort-si/itmo/tree/main/infa/labs/lab4)
![Python](https://github.com/kihort-si/itmo/blob/main/common/python.svg)
![XML](https://github.com/kihort-si/itmo/blob/main/common/xml.svg)
![YAML](https://github.com/kihort-si/itmo/blob/main/common/yaml.svg)

**Исследование протоколов, форматов обмена информацией и языков разметки документов**

Написать программу на языке Python, которая бы осуществляла парсинг и конвертацию исходного файла (XML) в новый (YAML) путём простой замены метасимволов исходного формата на метасимволы результирующего формата.

Дополнительно:
- Переписать исходный код, применив готовые библиотеки.
- Переписать исходный код, добавив в него использование регулярных выражений.
- Переписать исходный код таким образом, чтобы для решения задачи использовались формальные грамматики.
- Используя исходную программу из обязательного задания и программы из дополнительных заданий, сравнить стократное время выполнения парсинга и конвертации в цикле.
- Переписать исходную программу, чтобы она осуществляла парсинг и конвертацию исходного файла в любой другой формат (кроме JSON, YAML, XML, HTML): PROTOBUF, TSV, CSV, WML.

### [**Лабораторная работа №5**](https://github.com/kihort-si/itmo/tree/main/infa/labs/lab5)
![Excel](https://github.com/kihort-si/itmo/blob/main/common/excel.svg)
![Python](https://github.com/kihort-si/itmo/blob/main/common/python.svg)

**Работа с электронными таблицами**

По заданному варианту исходных данных получить набор десятичных чисел.

Используя 16-разрядный двоичный формат со знаком, выполнить перевод десятичных чисел X1,…,X12 в двоичную систему счисления, получив их двоичные эквиваленты.

Найти область допустимых значений для данного двоичного формата.

Выполнить сложения двоичных чисел, используя 16-разрядный двоичный формат со знаком. Результаты сложения перевести в десятичную систему счисления.

Расставить 6 флагов состояния для каждой из 7 операций.

Добавить в лист колонтитулы.

Дополнительно:
- Применить условное форматирование к ячейкам, представляющим собой двоичные числа.
- Используя .csv файл с биржевыми данными за сентябрь-декабрь 2018, создать свой .csv файл, в котором должна храниться информация за 4 дня торгов (по одному дню из каждого месяца). В данном файле построить диаграмму типа «Ящик с усами».
- Используя созданный ранее .csv файл построить в Python аналогичную диаграмму «Ящик с усами».

### [**Лабораторная работа №6**](https://github.com/kihort-si/itmo/tree/main/infa/labs/lab6)
![LaTeX](https://github.com/kihort-si/itmo/blob/main/common/latex.svg)

**Работа с системой компьютерной вёрстки TeX**

Сверстать страницу, максимально похожую на выбранную страницу из журнала Квант.

Дополнительно:
- Сверстать титульный лист для отчёта и объединить все файлы в новый.
- Используя пакет MusiXTeX написать не менее 25 первых нот гимна страны, название которой на русском языке начинается с буквы "З".