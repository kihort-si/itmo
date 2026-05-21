#include "exchange.h"
#include <linux/kernel.h>
#include <linux/string.h>
#include <linux/minmax.h>

int match_order(struct stock *s, int is_buy, int price, int volume,
                unsigned int event_id, int order_is_generated)
{
    struct order_book *book = &s->book;
    int traded = 0;
    int *side_count = is_buy ? &book->ask_count : &book->bid_count;
    struct order_level *levels = is_buy ? book->asks : book->bids;

    while (volume > 0 && *side_count > 0) {
        int best_price = levels[0].price;
        int fill, trade_price, trade_vol;
        struct trade_record *rec;

        if ((is_buy && price < best_price) || (!is_buy && price > best_price))
            break;

        fill = min(volume, levels[0].volume);
        trade_price = best_price;
        trade_vol = fill;
        volume -= fill;
        levels[0].volume -= fill;
        traded += fill;

        rec = &state->deals[state->deal_head];
        strncpy(rec->ticker, s->ticker, TICKER_LEN-1);
        rec->price = trade_price;
        rec->volume = trade_vol;
        rec->event_id = event_id;
        state->deal_head = (state->deal_head + 1) % MAX_DEAL_LOG;
        if (state->deal_head == state->deal_tail)
            state->deal_tail = (state->deal_tail + 1) % MAX_DEAL_LOG;

        s->last_price = trade_price;

        if (levels[0].volume == 0) {
            if (is_buy)
                remove_ask_level(book, 0);
            else
                remove_bid_level(book, 0);
            levels = is_buy ? book->asks : book->bids;
            side_count = is_buy ? &book->ask_count : &book->bid_count;
        }
    }

    if (volume > 0) {
        if (is_buy)
            insert_bid(book, price, volume, order_is_generated);
        else
            insert_ask(book, price, volume, order_is_generated);
    }

    return traded;
}

void create_spread_orders(struct stock *s, int is_buy, int total_volume,
                          int base_price, unsigned int event_id)
{
    int num_levels = 5;
    int price_step = 100;
    int i, vol_per_level, vol_variation;
    int price;

    vol_per_level = total_volume / num_levels;
    if (vol_per_level <= 0) vol_per_level = 1;

    for (i = 0; i < num_levels; i++) {
        vol_variation = (get_random_u32() % (vol_per_level/5 + 1)) - vol_per_level/10;
        if (vol_variation < 0 && vol_per_level + vol_variation <= 0)
            vol_variation = 0;
        if (is_buy) {
            price = base_price - i * price_step;
            if (price <= 0) price = 1;
            insert_bid(&s->book, price, vol_per_level + vol_variation, 1);
        } else {
            price = base_price + i * price_step;
            insert_ask(&s->book, price, vol_per_level + vol_variation, 1);
        }
    }

    {
        int extra_vol = (get_random_u32() % (total_volume/4 + 1)) + total_volume/4;
        if (is_buy) {
            int aggr_price = base_price + 200;
            if (aggr_price <= 0) aggr_price = 1;
            match_order(s, 1, aggr_price, extra_vol, event_id, 1);
        } else {
            int aggr_price = base_price - 200;
            if (aggr_price <= 0) aggr_price = 1;
            match_order(s, 0, aggr_price, extra_vol, event_id, 1);
        }
    }
}