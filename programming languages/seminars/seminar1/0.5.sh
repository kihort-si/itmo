$ grep '^[a]*$' test
$ grep '^[0-9]*$' test
$ grep '^.* 0x[0-9a-f]* .*$' test
$ grep '^.* [A-Za-z]\{3\} .*$' test
$ grep '^$' test
$ grep -v '^[a]*$' test
$ grep -v '^[0-9]*$' test
$ grep -v '^.* 0x[0-9a-f]* .*$' test
$ grep -v '^.* [A-Za-z]\{3\} .*$' test
$ grep -v '^$' test