#include "exchange.h"
#include <linux/module.h>
#include <linux/kernel.h>
#include <linux/fs.h>
#include <linux/device.h>
#include <linux/slab.h>
#include <linux/timer.h>
#include <linux/workqueue.h>
#include <linux/mutex.h>
#include <linux/string.h>
#include <linux/sched.h>

struct exchange_state *state;
dev_t dev_num;
struct cdev cdev;
struct class *dev_class;
struct work_struct event_work;
char *init_file;
module_param(init_file, charp, 0400);
MODULE_PARM_DESC(init_file, "Путь к файлу с начальными данными (ТИКЕР ЦЕНА [ОБЪЁМ_BID ОБЪЁМ_ASK])");

static int __init exchange_init(void)
{
    int ret;

    state = kzalloc(sizeof(*state), GFP_KERNEL);
    if (!state)
        return -ENOMEM;
    mutex_init(&state->lock);
    state->resp_len = 0;   /* буфер ответа пуст */

    ret = alloc_chrdev_region(&dev_num, 0, 1, DEVICE_NAME);
    if (ret < 0) {
        pr_err("exchange: ошибка выделения номера устройства\n");
        goto err_free;
    }

    cdev_init(&cdev, &fops);
    ret = cdev_add(&cdev, dev_num, 1);
    if (ret < 0) {
        unregister_chrdev_region(dev_num, 1);
        pr_err("exchange: ошибка добавления cdev\n");
        goto err_free;
    }

    dev_class = class_create(CLASS_NAME);
    if (IS_ERR(dev_class)) {
        cdev_del(&cdev);
        unregister_chrdev_region(dev_num, 1);
        ret = PTR_ERR(dev_class);
        pr_err("exchange: ошибка создания класса\n");
        goto err_free;
    }

    if (device_create(dev_class, NULL, dev_num, NULL, DEVICE_NAME) == NULL) {
        class_destroy(dev_class);
        cdev_del(&cdev);
        unregister_chrdev_region(dev_num, 1);
        ret = -ENOMEM;
        pr_err("exchange: ошибка создания устройства\n");
        goto err_free;
    }

    if (init_file && strlen(init_file) > 0)
        load_init_data(init_file);

    INIT_WORK(&event_work, event_work_handler);
    timer_setup(&state->day_timer, day_timer_callback, 0);
    mod_timer(&state->day_timer, jiffies + msecs_to_jiffies(DAY_INTERVAL));

    pr_info("exchange: модуль загружен, /dev/%s готов\n", DEVICE_NAME);
    return 0;

err_free:
    kfree(state);
    return ret;
}

static void __exit exchange_exit(void)
{
    del_timer_sync(&state->day_timer);
    flush_work(&event_work);
    device_destroy(dev_class, dev_num);
    class_destroy(dev_class);
    cdev_del(&cdev);
    unregister_chrdev_region(dev_num, 1);
    kfree(state);
    pr_info("exchange: модуль выгружен\n");
}

module_init(exchange_init);
module_exit(exchange_exit);

MODULE_LICENSE("GPL");
MODULE_AUTHOR("Драйвер виртуальной биржи");
MODULE_DESCRIPTION("Виртуальная биржа со стаканом, генератором событий и загрузкой из файла");