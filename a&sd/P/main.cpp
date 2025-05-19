#include <iostream>
#include <vector>

using namespace std;

vector<vector<long long>> fuel;
vector<vector<int>> graph;
vector<bool> visited;
long long fuelLimit;
int n;

void dfs(int node, vector<bool>& visited, bool reverse) {
  visited[node] = true;
  for (int neighbor = 0; neighbor < n; neighbor++) {
    if (!visited[neighbor]) {
      if ((!reverse && fuel[node][neighbor] <= fuelLimit) ||
          (reverse && fuel[neighbor][node] <= fuelLimit)) {
        dfs(neighbor, visited, reverse);
      }
    }
  }
}

bool check(long long maxFuel) {
  fuelLimit = maxFuel;

  vector<bool> visited(n, false);
  dfs(0, visited, false);
  for (int i = 0; i < n; i++) {
    if (!visited[i]) {
      return false;
    }
  }

  visited.assign(n, false);
  dfs(0, visited, true);
  for (int i = 0; i < n; i++) {
    if (!visited[i]) {
      return false;
    }
  }

  return true;
}

int main() {
  cin >> n;
  fuel.assign(n, vector<long long>(n));
  long long maximum = 0;

  for (int i = 0; i < n; i++) {
    for (int j = 0; j < n; j++) {
      cin >> fuel[i][j];
      if (fuel[i][j] > maximum) {
        maximum = fuel[i][j];
      }
    }
  }

  long long left = 0;
  long long right = maximum;
  long long result = maximum;

  while (left <= right) {
    long long mid = (left + right) / 2;

    if (check(mid)) {
      result = mid;
      right = mid - 1;
    } else {
      left = mid + 1;
    }
  }

  cout << result << endl;
  return 0;
}
