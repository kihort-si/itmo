JAVAC = javac
JFLAGS = -d bin -sourcepath src
SRC_DIR = src
BIN_DIR = bin
MAIN_CLASS = Main

SOURCES := $(shell find $(SRC_DIR) -name "*.java")
CLASSES := $(patsubst $(SRC_DIR)/%.java,$(BIN_DIR)/%.class,$(SOURCES))

all: $(CLASSES)

$(BIN_DIR)/%.class: $(SRC_DIR)/%.java
	@mkdir -p $(BIN_DIR)
	$(JAVAC) $(JFLAGS) $<

run: all
	java -cp $(BIN_DIR) $(MAIN_CLASS)

clean:
	rm -rf $(BIN_DIR)

.PHONY: all clean run