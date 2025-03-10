#include <iostream>
#include <string>
#include <vector>

using namespace std;

bool isSquared(const string& s) {
  size_t t = s.size();
  if (t % 2 != 0) {
    return false;
  } else {
    for (size_t i = 0; i < t / 2; i++) {
      if (s[i] != s[t / 2 + i]) {
        return false;
      }
    }
  }
  return true;
}

int main() {
  int t;

  do {
    cin >> t;
  } while (t < 1 || t > 100);
  cin.ignore();

  vector<string> strings(t);
  vector<string> result(t, "NO");

  for (int i = 0; i < t; i++) {
    getline(cin, strings[i]);
    if (isSquared(strings[i])) {
      result[i] = "YES";
    }
  }

  for (const string& s : result) {
    cout << s << endl;
  }

  return 0;
}
