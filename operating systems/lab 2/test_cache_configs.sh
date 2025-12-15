#!/bin/bash

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$SCRIPT_DIR"
BUILD_DIR="$PROJECT_ROOT/cache_test_builds"
TEST_DIR="$PROJECT_ROOT/test"
LIB_DIR="$PROJECT_ROOT/lib"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

echo -e "${CYAN}=== Cache Configuration Testing Script ===${NC}\n"

INPUT_FILE="$TEST_DIR/input.dat"
if [ ! -f "$INPUT_FILE" ]; then
    echo -e "${YELLOW}Warning: input.dat not found. Generating test data...${NC}"

    INPUT_GEN="$TEST_DIR/input-generator"
    if [ ! -f "$INPUT_GEN" ]; then
        echo -e "${YELLOW}Building input-generator...${NC}"
        cd "$TEST_DIR"
        if [ -f "input-generator.c" ]; then
            if gcc -o input-generator input-generator.c 2>/dev/null || \
               clang -o input-generator input-generator.c 2>/dev/null; then
                INPUT_GEN="$TEST_DIR/input-generator"
            fi
        fi
    fi
    
    if [ -f "$INPUT_GEN" ]; then
        cd "$TEST_DIR"
        echo -e "${BLUE}Generating input.dat with 1000000 integers...${NC}"
        "$INPUT_GEN" input.dat 1000000
        if [ ! -f "$INPUT_FILE" ]; then
            echo -e "${RED}Failed to generate input.dat${NC}"
            exit 1
        fi
        echo -e "${GREEN}✓ Generated input.dat${NC}\n"
    else
        echo -e "${RED}Error: input-generator not found and could not be built.${NC}"
        echo -e "${YELLOW}Please generate input.dat manually or build input-generator first.${NC}"
        exit 1
    fi
fi

if [ ! -f "$INPUT_FILE" ]; then
    echo -e "${RED}Error: input.dat still not found after generation attempt.${NC}"
    exit 1
fi

INPUT_SIZE=$(stat -f%z "$INPUT_FILE" 2>/dev/null || stat -c%s "$INPUT_FILE" 2>/dev/null || echo "0")
if [ "$INPUT_SIZE" -lt 1000 ]; then
    echo -e "${YELLOW}Warning: input.dat seems too small ($INPUT_SIZE bytes). Regenerating...${NC}"
    cd "$TEST_DIR"
    if [ -f "input-generator" ]; then
        ./input-generator input.dat 1000000
    fi
fi

declare -a CONFIGS=(
    "32:8:Tiny"
    "128:8:Smallest"
    "512:16:Small"
    "2048:64:Medium"
    "4096:128:Large"
)

OUTPUT_DIR="$BUILD_DIR"
mkdir -p "$OUTPUT_DIR"

echo -e "${BLUE}Building test executables with different cache configurations...${NC}\n"

CC="${CC:-gcc}"
if ! command -v "$CC" &> /dev/null; then
    CC="clang"
fi

for config in "${CONFIGS[@]}"; do
    IFS=':' read -r block_size cache_capacity config_name <<< "$config"
    
    echo -e "${YELLOW}Building configuration: $config_name (BLOCK_SIZE=$block_size, CACHE_CAPACITY=$cache_capacity)${NC}"
    
    BUILD_SUBDIR="$OUTPUT_DIR/$(echo "$config_name" | tr '[:upper:]' '[:lower:]')"
    mkdir -p "$BUILD_SUBDIR"

    LIB_OBJ="$BUILD_SUBDIR/vtpc.o"
    if ! "$CC" -c -O2 \
        -DBLOCK_SIZE=$block_size \
        -DCACHE_CAPACITY=$cache_capacity \
        -I"$LIB_DIR" \
        -o "$LIB_OBJ" \
        "$LIB_DIR/vtpc.c" 2>&1; then
        echo -e "${RED}Failed to compile library for $config_name${NC}"
        continue
    fi

    EXECUTABLE="$BUILD_SUBDIR/ema-sort-int"
    if ! "$CC" -O2 \
        -DBLOCK_SIZE=$block_size \
        -DCACHE_CAPACITY=$cache_capacity \
        -I"$LIB_DIR" \
        -o "$EXECUTABLE" \
        "$TEST_DIR/ema-sort-int.c" \
        "$LIB_OBJ" \
        -lpthread 2>&1; then
        echo -e "${RED}Failed to compile executable for $config_name${NC}"
        continue
    fi
    
    if [ -f "$EXECUTABLE" ]; then
        echo -e "${GREEN}✓ Built $config_name${NC}\n"
    else
        echo -e "${RED}✗ Build failed for $config_name${NC}\n"
    fi
