import numpy as np


def compute_pearson_correlation(x, y):
    av_x = np.mean(x)
    av_y = np.mean(y)

    numerator = np.sum((x - av_x) * (y - av_y))
    denominator = np.sqrt(np.sum((x - av_x) ** 2) * np.sum((y - av_y) ** 2))

    return numerator / denominator


def compute_mean_squared_error(x, y, phi):
    x = np.array(x)
    y = np.array(y)

    errors = phi(x) - y
    mse = np.mean(errors ** 2)
    return np.sqrt(mse)


def compute_measure_of_deviation(x, y, phi):
    x = np.array(x)
    y = np.array(y)

    deviations = phi(x) - y
    return np.sum(deviations ** 2)


def compute_coefficient_of_determination(x, y, phi, n):
    x = np.array(x)
    y = np.array(y)

    av_phi = np.sum(phi(x)) / n
    total_variation = np.sum((y - av_phi) ** 2)
    unexplained_variation = np.sum((y - phi(x)) ** 2)

    return 1 - (unexplained_variation / total_variation)