#include <algorithm>
#include <iostream>
#include <vector>

using namespace std;

int main() {
  int n;
  int k;
  cin >> n >> k;

  vector<int> prices(n);
  for (int i = 0; i < n; i++) {
    cin >> prices[i];
  }

  sort(prices.rbegin(), prices.rend());

  int sum = 0;
  for (int i = 0; i < n; i++) {
    if ((i + 1) % k != 0) {
      sum += prices[i];
    }
  }

  cout << sum << endl;
  return 0;
}
