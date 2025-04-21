#include <algorithm>
#include <iostream>
#include <queue>
#include <string>

using namespace std;

int main() {
  int n;
  cin >> n;
  cin.ignore();
  deque<string> left, right;
  vector<string> v(n);
  vector<string> result;

  for (int i = 0; i < n; i++) {
    getline(cin, v[i]);
    v[i].erase(remove(v[i].begin(), v[i].end(), ' '), v[i].end());
  }

  for (const string& s : v) {
    switch (s[0]) {
      case '+': {
        string x = s.substr(1, s.length());
        right.push_back(x);
        break;
      }
      case '*': {
        string x = s.substr(1, s.length());
        right.push_front(x);
        break;
      }
      case '-': {
        result.push_back(left.front());
        left.pop_front();
        break;
      }
      default:
        break;
    }
    while (left.size() < right.size()) {
      left.push_back(right.front());
      right.pop_front();
    }
    while (left.size() > right.size() + 1) {
      right.push_front(left.back());
      left.pop_back();
    }
  }
  for (size_t i = 0; i < result.size(); i++) {
    cout << result[i] << endl;
  }
  return 0;
}