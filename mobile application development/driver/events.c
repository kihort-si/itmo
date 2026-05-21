#include "exchange.h"
#include <linux/kernel.h>
#include <linux/random.h>
#include <linux/string.h>
#include <linux/timer.h>
#include <linux/workqueue.h>
#include <linux/sched.h>   /* для from_timer */
#include <linux/minmax.h>

static const struct event_desc event_table[] = {
    {"Повышение ключевой ставки (негатив)",     1,  0, -300,  -100,  50,  100},
    {"Снижение ключевой ставки (позитив)",      1,  1,  200,   500,  50,  100},
    {"Торговая война (негатив)",                2,  0, -1500, -500, 100,  200},
    {"Торговое соглашение (позитив)",           2,  1,  500,  1500, 100,  200},
    {"Геополитическая напряжённость (негатив)",  0.5,0, -800,  -300, 100,  150},
    {"Мирное соглашение (позитив)",             0.5,1,  300,   800, 100,  150},
    {"Природная катастрофа (негатив)",          0.3,0, -2000,-1000, 150,  250},
    {"Восстановление после катастрофы (позитив)",0.3,1,  500,  1000,  80,  150},
    {"Крупное банкротство (негатив)",           0.2,0, -2000, -500, 200,  300},
    {"Госпомощь / слияние (позитив)",           0.2,1,  2000, 5000, 100,  200},
    {"Скандал (негатив)",                       1,  0, -3000,-1000, 100,  200},
    {"Судебная победа / запуск продукта (позитив)",1,1, 1000, 3000,  80,  120},
    {"Дефицит поставок (негатив)",              0.5,0, -3000,-1000, 100,  200},
    {"Избыток предложения (позитив)",           0.5,1,  500,  1500, 100,  150},
    {"Пампинг (начало)",                        0.1,1,  5000, 10000,300,  500},
    {"Дамп (слив)",                             0.1,0, -7000,-5000, 300,  500},
    {"Рыночная паника (негатив)",               2,  0, -600,  -300,  50,  100},
    {"Эйфория (позитив)",                       2,  1,  300,   700,  50,  100},
};

static void apply_event(struct stock *s, const struct event_desc *ev)
{
    int move_percent, order_size, base_price;
    unsigned int event_id = ++state->global_event_counter;
    struct event_record *rec;
    int move_rub, move_kop;
    char sign;

    move_percent = ev->price_move_min +
        (get_random_u32() % (ev->price_move_max - ev->price_move_min + 1));
    order_size = ev->order_size_min +
        (get_random_u32() % (ev->order_size_max - ev->order_size_min + 1));

    clear_generated_orders(&s->book);

    {
        int price_move = (s->last_price * move_percent) / 10000;
        s->last_price += price_move;
        if (s->last_price < 1) s->last_price = 1;
    }

    if (ev->is_positive) {
        base_price = s->last_price + s->last_price / 100;
        if (base_price <= 0) base_price = 1;
        create_spread_orders(s, 1, order_size, base_price, event_id);
    } else {
        base_price = s->last_price - s->last_price / 100;
        if (base_price <= 0) base_price = 1;
        create_spread_orders(s, 0, order_size, base_price, event_id);
    }

    s->event_id = event_id;
    rec = &state->events[state->event_head];
    rec->id = event_id;
    if (move_percent < 0) {
        sign = '-';
        move_percent = -move_percent;
    } else {
        sign = '+';
    }
    move_rub = move_percent / 100;
    move_kop = move_percent % 100;
    snprintf(rec->desc, sizeof(rec->desc), "%s по %s: изменение цены %c%d.%02d%%, объём %d",
             ev->name, s->ticker, sign, move_rub, move_kop, order_size);

    state->event_head = (state->event_head + 1) % MAX_EVENT_LOG;
    if (state->event_head == state->event_tail)
        state->event_tail = (state->event_tail + 1) % MAX_EVENT_LOG;
}

void event_work_handler(struct work_struct *work)
{
    int i, e;
    mutex_lock(&state->lock);
    for (i = 0; i < state->stock_count; i++) {
        struct stock *s = &state->stocks[i];
        unsigned int roll = get_random_u32() % 10000;
        int total_prob = 0;

        if (s->last_price == 0) continue;

        for (e = 0; e < ARRAY_SIZE(event_table); e++) {
            total_prob += event_table[e].prob_percent * 100;
            if (roll < total_prob) {
                apply_event(s, &event_table[e]);
                break;
            }
        }
    }
    mutex_unlock(&state->lock);
    mod_timer(&state->day_timer, jiffies + msecs_to_jiffies(DAY_INTERVAL));
}

void day_timer_callback(struct timer_list *t)
{
    struct exchange_state *st = from_timer(st, t, day_timer);
    schedule_work(&event_work);
}