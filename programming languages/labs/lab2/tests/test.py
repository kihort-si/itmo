import unittest
import subprocess


class Test(unittest.TestCase):
    def run_subprocess(self, input_string):
        process = subprocess.run(
            ['./build/app'],
            input=input_string.encode(),
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE
        )

        return process

    def test_not_found_word(self):
        result = self.run_subprocess("python")
        self.assertEqual(result.stderr.decode(), "Error: not found")
        self.assertIn("", result.stdout.decode())

    def test_too_big_string(self):
        result = self.run_subprocess("A" * 257 + "\n")
        self.assertEqual(result.stderr.decode(), "Error: the string is too big")
        self.assertIn("", result.stdout.decode())

    def test_empty(self):
        result = self.run_subprocess("")
        self.assertEqual(result.stderr.decode(), "Error: not found")
        self.assertEqual(result.stdout.decode(), "")

    def test_found_word(self):
        result = self.run_subprocess("second word")
        self.assertEqual(result.stderr.decode(), "")
        self.assertEqual(result.stdout.decode(), "second word explanation")


if __name__ == "__main__":
    unittest.main()
