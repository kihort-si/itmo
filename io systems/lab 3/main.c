#include <stdio.h>
#include <stdint.h>

typedef int32_t  BMP280_S32_t;
typedef uint32_t BMP280_U32_t;

BMP280_S32_t t_fine;

BMP280_S32_t bmp280_compensate_T_int32(
    BMP280_S32_t adc_T,
    uint16_t dig_T1,
    int16_t dig_T2,
    int16_t dig_T3
) {
    BMP280_S32_t var1, var2, T;

    var1 = ((((adc_T >> 3) - ((BMP280_S32_t)dig_T1 << 1))) *
            ((BMP280_S32_t)dig_T2)) >> 11;

    var2 = (((((adc_T >> 4) - ((BMP280_S32_t)dig_T1)) *
             ((adc_T >> 4) - ((BMP280_S32_t)dig_T1))) >> 12) *
             ((BMP280_S32_t)dig_T3)) >> 14;

    t_fine = var1 + var2;
    T = (t_fine * 5 + 128) >> 8;

    return T;
}

int main(void) {
    uint8_t temp_msb  = 0x82;
    uint8_t temp_lsb  = 0x01;
    uint8_t temp_xlsb = 0x00;

    uint16_t dig_T1 = 0x6D0A;
    int16_t  dig_T2 = (int16_t)0x679B;
    int16_t  dig_T3 = (int16_t)0xFC18;

    BMP280_S32_t adc_T =
        ((BMP280_S32_t)temp_msb << 12) |
        ((BMP280_S32_t)temp_lsb << 4)  |
        ((BMP280_S32_t)temp_xlsb >> 4);

    BMP280_S32_t T = bmp280_compensate_T_int32(adc_T, dig_T1, dig_T2, dig_T3);

    printf("adc_T = 0x%X\n", adc_T);
    printf("Temperature = %.2f C\n", T / 100.0);

    return 0;
}
// 27.07