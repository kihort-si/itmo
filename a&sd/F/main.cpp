#include <algorithm>
#include <iostream>
#include <vector>

using namespace std;

bool comparator(const string& first, const string& second) {
  return first + second > second + first;
}

int main() {
  vector<string> result;
  string num;

  while (cin >> num) {
    result.push_back(num);
  }

  sort(result.begin(), result.end(), comparator);

  for (const auto& n : result) {
    cout << n << "";
  }
  cout << endl;

  return 0;
}
