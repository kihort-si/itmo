#include "exchange.h"
#include <linux/kernel.h>
#include <linux/string.h>
#include <linux/slab.h>

struct stock *find_or_create_stock(const char *ticker)
{
    int i;
    for (i = 0; i < state->stock_count; i++) {
        if (strncmp(state->stocks[i].ticker, ticker, TICKER_LEN) == 0)
            return &state->stocks[i];
    }
    if (state->stock_count >= MAX_STOCKS)
        return NULL;
    i = state->stock_count++;
    strncpy(state->stocks[i].ticker, ticker, TICKER_LEN-1);
    state->stocks[i].last_price = 100000; // 1000.00
    memset(&state->stocks[i].book, 0, sizeof(struct order_book));
    state->stocks[i].event_id = 0;
    return &state->stocks[i];
}