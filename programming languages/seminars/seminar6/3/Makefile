CC = gcc
CFLAGS = -Wall -Wextra -std=c11 -O2
TARGET = main

all: $(TARGET)

$(TARGET): bad.c vector.c
	$(CC) $(CFLAGS) -o $@ $^

clean:
	rm -f $(TARGET)
