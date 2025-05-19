#include <iostream>
#include <vector>

using namespace std;

int main() {
  int n;
  cin >> n;
  vector<int> keys(n);
  for (int i = 0; i < n; ++i) {
    cin >> keys[i];
    keys[i]--;
  }

  vector<vector<int>> graph(n);
  for (int i = 0; i < n; ++i) {
    graph[i].push_back(keys[i]);
    graph[keys[i]].push_back(i);
  }

  vector<bool> used(n, false);
  int counter = 0;

  for (int i = 0; i < n; ++i) {
    if (!used[i]) {
      counter++;
      vector<int> stack = {i};
      while (!stack.empty()) {
        int v = stack.back();
        stack.pop_back();

        if (used[v])
          continue;
        used[v] = true;

        for (int u : graph[v]) {
          if (!used[u]) {
            stack.push_back(u);
          }
        }
      }
    }
  }

  cout << counter << endl;
  return 0;
}
