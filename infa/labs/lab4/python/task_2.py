import re


def main():
    read_file = "files/income.xml"
    write_file = "files/outcome_task_2.yaml"
    base(read_file, write_file)


def base(read_file, write_file):
    with open(read_file, mode='r', encoding='utf-8') as xml:
        text = xml.read()

    regex_1 = re.compile(r'>([^<>\d]+)</')
    update_part_1 = regex_1.sub(r'>"\1"</', text)

    regex_2 = re.compile(r'</\w+>')
    update_part_2 = regex_2.sub(r'', update_part_1)

    regex_3 = re.compile(r'<(\w+)>')
    update_part_3 = regex_3.sub(r'"\1": ', update_part_2)

    regex_4 = re.compile(r'<\?.*\?>')
    update_part_4 = regex_4.sub(r'', update_part_3)

    regex_5 = re.compile(r'(\d+:\d+)')
    update_part_5 = regex_5.sub(r'"\1"', update_part_4)

    regex_6 = re.compile(r': (\d+)\"')
    update_part_6 = regex_6.sub(r': \1', update_part_5)

    regex_7 = re.compile(r'\t\t"\n')
    update_part_7 = regex_7.sub(r'\t\t-\n', update_part_6)

    regex_8 = re.compile(r'\n\t\t"\w+":')

    inside = regex_8.finditer(update_part_7)

    if inside:
        delete = inside.__next__().end()
        update_part_7 = update_part_7[:delete] + regex_8.sub('', update_part_7[delete:])

    line = regex_8.search(update_part_7)
    if line:
        position = line.end()
        new_line = update_part_7[:position] + '\n\t\t-' + update_part_7[position:]

    result = new_line[1:618]

    with open(write_file, mode='w', encoding='utf-8') as yaml:
        yaml.write(result)


if __name__ == "__main__":
    main()
