import re


def main():
    input_file_path = 'files/income_task_3.xml'
    output_file_path = 'files/outcome_task_3.yaml'
    process_xml(input_file_path, output_file_path)
    remove_empty_lines(output_file_path)


def process_xml(input_file, output_file):
    xml_name = re.compile(r'<\?xml version="1\.0" encoding="UTF-8"\?>')
    close_tag_xml = re.compile(r'</[^<>\/]+>')
    string = re.compile(r'(?<=>)([^<>\/]+)(?=<)')
    open_quote = re.compile(r'<')
    close_xml = re.compile(r'>')
    repeat_xml = re.compile(r'<пара>')

    yaml_name = ''
    close_tag_yaml = ""
    open_yaml = '"'
    close_yaml = '": '
    repeat_yaml = '-'

    grammar = {
        xml_name: yaml_name,
        repeat_xml: repeat_yaml,
        string: lambda match: f'"{match.group(1)}"',
        close_tag_xml: close_tag_yaml,
        open_quote: open_yaml,
        close_xml: close_yaml,

    }

    with open(input_file, 'r', encoding='utf-8') as infile, open(output_file, 'w', encoding='utf-8') as outfile:
        for line in infile:
            for pattern, replacement in grammar.items():
                line = re.sub(pattern, replacement, line)
            outfile.write(line)


def remove_empty_lines(output_file):
    with open(output_file, 'r', encoding='utf-8') as file:
        strings = file.readlines()

    non_empty = [strin for strin in strings if strin.strip() != '']

    with open(output_file, 'w', encoding='utf-8') as file:
        file.writelines(non_empty)


if __name__ == "__main__":
    main()
