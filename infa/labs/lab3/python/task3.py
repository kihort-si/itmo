import re

ISU_ID = 366389

print("Вариант:", ISU_ID % 5)
print("---")


def words(test):
    pattern = r'\b([^аеб]*а[^аеб]{2}е[^аеб]{2}б[^аеб]*)\b'
    matches = re.findall(pattern, test, re.IGNORECASE)
    for match in matches:
        words = match.split()
        for word in words:
            if re.match(pattern, word, re.IGNORECASE):
                print(word)
    if matches != []:
        print("---")


words("иконопечатание муравьелюб баскетбол")
words("саблезуб бакенбарды ораниенбаумец")
words("акнефобия камнедобыча камнеотбор")
words("гардероб канделябр лось")
words("амеба усадьба чаесборщик")
