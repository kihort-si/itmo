#include <iostream>
#include <numeric>
#include <sstream>
#include <vector>

using namespace std;

int main() {
  int n;
  int k;
  cin >> n >> k;
  vector<int> coordinates(n);
  vector<int> distances(n - 1);

  for (int i = 0; i < n; i++) {
    cin >> coordinates[i];
    if (i > 0 && i <= n) {
      distances[i - 1] = (coordinates[i] - coordinates[i - 1]);
    }
  }

  int first = 1;
  int last = accumulate(distances.begin(), distances.end(), 0);

  while (first < last) {
    int middle = last - (last - first) / 2;

    int parts = 1;
    int sum = 0;

    for (int d : distances) {
      sum += d;
      if (sum >= middle) {
        parts++;
        sum = 0;
      }
    }

    if (parts >= k) {
      first = middle;
    } else {
      last = middle - 1;
    }
  }

  cout << first << endl;

  return 0;
}
