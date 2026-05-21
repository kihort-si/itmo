#ifndef EXCHANGE_H
#define EXCHANGE_H

#include <linux/types.h>
#include <linux/mutex.h>
#include <linux/timer.h>
#include <linux/workqueue.h>
#include <linux/cdev.h>

/* ---------- константы ---------- */
#define DEVICE_NAME     "exchange"
#define CLASS_NAME      "exchclass"
#define MAX_STOCKS      128
#define MAX_LEVELS      60
#define TICKER_LEN      8
#define MAX_DEAL_LOG    256
#define MAX_EVENT_LOG   64
#define OUTPUT_BUF_SIZE 16384
#define MAX_ORDER_INPUT 128
#define DAY_INTERVAL    5000
#define MAX_INIT_FILE_SIZE 65536

/* ---------- структуры данных ---------- */
struct order_level {
    int price;
    int volume;
    int is_generated;
};

struct order_book {
    struct order_level bids[MAX_LEVELS];
    struct order_level asks[MAX_LEVELS];
    int bid_count;
    int ask_count;
};

struct trade_record {
    char ticker[TICKER_LEN];
    int price;
    int volume;
    unsigned int event_id;
};

struct event_record {
    unsigned int id;
    char desc[128];
};

struct stock {
    char ticker[TICKER_LEN];
    int last_price;
    struct order_book book;
    unsigned int event_id;
};

struct exchange_state {
    struct stock stocks[MAX_STOCKS];
    int stock_count;
    struct trade_record deals[MAX_DEAL_LOG];
    int deal_head, deal_tail;
    struct event_record events[MAX_EVENT_LOG];
    int event_head, event_tail;
    unsigned int global_event_counter;
    struct timer_list day_timer;
    struct mutex lock;
    /* буфер ответа на последнюю команду */
    char resp_buf[OUTPUT_BUF_SIZE];
    size_t resp_len;
};

struct event_desc {
    const char *name;
    int prob_percent;
    int is_positive;
    int price_move_min;
    int price_move_max;
    int order_size_min;
    int order_size_max;
};

/* ---------- глобальные extern-переменные ---------- */
extern struct exchange_state *state;
extern dev_t dev_num;
extern struct cdev cdev;
extern struct class *dev_class;
extern struct work_struct event_work;
extern char *init_file;

/* ---------- функции ---------- */
/* price.c */
int price_to_str(int kopecks, char *buf, size_t len);
int parse_price(const char *str, int *price_kop);

/* order_book.c */
void insert_bid(struct order_book *book, int price, int volume, int is_generated);
void insert_ask(struct order_book *book, int price, int volume, int is_generated);
int cancel_order(struct stock *s, int is_buy, int price, int volume);
void remove_bid_level(struct order_book *book, int idx);
void remove_ask_level(struct order_book *book, int idx);
void clear_generated_orders(struct order_book *book);

/* matching.c */
int match_order(struct stock *s, int is_buy, int price, int volume,
                unsigned int event_id, int order_is_generated);
void create_spread_orders(struct stock *s, int is_buy, int total_volume,
                          int base_price, unsigned int event_id);

/* events.c */
void event_work_handler(struct work_struct *work);
void day_timer_callback(struct timer_list *t);

/* stock.c */
struct stock *find_or_create_stock(const char *ticker);

/* init_data.c */
void load_init_data(const char *path);

/* commands.c – новые функции для команд */
void generate_book_resp(const char *ticker);
void generate_price_resp(const char *ticker);
void generate_deals_resp(const char *ticker);
void generate_full_resp(void);

/* fops.c */
extern const struct file_operations fops;

#endif /* EXCHANGE_H */