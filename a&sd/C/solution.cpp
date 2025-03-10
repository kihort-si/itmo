#include <stdint.h>

#include <iostream>
#include <unordered_map>
#include <vector>

using namespace std;

int main() {
  unordered_map<string, vector<pair<int, int>>> values;
  int currentBlock = 0;
  vector<vector<string>> blocks;
  string input;
  const string openBlock = "{";
  const string closeBlock = "}";

  while (cin >> input) {
    if (input == openBlock) {
      currentBlock++;
      blocks.emplace_back();
    } else if (input == closeBlock) {
      if (!blocks.empty()) {
        for (const string& var : blocks.back()) {
          values[var].pop_back();
          if (values[var].empty()) {
            values.erase(var);
          }
        }
        blocks.pop_back();
      }
      currentBlock = max(0, currentBlock - 1);
      ;
    } else {
      size_t pos = input.find('=');
      pair varVal = {input.substr(0, pos), input.substr(pos + 1)};

      if (isdigit(input[pos + 1]) || (input[pos + 1] == '-' && isdigit(input[pos + 2]))) {
        values[varVal.first].emplace_back(currentBlock, stoi(varVal.second));
        if (!blocks.empty()) {
          blocks.back().push_back(varVal.first);
        }
      } else {
        int val = 0;
        if (values.count(varVal.second) && !values[varVal.second].empty()) {
          val = values[varVal.second].back().second;
        }
        cout << val << endl;
        values[varVal.first].emplace_back(currentBlock, val);

        if (!blocks.empty()) {
          blocks.back().push_back(varVal.first);
        }
      }
    }
  }
  return 0;
}
