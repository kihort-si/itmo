def main():
    read_file = 'files/income.xml'
    write_file = 'files/outcome.yaml'
    base(read_file, write_file)


def base(read_file, write_file):
    with open(read_file, mode='r', encoding='utf-8') as xml, open(write_file, mode='w', encoding='utf-8') as yaml:

        para_cnt = 0
        for line in xml:
            start = line.find("</")
            end = line.find(">", start)
            while start != -1 and end != -1:
                line = line[:start] + line[end + 1:]

                start = line.find("</")
                end = line.find(">", start)
            line = line.replace('<', '"')
            line = line.replace('>', '": ')

            if "пара" in line:
                para_cnt += 1

            if "пара" in line and para_cnt > 1:
                line = line.replace('"пара":', "-")

            if "Лекция" in line:
                line = line.replace("Лекция", '"Лекция"')

            if "Информатика" in line:
                line = line.replace("Информатика", '"Информатика"')

            if "Основы профессиональной деятельности" in line:
                line = line.replace("Основы профессиональной деятельности", '"Основы профессиональной деятельности"')

            if "Бадминтон" in line:
                line = line.replace("Бадминтон", '"Бадминтон"')

            if "Спорт" in line:
                line = line.replace("Спорт", '"Спорт"')

            if "Балакшин Павел Валерьевич" in line:
                line = line.replace("Балакшин Павел Валерьевич", '"Балакшин Павел Валерьевич"')

            if "Клименков Сергей Викторович" in line:
                line = line.replace("Клименков Сергей Викторович", '"Клименков Сергей Викторович"')

            if "Трифонов Владислав Олегович" in line:
                line = line.replace("Трифонов Владислав Олегович", '"Трифонов Владислав Олегович"')

            if "8:20" in line:
                line = line.replace("8:20", '"8:20"')

            if "9:50" in line:
                line = line.replace("9:50", '"9:50"')

            if "10:00" in line:
                line = line.replace("10:00", '"10:00"')

            if "11:30" in line:
                line = line.replace("11:30", '"11:30"')

            if "11:40" in line:
                line = line.replace("11:40", '"11:40"')

            if "13:10" in line:
                line = line.replace("13:10", '"13:10"')

            if '"?xml version="1.0" encoding="UTF-8"?":' not in line:
                yaml.write(line)

    with open(write_file, 'r', encoding='utf-8') as file:
        strings = file.readlines()

    strings.insert(3, "		-" + '\n')
    non_empty = [strin for strin in strings if strin.strip() != '']

    with open(write_file, 'w', encoding='utf-8') as file:
        file.writelines(non_empty)


if __name__ == "__main__":
    main()