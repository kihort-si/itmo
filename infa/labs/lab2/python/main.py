def main():
    ent_code = str(input("Введите код Хэмминга: "))
    result = decode_hamming_code(ent_code)
    print(result)


def decode_hamming_code(ent_code):
    # Проверяем, является ли введенный код кодом Хэмминга
    if not is_hamming_code(ent_code):
        return 'Введенный код не является кодом Хэмминга'

    # Создаем словарь для хранения значений битов
    bv = {}
    bv['r1'], bv['r2'], bv['i1'], bv['r3'], bv['i2'], bv['i3'], bv['i4'] = ent_code[:7]

    # Строим синдромы и проверяем наличие ошибок
    s1 = int(bv['r1']) ^ int(bv['i1']) ^ int(bv['i2']) ^ int(bv['i4'])
    s2 = int(bv['r2']) ^ int(bv['i1']) ^ int(bv['i3']) ^ int(bv['i4'])
    s3 = int(bv['r3']) ^ int(bv['i2']) ^ int(bv['i3']) ^ int(bv['i4'])
    syndrome = str(s1) + str(s2) + str(s3)

    if syndrome == '000':
        return 'Ошибок нет'
    else:
        error_position = int(syndrome[::-1], 2)
        return correct_error(ent_code, error_position)


def is_hamming_code(ent_code):
    # Проверяем, является ли введенная строка кодом Хэмминга
    return len(ent_code) == 7 and all(bit in '01' for bit in ent_code)


def correct_error(ent_code, error_position):
    # Исправляем ошибку и выводим правильный код
    error_bit = ent_code[error_position - 1]
    corrected_bit = '0' if error_bit == '1' else '1'
    corrected_code = ent_code[:error_position - 1] + corrected_bit + ent_code[error_position:]

    return f'Ошибка в бите i{error_position}: Правильный код: {corrected_code[2] + corrected_code[4:]}'


if __name__ == '__main__':
    main()
