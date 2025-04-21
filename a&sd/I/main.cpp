#include <deque>
#include <iostream>
#include <queue>
#include <unordered_map>
#include <unordered_set>
#include <vector>

using namespace std;

int main() {
  int n, k, p = 0;
  cin >> n >> k >> p;
  vector<int> carsOrder(p);
  unordered_map<int, deque<int>> cars(p);
  for (int i = 0; i < p; i++) {
    int value;
    cin >> value;
    carsOrder[i] = value;
    cars[value].push_back(i);
  }

  int operations = 0;
  unordered_set<int> active;
  priority_queue<pair<int, int>> pq;

  for (int i = 0; i < p; i++) {
    int currentCar = carsOrder[i];
    cars[currentCar].pop_front();
    if (active.count(currentCar)) {
      int nextUsage = cars[currentCar].empty() ? p + 1 : cars[currentCar].front();
      pq.emplace(nextUsage, currentCar);
      continue;
    }
    operations++;
    if ((int)active.size() >= k) {
      while (!pq.empty()) {
        auto [nextUsage, car] = pq.top();
        pq.pop();
        if (active.find(car) != active.end()) {
          active.erase(car);
          break;
        }
      }
    }
    active.insert(currentCar);
    int nextUsage = cars[currentCar].empty() ? p + 1 : cars[currentCar].front();
    pq.emplace(nextUsage, currentCar);
  }
  cout << operations << endl;
  return 0;
}