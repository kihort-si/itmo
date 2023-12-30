#include <iostream>
#include <vector>
#include <algorithm>

std::vector<std::string> compare_strings(const std::vector<std::string>& string_array) {
    std::vector<std::string> result_with_x;

    for (size_t i = 0; i < string_array.size(); ++i) {
        const std::string& input_string = string_array[i];

        for (size_t j = 0; j < string_array.size(); ++j) {
            if (i != j) {
                std::string modified_string = input_string;

                size_t diff_count = 0;
                size_t diff_index = 0;

                for (size_t k = 0; k < input_string.size(); ++k) {
                    if (input_string[k] != string_array[j][k]) {
                        ++diff_count;
                        diff_index = k;
                    }
                }

                if (diff_count == 1 && std::find(result_with_x.begin(), result_with_x.end(), modified_string) == result_with_x.end()) {
                    modified_string[diff_index] = 'X';
                    result_with_x.push_back(modified_string);
                }
            }
        }
    }

    std::sort(result_with_x.begin(), result_with_x.end());
    result_with_x.erase(std::unique(result_with_x.begin(), result_with_x.end()), result_with_x.end());

    return result_with_x;
}

int main() {

//    введите набор аргументов булевой функции

    std::vector<std::string> string_array = {
            "00010", "00011", "00100", "00101", "01011", "01100", "01101", "01110",
            "10000", "10100", "10101", "10110", "10111", "11000", "11001", "11101",
            "11110", "11111"
    };

    std::vector<std::string> result = compare_strings(string_array);

    std::cout << "Result: ";
    for (const std::string& str : result) {
        std::cout << "\"" << str << "\", ";
    }

    return 0;
}
