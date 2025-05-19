#include <iostream>
#include <vector>

using namespace std;

int main() {
  int n, m;
  cin >> n >> m;
  vector<pair<int, int>> lkshata(m);
  for (int i = 0; i < m; ++i) {
    int u, v;
    cin >> u >> v;
    u--;
    v--;
    lkshata[i] = {u, v};
  }
  vector<vector<int>> graph(n);
  for (const auto& p : lkshata) {
    int u = p.first;
    int v = p.second;
    graph[u].push_back(v);
    graph[v].push_back(u);
  }
  vector<int> ans(n, -1);
  bool flag = true;
  for (int i = 0; i < n && flag; i++) {
    if (ans[i] == -1) {
      vector<int> stack = {i};
      ans[i] = 0;
      while (!stack.empty() && flag) {
        int v = stack.back();
        stack.pop_back();

        for (int u : graph[v]) {
          if (ans[u] == -1) {
            ans[u] = 1 - ans[v];
            stack.push_back(u);
          } else if (ans[u] == ans[v]) {
            flag = false;
            break;
          }
        }
      }
    }
  }
  cout << (flag ? "YES" : "NO") << endl;
  return 0;
}