def binary(a):
    return f"{a:08b}"


def dop_code(a):
    bin_a = binary(a)
    invert = ''.join('1' if c == '0' else '0' for c in bin_a)
    return invert


def BCD(a):
    bcd = ''
    for digit in str(a):
        n = f"{int(digit):04b}"
        bcd += n
    bcd = f"{int(bcd, 2):016b}"
    return bcd


def ASCII(a):
    result = ''
    number_string = str(a)
    if a < 1000:
        result += "00110000"
    for digit in number_string:
        binary_string = f"{int(digit):04b}"
        result += "0011" + binary_string
    return result


if __name__ == "__main__":
    a = int(input("Enter number: "))

    print("Binary:", binary(a))
    print("Dop code:", dop_code(a))
    print("BCD:", BCD(a))
    print("ASCII:", ASCII(a))
