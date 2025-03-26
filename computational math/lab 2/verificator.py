def check_root(f, a, b):
    if f(a) * f(b) > 0:
        return False
    return True