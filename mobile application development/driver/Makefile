obj-m := exchange.o
exchange-y := main.o fops.o stock.o order_book.o matching.o events.o price.o init_data.o commands.o

all:
	make -C /lib/modules/$(shell uname -r)/build M=$(PWD) modules
clean:
	make -C /lib/modules/$(shell uname -r)/build M=$(PWD) clean