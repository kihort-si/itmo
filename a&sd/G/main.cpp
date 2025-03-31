#include <algorithm>
#include <iostream>
#include <queue>
#include <vector>

using namespace std;

int main() {
  string input;
  cin >> input;

  vector<int> weights(26);
  for (int i = 0; i < 26; i++) {
    cin >> weights[i];
  }

  vector<int> reps(26, 0);
  for (char c : input) {
    reps[c - 'a']++;
  }

  vector<pair<int, char>> repeated_letters;
  for (int i = 0; i < 26; i++) {
    if (reps[i] >= 2) {
      repeated_letters.push_back({weights[i], 'a' + i});
    }
  }

  sort(repeated_letters.rbegin(), repeated_letters.rend());

  vector<char> s(input.size(), '?');
  vector<char> middle_chars;
  int left = 0;
  int right = input.size() - 1;
  for (auto [weight, ch] : repeated_letters) {
    int count = reps[ch - 'a'];

    if (count >= 2) {
      if (left <= right)
        s[left++] = ch;
      if (left <= right)
        s[right--] = ch;
      for (int i = 0; i < count - 2; i++) {
        middle_chars.push_back(ch);
      }
    }
    reps[ch - 'a'] = 0;
  }

  queue<int> free_positions;
  for (size_t i = 0; i < s.size(); i++) {
    if (s[i] == '?') {
      free_positions.push(i);
    }
  }

  for (size_t i = 0; i < 26; i++) {
    if (reps[i] == 1 && !free_positions.empty()) {
      s[free_positions.front()] = 'a' + i;
      free_positions.pop();
    }
  }

  for (char ch : middle_chars) {
    if (!free_positions.empty()) {
      s[free_positions.front()] = ch;
      free_positions.pop();
    }
  }

  for (char c : s)
    cout << c;
  cout << endl;

  return 0;
}
