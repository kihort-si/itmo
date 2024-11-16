/* heap-1.c */

#include <stdbool.h>
#include <stddef.h>
#include <stdio.h>
#include <stdlib.h>

#define HEAP_BLOCKS 16
#define BLOCK_CAPACITY 1024

enum block_status { BLK_FREE = 0, BLK_ONE, BLK_FIRST, BLK_CONT, BLK_LAST };

struct heap {
  struct block {
    char contents[BLOCK_CAPACITY];
  } blocks[HEAP_BLOCKS];
  enum block_status status[HEAP_BLOCKS];
} global_heap = {0};

struct block_id {
  size_t       value;
  bool         valid;
  struct heap* heap;
};

struct block_id block_id_new(size_t value, struct heap* from) {
  return (struct block_id){.valid = true, .value = value, .heap = from};
}
struct block_id block_id_invalid() {
  return (struct block_id){.valid = false};
}

bool block_id_is_valid(struct block_id bid) {
  return bid.valid && bid.value < HEAP_BLOCKS;
}

/* Find block */

bool block_is_free(struct block_id bid) {
  if (!block_id_is_valid(bid))
    return false;
  return bid.heap->status[bid.value] == BLK_FREE;
}

/* Allocate */
struct block_id block_allocate(struct heap* heap, size_t size) {
  if (size == 0 || size > HEAP_BLOCKS) {
    return block_id_invalid();
  }

  for (size_t i = 0; i <= HEAP_BLOCKS - size; i++) {
    bool can_allocate = true;

    for (size_t j = 0; j < size; j++) {
      if (heap->status[i + j] != BLK_FREE) {
        can_allocate = false;
        break;
      }
    }

    if (can_allocate) {
      if (size == 1) {
        heap->status[i] = BLK_ONE;
      } else {
        heap->status[i] = BLK_FIRST;
        for (size_t j = 1; j < size - 1; j++) {
          heap->status[i + j] = BLK_CONT;
        }
        heap->status[i + size - 1] = BLK_LAST;
      }

      return block_id_new(i, heap);
    }
  }

  return block_id_invalid();
}

/* Free */
void block_free(struct block_id b) {
  if (!block_id_is_valid(b) || b.heap->status[b.value] == BLK_FREE) {
    return;
  }

  struct heap* heap = b.heap;
  size_t i = b.value;

  if (heap->status[i] == BLK_ONE) {
    heap->status[i] = BLK_FREE;
  } else if (heap->status[i] == BLK_FIRST) {
    heap->status[i] = BLK_FREE;
    i++;
    while (i < HEAP_BLOCKS && (heap->status[i] == BLK_CONT || heap->status[i] == BLK_LAST)) {
      heap->status[i] = BLK_FREE;
      if (heap->status[i] == BLK_LAST) {
        break;
      }
      i++;
    }
  }
}


/* Printer */
const char* block_repr(struct block_id b) {
  static const char* const repr[] = {[BLK_FREE] = " .",
                                     [BLK_ONE] = " *",
                                     [BLK_FIRST] = "[=",
                                     [BLK_LAST] = "=]",
                                     [BLK_CONT] = " ="};
  if (b.valid)
    return repr[b.heap->status[b.value]];
  else
    return "INVALID";
}

void block_debug_info(struct block_id b, FILE* f) {
  fprintf(f, "%s", block_repr(b));
}

void block_foreach_printer(struct heap* h, size_t count,
                           void printer(struct block_id, FILE* f), FILE* f) {
  for (size_t c = 0; c < count; c++)
    printer(block_id_new(c, h), f);
}

void heap_debug_info(struct heap* h, FILE* f) {
  block_foreach_printer(h, HEAP_BLOCKS, block_debug_info, f);
  fprintf(f, "\n");
}
/*  -------- */

int main() {
  heap_debug_info(&global_heap, stdout);

  struct block_id single_block = block_allocate(&global_heap, 1);
  heap_debug_info(&global_heap, stdout);

  struct block_id sequence_block = block_allocate(&global_heap, 3);
  heap_debug_info(&global_heap, stdout);

  struct block_id invalid_block = block_allocate(&global_heap, HEAP_BLOCKS + 1);
  if (!block_id_is_valid(invalid_block)) {
    printf("Allocation failed as expected.\n");
  }
  heap_debug_info(&global_heap, stdout);

  block_free(single_block);
  heap_debug_info(&global_heap, stdout);

  block_free(sequence_block);
  heap_debug_info(&global_heap, stdout);

  block_free(single_block);
  heap_debug_info(&global_heap, stdout);

  return 0;
}

