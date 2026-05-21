#include "exchange.h"
#include <linux/kernel.h>
#include <linux/fs.h>
#include <linux/uaccess.h>
#include <linux/slab.h>
#include <linux/string.h>
#include <linux/mutex.h>

static int dev_open(struct inode *i, struct file *f)
{
    pr_info("exchange: устройство открыто\n");
    return 0;
}

static int dev_release(struct inode *i, struct file *f)
{
    pr_info("exchange: устройство закрыто\n");
    return 0;
}

static ssize_t dev_read(struct file *f, char __user *buf, size_t len, loff_t *off)
{
    size_t resp_len;
    if (*off > 0)
        return 0;

    mutex_lock(&state->lock);
    resp_len = state->resp_len;
    if (resp_len == 0) {
        const char *msg = "No command pending. Use BOOK/PRICE/DEALS/ALL or an order.\n";
        resp_len = strlen(msg);
        if (resp_len > len) resp_len = len;
        if (copy_to_user(buf, msg, resp_len)) {
            mutex_unlock(&state->lock);
            return -EFAULT;
        }
        *off += resp_len;   /* один раз показали — больше не читаем */
        mutex_unlock(&state->lock);
        return resp_len;
    }

    if (resp_len > len)
        resp_len = len;
    if (copy_to_user(buf, state->resp_buf, resp_len)) {
        mutex_unlock(&state->lock);
        return -EFAULT;
    }
    state->resp_len = 0;   /* сброс буфера */
    *off += resp_len;
    mutex_unlock(&state->lock);
    return resp_len;
}

static ssize_t dev_write(struct file *f, const char __user *buf, size_t len, loff_t *off)
{
    char kbuf[MAX_ORDER_INPUT];
    char cmd[16], ticker[TICKER_LEN];
    int fields;

    if (len == 0 || len >= MAX_ORDER_INPUT)
        return -EINVAL;
    if (copy_from_user(kbuf, buf, len))
        return -EFAULT;
    kbuf[len] = '\0';

    /* удалить символ новой строки, если есть */
    if (kbuf[len-1] == '\n') kbuf[len-1] = '\0';

    if (sscanf(kbuf, "%15s", cmd) != 1)
        return -EINVAL;

    if (strcmp(cmd, "BOOK") == 0 || strcmp(cmd, "PRICE") == 0 ||
        strcmp(cmd, "DEALS") == 0 || strcmp(cmd, "ALL") == 0) {

        if (strcmp(cmd, "ALL") == 0) {
            mutex_lock(&state->lock);
            generate_full_resp();
            mutex_unlock(&state->lock);
            return len;
        }

        if (sscanf(kbuf, "%15s %7s", cmd, ticker) != 2)
            return -EINVAL;

        mutex_lock(&state->lock);
        if (strcmp(cmd, "BOOK") == 0)
            generate_book_resp(ticker);
        else if (strcmp(cmd, "PRICE") == 0)
            generate_price_resp(ticker);
        else if (strcmp(cmd, "DEALS") == 0)
            generate_deals_resp(ticker);
        mutex_unlock(&state->lock);
        return len;
    }

    /* старый формат: TICKER IS_BUY PRICE VOLUME */
    {
        char price_str[16];
        int type, volume, price;

        if (sscanf(kbuf, "%7s %d %15s %d", ticker, &type, price_str, &volume) == 4) {
            // Проверяем корректность типа
            if (type != 0 && type != 1 && type != 2 && type != 3)
                return -EINVAL;
            if (volume <= 0)
                return -EINVAL;
            if (parse_price(price_str, &price) != 0 || price <= 0)
                return -EINVAL;

            mutex_lock(&state->lock);
            struct stock *s = find_or_create_stock(ticker);
            if (!s) {
                mutex_unlock(&state->lock);
                return -ENOSPC;
            }

            if (type == 2 || type == 3) {
                // Отмена заявки
                int is_buy = (type == 2) ? 1 : 0;  // 2 - bid, 3 - ask
                int canceled = cancel_order(s, is_buy, price, volume);
                if (canceled > 0) {
                    snprintf(state->resp_buf, OUTPUT_BUF_SIZE,
                             "Canceled %d of %s %s at %s\n",
                             canceled, s->ticker,
                             is_buy ? "BID" : "ASK", price_str);
                } else {
                    snprintf(state->resp_buf, OUTPUT_BUF_SIZE,
                             "No %s order at %s for %s\n",
                             is_buy ? "BID" : "ASK", price_str, s->ticker);
                }
                state->resp_len = strlen(state->resp_buf);
            } else {
                // Обычный ордер (0 или 1)
                unsigned int event_id = ++state->global_event_counter;
                match_order(s, type, price, volume, event_id, 0);
                snprintf(state->resp_buf, OUTPUT_BUF_SIZE,
                         "Order executed: %s %s %s %d\n",
                         s->ticker, type ? "BUY" : "SELL", price_str, volume);
                state->resp_len = strlen(state->resp_buf);
            }
            mutex_unlock(&state->lock);
            return len;
        }
    }

    return -EINVAL;
}

const struct file_operations fops = {
    .owner = THIS_MODULE,
    .open = dev_open,
    .release = dev_release,
    .read = dev_read,
    .write = dev_write,
};