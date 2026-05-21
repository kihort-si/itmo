#include "exchange.h"
#include <linux/kernel.h>
#include <linux/string.h>
#include <linux/slab.h>
#include <linux/file.h>
#include <linux/namei.h>
#include <linux/uio.h>
#include <linux/kernel_read_file.h>
#include <linux/fs.h>

void load_init_data(const char *path)
{
    struct file *filp;
    char *buf;
    loff_t pos = 0;
    size_t size;
    int ret;

    if (!path || strlen(path) == 0)
        return;

    filp = filp_open(path, O_RDONLY, 0);
    if (IS_ERR(filp)) {
        pr_warn("exchange: не удалось открыть файл инициализации %s, ошибка %ld\n",
                path, PTR_ERR(filp));
        return;
    }

    size = i_size_read(file_inode(filp));
    if (size > MAX_INIT_FILE_SIZE) {
        pr_warn("exchange: файл инициализации слишком большой (%zu > %d)\n", size, MAX_INIT_FILE_SIZE);
        filp_close(filp, NULL);
        return;
    }

    buf = kmalloc(size + 1, GFP_KERNEL);
    if (!buf) {
        filp_close(filp, NULL);
        return;
    }

    ret = kernel_read(filp, buf, size, &pos);
    if (ret < 0) {
        pr_warn("exchange: ошибка чтения файла инициализации\n");
        kfree(buf);
        filp_close(filp, NULL);
        return;
    }
    buf[size] = '\0';
    filp_close(filp, NULL);

    {
        char *line = buf;
        while (*line && state->stock_count < MAX_STOCKS) {
            char ticker[TICKER_LEN];
            char price_str[16];
            int price_kop, bid_vol = 0, ask_vol = 0;
            int fields;

            while (*line == '\n' || *line == '\r' || *line == ' ')
                line++;
            if (*line == '\0') break;

            fields = sscanf(line, "%7s %15s %d %d", ticker, price_str, &bid_vol, &ask_vol);
            if (fields < 2) {
                while (*line && *line != '\n') line++;
                continue;
            }

            if (parse_price(price_str, &price_kop) != 0 || price_kop <= 0) {
                while (*line && *line != '\n') line++;
                continue;
            }

            {
                struct stock *s = find_or_create_stock(ticker);
                if (!s) break;
                s->last_price = price_kop;

                if (fields >= 4) {
                    int bid_price = price_kop - price_kop / 100;
                    int ask_price = price_kop + price_kop / 100;
                    if (bid_price > 0 && bid_vol > 0)
                        insert_bid(&s->book, bid_price, bid_vol, 0);
                    if (ask_price > 0 && ask_vol > 0)
                        insert_ask(&s->book, ask_price, ask_vol, 0);
                }
            }

            while (*line && *line != '\n') line++;
        }
    }

    kfree(buf);
    pr_info("exchange: загружено %d инструментов из %s\n", state->stock_count, path);
}