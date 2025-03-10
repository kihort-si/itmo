#include <cstdint>
#include <iostream>
#include <vector>

using namespace std;

int main() {
  uint64_t n;
  cin >> n;
  vector<int64_t> a(n);
  for (uint64_t i = 0; i < n; i++) {
    cin >> a[i];
  }

  if (n <= 2) {
    cout << "1 " << n << "\n";
    return 0;
  }

  uint64_t first = 0;
  uint64_t last = 1;
  uint64_t curFirst = 0;
  uint64_t length = 2;

  if (a[0] == a[1] && a[1] == a[2]) {
    curFirst = 1;
    length = 2;
    first = 0;
    last = 2;
  } else {
    length = 3;
    last = 3;
  }

  for (uint64_t curLast = 3; curLast < n; ++curLast) {
    if (a[curLast] == a[curLast - 1] && a[curLast] == a[curLast - 2]) {
      curFirst = curLast - 1;
    }

    uint64_t curLength = curLast - curFirst + 1;

    if (curLength > length) {
      length = curLength;
      first = curFirst;
      last = curLast + 1;
    }
  }

  cout << first + 1 << " " << last << "\n";
  return 0;
}
