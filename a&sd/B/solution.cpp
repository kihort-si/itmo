#include <cctype>
#include <iostream>
#include <stack>
#include <vector>

using namespace std;

int main() {
  string input;
  cin >> input;
  int n = input.size();

  if (n % 2 != 0 || n == 0) {
    cout << "Impossible" << endl;
    return 0;
  }

  stack<pair<char, int>> animals;
  stack<pair<char, int>> traps;
  stack<char> chars;
  int animalIndex = 0;
  int trapIndex = 0;
  vector<int> indexes(n / 2, 0);

  for (int i = 0; i < n; i++) {
    char current = input[i];

    if (!isalpha(current)) {
      cout << "Impossible" << endl;
      return 0;
    }

    if (islower(current)) {
      animalIndex++;
      animals.push({current, animalIndex});
    } else {
      trapIndex++;
      traps.push({current, trapIndex});
    }

    if (chars.empty() || current == chars.top()) {
      chars.push(current);
    } else if (!chars.empty() && tolower(current) == tolower(chars.top()) && !traps.empty() &&
               !animals.empty()) {
      indexes[traps.top().second - 1] = animals.top().second;
      animals.pop();
      traps.pop();
      chars.pop();
    } else {
      chars.push(current);
    }
  }

  if (chars.empty()) {
    cout << "Possible" << endl;
    for (int ind : indexes) {
      cout << ind << " ";
    }
    cout << endl;
  } else {
    cout << "Impossible" << endl;
  }

  return 0;
}
