#include "exchange.h"
#include <linux/kernel.h>
#include <linux/string.h>
#include <linux/mutex.h>

void generate_book_resp(const char *ticker)
{
    struct stock *s = find_or_create_stock(ticker);
    char *buf = state->resp_buf;
    int pos = 0;

    if (!s) {
        pos = snprintf(buf, OUTPUT_BUF_SIZE, "Ticker %s not found\n", ticker);
    } else {
        pos += snprintf(buf + pos, OUTPUT_BUF_SIZE - pos, "--- %s стакан ---\n", s->ticker);
        pos += snprintf(buf + pos, OUTPUT_BUF_SIZE - pos, "BIDS:\n");
        for (int j = 0; j < s->book.bid_count && j < 10; j++) {
            char pstr[16];
            price_to_str(s->book.bids[j].price, pstr, sizeof(pstr));
            pos += snprintf(buf + pos, OUTPUT_BUF_SIZE - pos, "%s %d%s\n",
                            pstr, s->book.bids[j].volume,
                            s->book.bids[j].is_generated ? " (авто)" : "");
        }
        pos += snprintf(buf + pos, OUTPUT_BUF_SIZE - pos, "ASKS:\n");
        for (int j = 0; j < s->book.ask_count && j < 10; j++) {
            char pstr[16];
            price_to_str(s->book.asks[j].price, pstr, sizeof(pstr));
            pos += snprintf(buf + pos, OUTPUT_BUF_SIZE - pos, "%s %d%s\n",
                            pstr, s->book.asks[j].volume,
                            s->book.asks[j].is_generated ? " (авто)" : "");
        }
    }
    state->resp_len = pos;
}

void generate_price_resp(const char *ticker)
{
    struct stock *s = find_or_create_stock(ticker);
    char *buf = state->resp_buf;
    int pos = 0;

    if (!s) {
        pos = snprintf(buf, OUTPUT_BUF_SIZE, "Ticker %s not found\n", ticker);
    } else {
        char pstr[16];
        price_to_str(s->last_price, pstr, sizeof(pstr));
        pos = snprintf(buf, OUTPUT_BUF_SIZE, "%s: последняя цена %s\n", s->ticker, pstr);
    }
    state->resp_len = pos;
}

void generate_deals_resp(const char *ticker)
{
    char *buf = state->resp_buf;
    int pos = 0;

    struct stock *s = find_or_create_stock(ticker);
    if (!s) {
        pos = snprintf(buf, OUTPUT_BUF_SIZE, "Ticker %s not found\n", ticker);
    } else {
        pos += snprintf(buf + pos, OUTPUT_BUF_SIZE - pos, "--- Сделки по %s ---\n", ticker);
        int i = state->deal_tail;
        int count = 0;
        while (i != state->deal_head) {
            struct trade_record *tr = &state->deals[i];
            if (strncmp(tr->ticker, ticker, TICKER_LEN) == 0) {
                char pstr[16];
                price_to_str(tr->price, pstr, sizeof(pstr));
                pos += snprintf(buf + pos, OUTPUT_BUF_SIZE - pos, "%s %d соб%d\n",
                                pstr, tr->volume, tr->event_id);
                count++;
            }
            i = (i + 1) % MAX_DEAL_LOG;
        }
        if (count == 0) {
            pos += snprintf(buf + pos, OUTPUT_BUF_SIZE - pos, "Нет сделок\n");
        }
    }
    state->resp_len = pos;
}

void generate_full_resp(void)
{
    char *buf = state->resp_buf;
    int pos = 0;
    int i, j;

    for (i = 0; i < state->stock_count; i++) {
        struct stock *s = &state->stocks[i];
        char price_str[16];

        pos += snprintf(buf + pos, OUTPUT_BUF_SIZE - pos, "--- %s последняя цена: ", s->ticker);
        pos += price_to_str(s->last_price, buf + pos, OUTPUT_BUF_SIZE - pos);
        pos += snprintf(buf + pos, OUTPUT_BUF_SIZE - pos, " ---\nЗАЯВКИ НА ПОКУПКУ:\n");
        for (j = 0; j < s->book.bid_count && j < 10; j++) {
            pos += price_to_str(s->book.bids[j].price, price_str, sizeof(price_str));
            pos += snprintf(buf + pos, OUTPUT_BUF_SIZE - pos, "%s %d%s\n",
                            price_str, s->book.bids[j].volume,
                            s->book.bids[j].is_generated ? " (авто)" : "");
        }
        pos += snprintf(buf + pos, OUTPUT_BUF_SIZE - pos, "ЗАЯВКИ НА ПРОДАЖУ:\n");
        for (j = 0; j < s->book.ask_count && j < 10; j++) {
            pos += price_to_str(s->book.asks[j].price, price_str, sizeof(price_str));
            pos += snprintf(buf + pos, OUTPUT_BUF_SIZE - pos, "%s %d%s\n",
                            price_str, s->book.asks[j].volume,
                            s->book.asks[j].is_generated ? " (авто)" : "");
        }
    }

    pos += snprintf(buf + pos, OUTPUT_BUF_SIZE - pos, "--- ПОСЛЕДНИЕ СДЕЛКИ ---\n");
    i = state->deal_tail;
    while (i != state->deal_head) {
        struct trade_record *tr = &state->deals[i];
        char pstr[16];
        price_to_str(tr->price, pstr, sizeof(pstr));
        pos += snprintf(buf + pos, OUTPUT_BUF_SIZE - pos, "%s %s %d соб%d\n",
                        tr->ticker, pstr, tr->volume, tr->event_id);
        i = (i + 1) % MAX_DEAL_LOG;
    }

    pos += snprintf(buf + pos, OUTPUT_BUF_SIZE - pos, "--- СОБЫТИЯ ---\n");
    i = state->event_tail;
    while (i != state->event_head) {
        struct event_record *er = &state->events[i];
        pos += snprintf(buf + pos, OUTPUT_BUF_SIZE - pos, "Соб%d: %s\n", er->id, er->desc);
        i = (i + 1) % MAX_EVENT_LOG;
    }

    state->resp_len = pos;
}