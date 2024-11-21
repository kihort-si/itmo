import unittest
import subprocess


class Test(unittest.TestCase):
    def run(self, input_string):
        process = subprocess.run(
            ['.src/main'],
            input=input_string.encode(),
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE
        )
        return process

    def test_not_found_word(self):
        result = self.run("python\n")
        self.assertEqual(result.stdout.decode(), "\n")
        self.assertIs("Error: not found", result.stderr.decode())

    def test_too_big_string(self):
        result = self.run("A" * 257 + "\n")
        self.assertEqual(result.stdout.decode(), "\n")
        self.assertIs("Error: the string is too big", result.stderr.decode())

    def test_empty(self):
        result = self.run("\n")
        self.assertEqual(result.stdout.decode(), "\n")
        self.assertIs("Error: Empty string", result.stderr.decode())

    def test_found_word(self):
        result = self.run("second word\n")
        self.assertEqual(result.stderr.decode(), "\n")
        self.assertIs("Find: second word", result.stdout.decode())


if __name__ == "__main__":
    unittest.main
