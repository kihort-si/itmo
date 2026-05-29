import math
import matplotlib.pyplot as plt

MTU = 1500
IP_HEADER = 20
ICMP_HEADER = 8
MAX_PAYLOAD_PER_FRAGMENT = MTU - IP_HEADER  # 1480

ping_sizes = [100, 500, 1000, 2000, 4000, 6000, 8000, 10000]

fragments = [
    math.ceil((s + ICMP_HEADER) / MAX_PAYLOAD_PER_FRAGMENT)
    for s in ping_sizes
]

print(f"{'-s (байт)':<12}{'IP-пакет (байт)':<20}{'Фрагментов':<12}")
print("-" * 44)
for s, f in zip(ping_sizes, fragments):
    print(f"{s:<12}{s + ICMP_HEADER + IP_HEADER:<20}{f:<12}")

fig, ax = plt.subplots(figsize=(8, 5))

ax.plot(ping_sizes, fragments, marker="o", linewidth=2,
        markersize=7, color="#1f4e79")

for s, f in zip(ping_sizes, fragments):
    ax.annotate(f"{f}", (s, f), textcoords="offset points",
                xytext=(0, 10), ha="center", fontsize=10)

ax.set_xlabel("Размер пакета (ключ -s), байт", fontsize=12)
ax.set_ylabel("Количество фрагментов", fontsize=12)
ax.set_title("Зависимость числа IP-фрагментов от размера ping-пакета",
             fontsize=13)
ax.grid(True, linestyle="--", alpha=0.6)
ax.set_xticks(ping_sizes)
ax.set_yticks(range(0, max(fragments) + 2))
ax.set_ylim(0, max(fragments) + 1.5)

plt.tight_layout()
plt.savefig("/mnt/user-data/outputs/ping_fragments.png", dpi=150)
print("\nГрафик сохранён в ping_fragments.png")
