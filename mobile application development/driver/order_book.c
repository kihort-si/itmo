#include "exchange.h"
#include <linux/kernel.h>
#include <linux/string.h>
#include <linux/minmax.h>

void insert_bid(struct order_book *book, int price, int volume, int is_generated)
{
    int i, j;
    for (i = 0; i < book->bid_count; i++) {
        if (book->bids[i].price == price) {
            book->bids[i].volume += volume;
            return;
        }
    }
    if (book->bid_count >= MAX_LEVELS)
        return;
    for (i = 0; i < book->bid_count; i++) {
        if (price > book->bids[i].price)
            break;
    }
    for (j = book->bid_count; j > i; j--)
        book->bids[j] = book->bids[j-1];
    book->bids[i].price = price;
    book->bids[i].volume = volume;
    book->bids[i].is_generated = is_generated;
    book->bid_count++;
}

void insert_ask(struct order_book *book, int price, int volume, int is_generated)
{
    int i, j;
    for (i = 0; i < book->ask_count; i++) {
        if (book->asks[i].price == price) {
            book->asks[i].volume += volume;
            return;
        }
    }
    if (book->ask_count >= MAX_LEVELS)
        return;
    for (i = 0; i < book->ask_count; i++) {
        if (price < book->asks[i].price)
            break;
    }
    for (j = book->ask_count; j > i; j--)
        book->asks[j] = book->asks[j-1];
    book->asks[i].price = price;
    book->asks[i].volume = volume;
    book->asks[i].is_generated = is_generated;
    book->ask_count++;
}

void remove_bid_level(struct order_book *book, int idx)
{
    memmove(&book->bids[idx], &book->bids[idx+1],
            (book->bid_count - idx - 1) * sizeof(struct order_level));
    book->bid_count--;
}

void remove_ask_level(struct order_book *book, int idx)
{
    memmove(&book->asks[idx], &book->asks[idx+1],
            (book->ask_count - idx - 1) * sizeof(struct order_level));
    book->ask_count--;
}

void clear_generated_orders(struct order_book *book)
{
    int i;
    for (i = 0; i < book->bid_count; ) {
        if (book->bids[i].is_generated) {
            remove_bid_level(book, i);
        } else {
            i++;
        }
    }
    for (i = 0; i < book->ask_count; ) {
        if (book->asks[i].is_generated) {
            remove_ask_level(book, i);
        } else {
            i++;
        }
    }
}

/**
 * @brief Отменяет указанный объём с уровня цены в стакане.
 * @param s       Инструмент
 * @param is_buy  1 для Bid, 0 для Ask
 * @param price   Цена уровня
 * @param volume  Запрашиваемый объём для отмены
 * @return        Фактически отменённый объём (может быть меньше запрошенного)
 */
int cancel_order(struct stock *s, int is_buy, int price, int volume)
{
    struct order_book *book = &s->book;
    int *count = is_buy ? &book->bid_count : &book->ask_count;
    struct order_level *levels = is_buy ? book->bids : book->asks;
    int i;

    for (i = 0; i < *count; i++) {
        if (levels[i].price == price) {
            int removed = min(volume, levels[i].volume);
            levels[i].volume -= removed;
            if (levels[i].volume <= 0) {
                if (is_buy)
                    remove_bid_level(book, i);
                else
                    remove_ask_level(book, i);
            }
            return removed;
        }
    }
    return 0;   /* уровень не найден */
}