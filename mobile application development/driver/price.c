#include "exchange.h"
#include <linux/kernel.h>
#include <linux/string.h>
#include <linux/kstrtox.h>

int price_to_str(int kopecks, char *buf, size_t len)
{
    int rub = kopecks / 100;
    int kop = kopecks % 100;
    if (kop < 0) { kop = -kop; rub = -rub; }
    return snprintf(buf, len, "%d.%02d", rub, kop);
}

int parse_price(const char *str, int *price_kop)
{
    int rub = 0, kop = 0;
    const char *dot = strchr(str, '.');

    if (dot) {
        char rub_str[16], kop_str[4] = {0};
        int len_rub = dot - str;
        if (len_rub >= sizeof(rub_str))
            return -EINVAL;
        memcpy(rub_str, str, len_rub);
        rub_str[len_rub] = '\0';
        if (kstrtoint(rub_str, 10, &rub) != 0)
            return -EINVAL;

        const char *kop_start = dot + 1;
        int kop_len = strlen(kop_start);
        if (kop_len > 2)
            kop_len = 2;
        if (kop_len == 1) {
            kop_str[0] = kop_start[0];
            kop_str[1] = '0';
        } else if (kop_len == 2) {
            kop_str[0] = kop_start[0];
            kop_str[1] = kop_start[1];
        } else {
            kop_str[0] = '0';
            kop_str[1] = '0';
        }
        if (kstrtoint(kop_str, 10, &kop) != 0)
            return -EINVAL;

        if (rub < 0)
            kop = -kop;
    } else {
        if (kstrtoint(str, 10, &rub) != 0)
            return -EINVAL;
    }

    *price_kop = rub * 100 + kop;
    return 0;
}