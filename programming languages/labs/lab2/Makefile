ASM=nasm
ASMFLAGS=-f elf64
LD=ld
PYTHON=python3

SRC_DIR=./src
BUILD_DIR=./build
TEST_DIR=./tests

ASM_FILES=$(wildcard $(SRC_DIR)/*.asm)
OBJ_FILES=$(ASM_FILES:$(SRC_DIR)/%.asm=$(BUILD_DIR)/%.o)

all: app clean

$(BUILD_DIR)/%.o: $(SRC_DIR)/%.asm | $(BUILD_DIR)
	$(ASM) $(ASMFLAGS) -o $@ $<

$(BUILD_DIR):
	mkdir -p $(BUILD_DIR)

app: $(OBJ_FILES)
	$(LD) -o $(BUILD_DIR)/app $(OBJ_FILES)

.PHONY: run clean test

run: app
	./build/app

clean:
	rm -f $(BUILD_DIR)/*.o

test: app
	$(PYTHON) $(TEST_DIR)/test.py

rebuild: clean run