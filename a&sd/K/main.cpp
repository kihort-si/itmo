#include <iostream>
#include <iterator>
#include <vector>

using namespace std;

struct Block {
  int start, size;
  bool operator<(const Block& other) const {
    return start < other.start;
  }
};

int main() {
  int n, m;
  cin >> n >> m;

  vector<Block> free_blocks = {
      {1, n}
  };
  vector<pair<int, int>> history(m);
  vector<int> result;

  for (int i = 0; i < m; ++i) {
    int req;
    cin >> req;

    if (req > 0) {
      int idx = -1;
      for (size_t j = 0; j < free_blocks.size(); ++j) {
        if (free_blocks[j].size >= req) {
          idx = free_blocks[j].start;
          history[i] = {req, idx};

          if (free_blocks[j].size == req) {
            free_blocks.erase(free_blocks.begin() + j);
          } else {
            free_blocks[j].start += req;
            free_blocks[j].size -= req;
          }
          break;
        }
      }
      result.push_back(idx);
    } else {
      int prev_idx = -req - 1;
      int size = history[prev_idx].first;
      int start = history[prev_idx].second;

      if (start == -1)
        continue;

      Block new_block = {start, size};

      auto it = lower_bound(free_blocks.begin(), free_blocks.end(), new_block);

      if (it != free_blocks.end() && start + size == it->start) {
        new_block.size += it->size;
        free_blocks.erase(it);
      }

      if (it != free_blocks.begin()) {
        auto prev = std::prev(it);
        if (prev->start + prev->size == new_block.start) {
          new_block.start = prev->start;
          new_block.size += prev->size;
          free_blocks.erase(prev);
        }
      }
      free_blocks.insert(lower_bound(free_blocks.begin(), free_blocks.end(), new_block), new_block);
    }
  }

  for (int x : result) {
    cout << x << endl;
  }

  return 0;
}
