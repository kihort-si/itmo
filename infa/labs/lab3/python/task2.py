import re

ISU_ID = 366389

print("Вариант:", ISU_ID % 6)
print("---")

vowels = 'АаИиОоУуЫыЭэЕеЁёЮюЯя'
consonants = 'БбВвГгДдЖжЗзЙйКкЛлМмНнПпРрСсТтФфХхЦцЧчШшЩщ'


def text(test):
    pattern_first = fr"\b\w*[{vowels}]+[{vowels}]\w*\b"
    pattern_second = fr"\b^((?![{consonants}].*[{consonants}].*[{consonants}].*[{consonants}]).)*$\b"
    words = test.split()
    for i in range(len(words) - 1):
        current_word = words[i]
        next_word = words[i + 1]
        if re.match(pattern_first, current_word, re.IGNORECASE):
            if re.match(pattern_second, next_word, re.IGNORECASE):
                print(current_word)
    print("---")


text("Солнечное лето наступило, и каждый день становится все ярче, как будто вся природа улыбается.")
text("Маленький кот мяукал на улице, пес радостно лаял без остановки, а птицы пели звонкие песни.")
text("Красочные осенние листья шуршат под ногами, напоминая о приближении холодов.")
text("Мелодия ветра в горах унесла мои заботы, оставив лишь спокойствие и вдохновение.")
text("Сияние полной луны озаряло ночное небо, создавая неповторимую атмосферу магии и загадочности.")
