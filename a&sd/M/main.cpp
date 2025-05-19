#include <climits>
#include <iostream>
#include <queue>
#include <vector>

using namespace std;

struct Node {
  int cost;
  int x;
  int y;

  bool operator>(const Node& other) const {
    return cost > other.cost;
  }
};

int main() {
  int n, m, x, y, x_cell, y_cell;
  cin >> n >> m >> x >> y >> x_cell >> y_cell;
  x--;
  y--;
  x_cell--;
  y_cell--;

  vector<vector<char>> grid(n, vector<char>(m));
  for (int i = 0; i < n; i++) {
    for (int j = 0; j < m; j++) {
      cin >> grid[i][j];
    }
  }

  int dx[4] = {-1, 0, 1, 0};
  int dy[4] = {0, 1, 0, -1};
  char directions[4] = {'N', 'E', 'S', 'W'};

  vector<vector<int>> distances(n, vector<int>(m, INT_MAX));
  vector<vector<char>> from_dir(n, vector<char>(m, 0));
  vector<vector<pair<int, int>>> parent(n, vector<pair<int, int>>(m, {-1, -1}));

  priority_queue<Node, vector<Node>, greater<Node>> pq;
  distances[x][y] = 0;
  pq.push({0, x, y});

  while (!pq.empty()) {
    Node current = pq.top();
    pq.pop();

    if (current.cost > distances[current.x][current.y])
      continue;

    for (int d = 0; d < 4; d++) {
      int nx = current.x + dx[d];
      int ny = current.y + dy[d];

      if (nx < 0 || ny < 0 || nx >= n || ny >= m) {
        continue;
      }
      if (grid[nx][ny] == '#') {
        continue;
      }

      int step_cost = (grid[nx][ny] == '.') ? 1 : 2;
      int new_cost = current.cost + step_cost;

      if (new_cost < distances[nx][ny]) {
        distances[nx][ny] = new_cost;
        pq.push({new_cost, nx, ny});
        from_dir[nx][ny] = directions[d];
        parent[nx][ny] = {current.x, current.y};
      }
    }
  }

  if (distances[x_cell][y_cell] == INT_MAX) {
    cout << -1 << endl;
  } else {
    cout << distances[x_cell][y_cell] << endl;

    vector<char> path;
    int cx = x_cell, cy = y_cell;
    while (cx != x || cy != y) {
      char d = from_dir[cx][cy];
      path.push_back(d);
      tie(cx, cy) = parent[cx][cy];
    }

    for (int i = path.size() - 1; i >= 0; i--) {
      cout << path[i];
    }
    cout << endl;
  }

  return 0;
}