done

echo -e "${BLUE}Running tests with different cache configurations...${NC}\n"
echo -e "${CYAN}========================================${NC}\n"

for config in "${CONFIGS[@]}"; do
    IFS=':' read -r block_size cache_capacity config_name <<< "$config"
    
    BUILD_SUBDIR="$OUTPUT_DIR/$(echo "$config_name" | tr '[:upper:]' '[:lower:]')"
    EXECUTABLE="$BUILD_SUBDIR/ema-sort-int"
    
    if [ ! -f "$EXECUTABLE" ]; then
        echo -e "${RED}Executable not found for $config_name${NC}\n"
        continue
    fi
    
    echo -e "${CYAN}=== Configuration: $config_name ===${NC}"
    echo -e "Block Size: ${YELLOW}$block_size${NC} bytes"
    echo -e "Cache Capacity: ${YELLOW}$cache_capacity${NC} pages"
    echo -e "Total Cache Size: ${YELLOW}$((block_size * cache_capacity / 1024))${NC} KB"
    echo ""
    
    CONFIG_LOWER=$(echo "$config_name" | tr '[:upper:]' '[:lower:]')
    OUTPUT_FILE="$OUTPUT_DIR/output_${CONFIG_LOWER}.dat"

    echo -e "${BLUE}Running external sort test...${NC}"

    cd "$TEST_DIR"
    "$EXECUTABLE" "$INPUT_FILE" "$OUTPUT_FILE" 5 2>&1 | tee "$OUTPUT_DIR/log_${CONFIG_LOWER}.txt"
    
    echo ""
    echo -e "${CYAN}----------------------------------------${NC}\n"
done

echo -e "${GREEN}=== Test Summary ===${NC}\n"

echo -e "${CYAN}Configuration Comparison:${NC}\n"
printf "%-15s %-12s %-12s %-15s %-12s %-12s\n" \
    "Config" "Block Size" "Capacity" "Cache Size (KB)" "Avg Time (s)" "Hit Rate (%)"
echo "----------------------------------------------------------------------------------------"

for config in "${CONFIGS[@]}"; do
    IFS=':' read -r block_size cache_capacity config_name <<< "$config"
    CONFIG_LOWER=$(echo "$config_name" | tr '[:upper:]' '[:lower:]')
    LOG_FILE="$OUTPUT_DIR/log_${CONFIG_LOWER}.txt"
    
    if [ ! -f "$LOG_FILE" ]; then
        continue
    fi

    AVG_TIME=$(grep "avg:" "$LOG_FILE" | tail -1 | grep -oE '[0-9]+\.[0-9]+' | tail -1 || echo "N/A")

    HIT_RATE=$(grep "Hit Rate:" "$LOG_FILE" | tail -1 | grep -oE '[0-9]+\.[0-9]+' || echo "N/A")
    
    CACHE_SIZE_KB=$((block_size * cache_capacity / 1024))
    
    printf "%-15s %-12s %-12s %-15s %-12s %-12s\n" \
        "$config_name" \
        "$block_size" \
        "$cache_capacity" \
        "$CACHE_SIZE_KB" \
        "$AVG_TIME" \
        "$HIT_RATE"
done

echo ""
echo -e "${GREEN}All test results saved in: $OUTPUT_DIR${NC}"
echo -e "${CYAN}Log files:${NC}"
for config in "${CONFIGS[@]}"; do
    IFS=':' read -r block_size cache_capacity config_name <<< "$config"
    CONFIG_LOWER=$(echo "$config_name" | tr '[:upper:]' '[:lower:]')
    echo "  - ${CONFIG_LOWER}: $OUTPUT_DIR/log_${CONFIG_LOWER}.txt"
done

