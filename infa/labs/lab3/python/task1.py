import re

ISU_ID = 366389

var_1 = ISU_ID % 6
var_2 = ISU_ID % 4
var_3 = ISU_ID % 7

var = str(var_1) + str(var_2) + str(var_3)
# var: 512
print("Вариант:", var)

smile = "\[<O"
print("Смайлик:", smile[1:])


def count(test):
    match = re.findall(smile, test)
    print(len(match))


count("88[<)POP=<{([<O;:-(8<{")
count("X-|<<{\|/[<O[<O[<O=<OO[<O")
count(";<{---P[</*-P=-{|:<OXXXO[<O=-P")
count("X[<<{O[<O[<O<O<O[[8-O")
count("пог[<O[<Oода")





