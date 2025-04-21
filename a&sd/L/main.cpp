#include <deque>
#include <iostream>
#include <vector>

using namespace std;

int main() {
  int n, k;
  cin >> n >> k;
  vector<int> consistency(n);
  for (int i = 0; i < n; i++) {
    cin >> consistency[i];
  }
  deque<int> q;
  vector<int> result;
  for (int i = 0; i < n; i++) {
    while (!q.empty() && q.front() <= i - k) {
      q.pop_front();
    }
    while (!q.empty() && consistency[q.back()] >= consistency[i]) {
      q.pop_back();
    }
    q.push_back(i);
    if (i >= k - 1) {
      result.push_back(consistency[q.front()]);
    }
  }
  for (int i : result) {
    cout << i << " ";
  }
  cout << endl;
  return 0;
}