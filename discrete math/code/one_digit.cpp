#include <iostream>
#include <bitset>

std::string binary(int a) {
    return std::bitset<8>(a).to_string();
}

std::string dop_code(int a) {
    std::string bin_a = binary(a);
    std::string invert = bin_a;
    for (char &c : invert) {
        c = (c == '0') ? '1' : '0';
    }
    bin_a = invert;
    return bin_a;
}

std::string BCD(int a) {
    std::string bcd = "";
    for (char digit : std::to_string(a)) {
        std::string n = std::bitset<4>(digit - '0').to_string();
        bcd += n;
    }
    bcd = std::bitset<16>(std::bitset<16>(bcd).to_ulong()).to_string();
    return bcd;
}

std::string ASCII(int a) {
    std::string result = "";
    std::string numberString = std::to_string(a);
    if (a < 1000) {
        result += "00110000";
    }
    for (char digit : numberString) {
        std::string binaryString = std::bitset<4>(digit - '0').to_string();
        result += "0011" + binaryString;
    }
    return result;
}

int main() {
    int a;
    std::cout << "Enter number: ";
    std::cin >> a;

    std::cout << "Binary: " << binary(a) << std::endl;
    std::cout << "Dop code: " << dop_code(a) << std::endl;
    std::cout << "BCD: " << BCD(a) << std::endl;
    std::cout << "ASCII: " << ASCII(a) << std::endl;

    return 0;
}
