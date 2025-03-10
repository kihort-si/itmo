#include <cstdint>
#include <iostream>
#include <sstream>
#include <string>

using namespace std;

int main() {
  int64_t a, b, c, d, k = 0;
  cin >> a >> b >> c >> d >> k;
  int64_t last = a;
  for (int day = 1; day <= k; day++) {
    a = a * b;
    a = max(a - c, (int64_t)0);
    a = min(a, d);
    if (a <= 0) {
      a = 0;
      break;
    }
    if (a == last) {
      break;
    }
    last = a;
  }
  cout << a << endl;

  return 0;
}
