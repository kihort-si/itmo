from math import pi
import numpy as np
import matplotlib.pyplot as plt

x = np.linspace(-20, 20, 5000)
y = np.linspace(-20, 20, 5000)
X, Y = np.meshgrid(x, y)
area = X + 1j * Y

basic_area = area[(area.imag >= 0) & (np.abs(area) > 1)]

area_1 = np.log(basic_area)
area_1 = area_1[(area_1.imag >= 0) & (area_1.imag <= pi) & (area_1.real >= 0)]

area_3 = (((1j * area_1) / np.pi) + 1)
area_3 = area_3[(area_3.imag >= 0) & (area_3.real <= 1) & (area_3.real >= 0)]

area_4 = np.conj(2 / area_3)
area_4 = area_4[(((area_4.real - 1)**2 + area_4.imag**2) >= 1) & (area_4.real >= 0) & (area_4.imag >= 0)]

fig, axs = plt.subplots(1, 4, figsize=(25, 5))

axs[0].scatter(basic_area.real, basic_area.imag, color='red', s=1, label='Этап 1: Исходное множество')
axs[0].set_xlim(-2.5, 2.5)
axs[0].set_ylim(-2.5, 2.5)

axs[1].scatter(area_1.real, area_1.imag, color='green', s=1, label='Этап 2:')
axs[1].set_xlim(-3.5, 3.5)
axs[1].set_ylim(-3.5, 3.5)

axs[2].scatter(area_3.real, area_3.imag, color='blue', s=1, label='Этап 3:')
axs[2].set_xlim(-2.5, 2.5)
axs[2].set_ylim(-2.5, 2.5)

axs[3].scatter(area_4.real, area_4.imag, color='purple', s=1, label='Этап 4:')
axs[3].set_xlim(-2.5, 2.5)
axs[3].set_ylim(-2.5, 2.5)

for ax in axs:
    ax.legend()
    ax.grid(True)
    ax.axhline(0, color='black', linewidth=0.5)
    ax.axvline(0, color='black', linewidth=0.5)
    ax.set_aspect('equal')

plt.tight_layout()
plt.show()
