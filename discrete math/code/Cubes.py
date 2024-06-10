def compare_strings(string_array):
    result_with_x = []

    def find_and_replace():
        for i in range(len(string_array)):
            input_string = string_array[i]

            for j in range(len(string_array)):
                if i != j:
                    modified_string = list(input_string)
                    diff_count = 0
                    diff_index = 0

                    for k in range(len(input_string)):
                        if input_string[k] != string_array[j][k]:
                            diff_count += 1
                            diff_index = k

                    if diff_count == 1:
                        modified_string[diff_index] = 'X'
                        modified_string = ''.join(modified_string)
                        if modified_string not in result_with_x:
                            result_with_x.append(modified_string)
                            string_array[i] = modified_string

    prev_result_size = -1
    while prev_result_size != len(result_with_x):
        prev_result_size = len(result_with_x)
        find_and_replace()

    result_with_x = sorted(set(result_with_x))
    return result_with_x


if __name__ == "__main__":
    # введите набор аргументов булевой функции
    string_array = [
        "00010", "00011", "00100", "00101", "01011", "01100", "01101", "01110",
        "10000", "10100", "10101", "10110", "10111", "11000", "11001", "11101",
        "11110", "11111"
    ]

    result = compare_strings(string_array)

    print("Result: ", end="")
    for str in result:
        print(f"\"{str}\", ", end="")
