from __future__ import annotations

import argparse
import os
import re
from pathlib import Path

_mpl_dir = Path(__file__).resolve().parent / ".mplconfig"
_mpl_dir.mkdir(parents=True, exist_ok=True)
os.environ.setdefault("MPLCONFIGDIR", str(_mpl_dir))

import matplotlib.dates as mdates
import matplotlib.pyplot as plt
import matplotlib.ticker as mticker
import numpy as np
import pandas as pd


SLA_MS = 470


def _read_jtl_csv(path: Path) -> pd.DataFrame:
    df = pd.read_csv(path, dtype={"timeStamp": "int64", "elapsed": "int64"})
    if "timeStamp" not in df.columns or "elapsed" not in df.columns:
        raise ValueError(f"Файл {path} не похож на JMeter CSV (нет timeStamp / elapsed).")
    df["t"] = pd.to_datetime(df["timeStamp"], unit="ms", utc=True).dt.tz_convert(None)
    if "success" in df.columns:
        df["ok"] = df["success"].astype(str).str.lower().eq("true")
    else:
        df["ok"] = True
    if "responseCode" in df.columns:
        df["http_ok"] = df["responseCode"].astype(str).eq("200")
    else:
        df["http_ok"] = True
    if "allThreads" in df.columns:
        threads = pd.to_numeric(df["allThreads"], errors="coerce")
    elif "grpThreads" in df.columns:
        threads = pd.to_numeric(df["grpThreads"], errors="coerce")
    else:
        threads = pd.Series(1, index=df.index)
    df["allThreads"] = threads.fillna(1).clip(lower=1).astype(int)
    return df


def _config_from_url_row(row: pd.Series) -> int | None:
    if "URL" not in row.index:
        return None
    m = re.search(r"config=(\d+)", str(row["URL"]))
    return int(m.group(1)) if m else None


def discover_load_files(results_dir: Path) -> list[tuple[int, Path]]:
    out: list[tuple[int, Path]] = []
    for p in sorted(results_dir.glob("load-config-*.csv")):
        if "stress" in p.name.lower():
            continue
        m = re.match(r"load-config-(\d+)\.csv$", p.name, re.I)
        if m:
            out.append((int(m.group(1)), p))
    return out


def discover_stress_file(results_dir: Path) -> Path | None:
    preferred = results_dir / "load-config-stress.csv"
    if preferred.is_file():
        return preferred
    candidates = sorted(
        {p for p in results_dir.glob("*stress*.csv") if p.is_file()}
        | {p for p in results_dir.glob("*Stress*.csv") if p.is_file()},
        key=lambda p: p.stat().st_mtime,
        reverse=True,
    )
    for p in candidates:
        try:
            with p.open(encoding="utf-8", errors="replace") as f:
                line = f.readline()
        except OSError:
            continue
        if "timeStamp" in line and "elapsed" in line:
            return p
    return None


def latency_series(df: pd.DataFrame, bin_s: float) -> tuple[pd.Series, pd.Series]:
    """По временным корзинам bin_s: среднее и P95 elapsed (мс)."""
    d = df.copy()
    d["bucket"] = d["t"].dt.floor(f"{bin_s}s")
    g = d.groupby("bucket", sort=True)["elapsed"]
    mean_ms = g.mean()
    p95_ms = g.quantile(0.95)
    return mean_ms, p95_ms


def sla_ok_series(df: pd.DataFrame, bin_s: float) -> pd.Series:
    """Доля запросов с elapsed <= SLA_MS в каждой корзине времени (0..1)."""
    d = df.copy()
    d["bucket"] = d["t"].dt.floor(f"{bin_s}s")
    d["sla_ok"] = d["elapsed"] <= SLA_MS
    return d.groupby("bucket", sort=True)["sla_ok"].mean()


def throughput_series(df: pd.DataFrame, bin_s: float) -> tuple[pd.Series, pd.Series, pd.Series]:
    """Корзины по времени bin_s: RPS всех завершённых, success=true, HTTP 200."""
    d = df.copy()
    d["bucket"] = d["t"].dt.floor(f"{bin_s}s")
    total = d.groupby("bucket", sort=True).size() / bin_s
    ok = d.loc[d["ok"]].groupby("bucket", sort=True).size().reindex(total.index, fill_value=0) / bin_s
    http = d.loc[d["http_ok"]].groupby("bucket", sort=True).size().reindex(total.index, fill_value=0) / bin_s
    return total, ok, http


def plot_load_throughput(
    load_files: list[tuple[int, Path]],
    out_dir: Path,
    bin_s: float,
    rolling: int,
) -> None:
    out_dir.mkdir(parents=True, exist_ok=True)
    plt.style.use("seaborn-v0_8-whitegrid" if "seaborn-v0_8-whitegrid" in plt.style.available else "ggplot")

    fig, axes = plt.subplots(len(load_files), 1, figsize=(12, 3.8 * max(1, len(load_files))), squeeze=False)
    for ax, (cfg, path) in zip(axes.ravel(), load_files):
        df = _read_jtl_csv(path)
        rps_all, rps_ok, rps_200 = throughput_series(df, bin_s)
        if rolling > 1:
            rps_all = rps_all.rolling(rolling, min_periods=1).mean()
            rps_ok = rps_ok.rolling(rolling, min_periods=1).mean()
            rps_200 = rps_200.rolling(rolling, min_periods=1).mean()

        ax.plot(rps_all.index, rps_all.values, color="#4c72b0", linewidth=1.4, label="Все завершённые запросы (RPS)")
        ax.plot(rps_ok.index, rps_ok.values, color="#55a868", linewidth=1.2, alpha=0.85, label="Успех по JMeter (success=true)")
        ax.plot(rps_200.index, rps_200.values, color="#c44e52", linewidth=1.0, linestyle="--", alpha=0.9, label="HTTP 200 (RPS)")
        sec_rpm = ax.secondary_yaxis("right", functions=(lambda rps: rps * 60.0, lambda rpm: rpm / 60.0))
        sec_rpm.set_ylabel("Запросов / мин")
        ax.set_title(f"Пропускная способность — конфигурация {cfg} ({path.name})")
        ax.set_ylabel("Запросов / с")
        ax.set_xlabel("Время")
        ax.legend(loc="upper right", fontsize=9)
        ax.xaxis.set_major_formatter(mdates.DateFormatter("%H:%M:%S"))
        ax.xaxis.set_major_locator(mdates.AutoDateLocator())
        fig.autofmt_xdate(rotation=18)

        t0, t1 = df["t"].min(), df["t"].max()
        dur = (t1 - t0).total_seconds()
        thr_mean = len(df) / dur if dur > 0 else float("nan")
        ax.text(
            0.02,
            0.98,
            f"Средняя интенсивность за весь прогон: {thr_mean:.2f} req/s\n"
            f"Длительность: {dur:.0f} s · Запросов: {len(df)}",
            transform=ax.transAxes,
            fontsize=9,
            verticalalignment="top",
            bbox=dict(boxstyle="round", facecolor="white", alpha=0.88),
        )

        bins_tbl = pd.DataFrame(
            {
                "time_bucket_utc": rps_all.index,
                "rps_all": rps_all.values,
                "rps_jmeter_success": rps_ok.values,
                "rps_http_200": rps_200.values,
                "rpm_all": rps_all.values * 60.0,
            }
        )
        bins_tbl.to_csv(out_dir / f"throughput_bins_config_{cfg}.csv", index=False)

    fig.tight_layout()
    fig.savefig(out_dir / "throughput_by_configuration.png", dpi=160, bbox_inches="tight")
    plt.close(fig)

    fig2, ax2 = plt.subplots(figsize=(12, 5.5))
    colors = ["#4c72b0", "#dd8452", "#55a868", "#8172b3", "#937860"]
    for i, (cfg, path) in enumerate(load_files):
        df = _read_jtl_csv(path)
        t0 = df["t"].min()
        rel_s = (df["t"] - t0).dt.total_seconds()
        df_rel = df.assign(rel_s=rel_s)
        bins = np.arange(0, df_rel["rel_s"].max() + bin_s, bin_s)
        counts, edges = np.histogram(df_rel["rel_s"], bins=bins)
        centers = (edges[:-1] + edges[1:]) / 2
        rps = counts / bin_s
        if rolling > 1:
            rps = pd.Series(rps).rolling(rolling, min_periods=1).mean().to_numpy()
        ax2.plot(centers, rps, color=colors[i % len(colors)], linewidth=1.6, label=f"Config {cfg}")

    ax2.set_title("Сравнение пропускной способности (все завершённые запросы), время от начала каждого прогона")
    ax2.set_xlabel("Секунды от начала теста")
    ax2.set_ylabel("Запросов / с")
    ax2.legend(title="Конфигурация")
    ax2.grid(True, alpha=0.35)
    fig2.tight_layout()
    fig2.savefig(out_dir / "throughput_compare_normalized.png", dpi=160, bbox_inches="tight")
    plt.close(fig2)


def plot_stress_response_vs_load(stress_path: Path, out_dir: Path) -> None:
    df = _read_jtl_csv(stress_path)
    cfg = _config_from_url_row(df.iloc[0]) if len(df) > 0 else None
    g = df.groupby("allThreads", sort=True)["elapsed"]
    agg = g.agg(mean="mean", median="median", p95=lambda s: float(s.quantile(0.95)), count="count").reset_index()
    agg.rename(columns={"allThreads": "Параллельных потоков (нагрузка)"}, inplace=True)
    load_col = "Параллельных потоков (нагрузка)"

    plt.style.use("seaborn-v0_8-whitegrid" if "seaborn-v0_8-whitegrid" in plt.style.available else "ggplot")
    fig, ax = plt.subplots(figsize=(11, 6))
    x = agg[load_col].to_numpy()
    ax.plot(x, agg["mean"], "o-", color="#4c72b0", linewidth=2, markersize=7, label="Среднее время отклика (ms)")
    ax.plot(x, agg["median"], "s--", color="#8172b3", linewidth=1.5, markersize=5, label="Медиана (ms)")
    ax.plot(x, agg["p95"], "^-", color="#dd8452", linewidth=1.5, markersize=5, label="P95 (ms)")
    ax.axhline(SLA_MS, color="#c44e52", linestyle=":", linewidth=2, label=f"SLA {SLA_MS} ms")

    over = agg[agg["mean"] > SLA_MS]
    if not over.empty:
        first = int(over.iloc[0][load_col])
        ax.axvline(first, color="#937860", linestyle="--", alpha=0.85, label=f"Среднее > SLA при нагрузке ≥ {first}")

    title = "Стресс-тест: время отклика vs нагрузка"
    if cfg is not None:
        title += f" (config={cfg})"
    ax.set_title(title + f"\n{stress_path.name}")
    ax.set_xlabel("Нагрузка (число активных потоков JMeter, allThreads)")
    ax.set_ylabel("Время отклика, ms")
    ax.legend(loc="best", fontsize=9)
    ax.xaxis.set_major_locator(mticker.MaxNLocator(integer=True))
    ax.grid(True, alpha=0.35)

    fig.tight_layout()
    out_dir.mkdir(parents=True, exist_ok=True)
    fig.savefig(out_dir / "stress_response_time_vs_load.png", dpi=160, bbox_inches="tight")
    plt.close(fig)

    agg.to_csv(out_dir / "stress_aggregated_by_load.csv", index=False)

    fig_s, ax_s = plt.subplots(figsize=(11, 6))
    ax_s.scatter(
        df["allThreads"],
        df["elapsed"],
        s=6,
        alpha=0.12,
        c="#4c72b0",
        linewidths=0,
        rasterized=True,
    )
    ax_s.axhline(SLA_MS, color="#c44e52", linestyle=":", linewidth=2, label=f"SLA {SLA_MS} ms")
    ax_s.set_title("Стресс-тест: все измерения (нагрузка vs время отклика)")
    ax_s.set_xlabel("Нагрузка (allThreads)")
    ax_s.set_ylabel("Время отклика, ms")
    ax_s.legend(loc="upper left")
    ax_s.grid(True, alpha=0.3)
    fig_s.tight_layout()
    fig_s.savefig(out_dir / "stress_all_samples_scatter.png", dpi=160, bbox_inches="tight")
    plt.close(fig_s)

    thr_rows: list[dict[str, float | int]] = []
    for k, g in df.groupby("allThreads", sort=True):
        dt = (g["t"].max() - g["t"].min()).total_seconds()
        if dt <= 0:
            dt = 1e-3
        thr_rows.append({"threads": int(k), "rps": len(g) / dt, "samples": int(len(g))})
    thr_tbl = pd.DataFrame(thr_rows)
    if not thr_tbl.empty:
        thr_tbl.to_csv(out_dir / "stress_throughput_by_load.csv", index=False)
        fig_t, ax_t = plt.subplots(figsize=(11, 6))
        ax_t.plot(
            thr_tbl["threads"],
            thr_tbl["rps"],
            "o-",
            color="#55a868",
            linewidth=2,
            markersize=7,
        )
        ax_t.set_title("Стресс-тест: средняя интенсивность по ступеням нагрузки\n" + stress_path.name)
        ax_t.set_xlabel("Нагрузка (allThreads)")
        ax_t.set_ylabel("Запросов / с (оценка по интервалу времени ступени)")
        ax_t.xaxis.set_major_locator(mticker.MaxNLocator(integer=True))
        ax_t.grid(True, alpha=0.35)
        fig_t.tight_layout()
        fig_t.savefig(out_dir / "stress_throughput_vs_load.png", dpi=160, bbox_inches="tight")
        plt.close(fig_t)


def discover_load_summary(results_dir: Path) -> Path | None:
    p = results_dir / "load_summary.csv"
    return p if p.is_file() else None


def plot_load_summary_chart(summary_path: Path, out_dir: Path) -> None:
    """Сводка из Java-раннера: сравнение конфигураций по метрикам и цене (отдельно от сырых JTL)."""
    tbl = pd.read_csv(summary_path)
    colmap = {c.lower(): c for c in tbl.columns}
    req = ("config", "avg_ms", "p95_ms", "price_usd")
    if not all(r in colmap for r in req):
        print(f"Предупреждение: {summary_path.name} без колонок {req} — график сводки пропущен.")
        return
    tbl = tbl.rename(columns={colmap[r]: r for r in req})
    cfg = tbl["config"].astype(int)
    x = np.arange(len(cfg))
    w = 0.35

    plt.style.use("seaborn-v0_8-whitegrid" if "seaborn-v0_8-whitegrid" in plt.style.available else "ggplot")
    fig, ax = plt.subplots(figsize=(10, 6))
    ax.bar(x - w / 2, tbl["avg_ms"], width=w, label="Среднее время отклика (ms)", color="#4c72b0")
    ax.bar(x + w / 2, tbl["p95_ms"], width=w, label="P95 (ms)", color="#dd8452")
    ax.axhline(SLA_MS, color="#c44e52", linestyle=":", linewidth=2, label=f"SLA {SLA_MS} ms (по среднему)")
    ax.set_xticks(x)
    ax.set_xticklabels([f"config {c}" for c in cfg])
    ax.set_ylabel("Время отклика, ms")
    ax.set_title("Нагрузочный тест (фиксированные пользователи): сводка по конфигурациям")
    ax.grid(True, axis="y", alpha=0.35)

    ax2 = ax.twinx()
    ax2.plot(x, tbl["price_usd"], "D-", color="#8172b3", linewidth=1.8, markersize=8, label="Цена, USD")
    ax2.set_ylabel("Стоимость конфигурации, USD")
    lines, labels = ax.get_legend_handles_labels()
    l2, lab2 = ax2.get_legend_handles_labels()
    ax.legend(lines + l2, labels + lab2, loc="best", fontsize=9)

    fig.tight_layout()
    out_dir.mkdir(parents=True, exist_ok=True)
    fig.savefig(out_dir / "load_summary_config_comparison.png", dpi=160, bbox_inches="tight")
    plt.close(fig)


def plot_load_latency_over_time(
    load_files: list[tuple[int, Path]],
    out_dir: Path,
    bin_s: float,
    rolling: int,
) -> None:
    """Динамика задержки во времени (дополняет графики RPS, где по оси Y — пропускная способность)."""
    plt.style.use("seaborn-v0_8-whitegrid" if "seaborn-v0_8-whitegrid" in plt.style.available else "ggplot")
    fig, axes = plt.subplots(len(load_files), 1, figsize=(12, 3.8 * max(1, len(load_files))), squeeze=False)
    for ax, (cfg, path) in zip(axes.ravel(), load_files):
        df = _read_jtl_csv(path)
        mean_ms, p95_ms = latency_series(df, bin_s)
        if rolling > 1:
            mean_ms = mean_ms.rolling(rolling, min_periods=1).mean()
            p95_ms = p95_ms.rolling(rolling, min_periods=1).mean()
        ax.plot(mean_ms.index, mean_ms.values, color="#4c72b0", linewidth=1.4, label="Среднее elapsed (ms)")
        ax.plot(p95_ms.index, p95_ms.values, color="#dd8452", linewidth=1.2, linestyle="--", label="P95 elapsed (ms)")
        ax.axhline(SLA_MS, color="#c44e52", linestyle=":", linewidth=2, label=f"SLA {SLA_MS} ms")
        ax.set_title(f"Задержка во времени — конфигурация {cfg} ({path.name})")
        ax.set_ylabel("ms")
        ax.set_xlabel("Время")
        ax.legend(loc="upper right", fontsize=9)
        ax.xaxis.set_major_formatter(mdates.DateFormatter("%H:%M:%S"))
        fig.autofmt_xdate(rotation=18)

    fig.tight_layout()
    fig.savefig(out_dir / "load_latency_over_time.png", dpi=160, bbox_inches="tight")
    plt.close(fig)


def plot_load_sla_compliance_timeline(
    load_files: list[tuple[int, Path]],
    out_dir: Path,
    bin_s: float,
    rolling: int,
) -> None:
    """Доля запросов с elapsed <= SLA по времени (JMeter success при SLA-ассерте совпадает с бизнес-SLA)."""
    plt.style.use("seaborn-v0_8-whitegrid" if "seaborn-v0_8-whitegrid" in plt.style.available else "ggplot")
    fig, axes = plt.subplots(len(load_files), 1, figsize=(12, 3.5 * max(1, len(load_files))), squeeze=False)
    for ax, (cfg, path) in zip(axes.ravel(), load_files):
        df = _read_jtl_csv(path)
        rate = sla_ok_series(df, bin_s)
        if rolling > 1:
            rate = rate.rolling(rolling, min_periods=1).mean()
        ax.plot(rate.index, rate.values * 100.0, color="#55a868", linewidth=1.5, label=f"Доля ≤ {SLA_MS} ms (%)")
        ax.set_ylim(-2, 102)
        ax.set_title(f"Соблюдение порога задержки во времени — конфигурация {cfg}")
        ax.set_ylabel("% запросов")
        ax.set_xlabel("Время")
        ax.legend(loc="lower right", fontsize=9)
        ax.xaxis.set_major_formatter(mdates.DateFormatter("%H:%M:%S"))
        fig.autofmt_xdate(rotation=18)
        ax.grid(True, alpha=0.35)

    fig.tight_layout()
    fig.savefig(out_dir / "load_sla_compliance_over_time.png", dpi=160, bbox_inches="tight")
    plt.close(fig)


def plot_load_latency_cdf(load_files: list[tuple[int, Path]], out_dir: Path) -> None:
    """Эмпирическая CDF задержки по конфигурациям (один график, без оси времени)."""
    plt.style.use("seaborn-v0_8-whitegrid" if "seaborn-v0_8-whitegrid" in plt.style.available else "ggplot")
    fig, ax = plt.subplots(figsize=(10, 6))
    colors = ["#4c72b0", "#dd8452", "#55a868", "#8172b3", "#937860"]
    for i, (cfg, path) in enumerate(load_files):
        df = _read_jtl_csv(path)
        x = np.sort(df["elapsed"].to_numpy())
        n = len(x)
        if n == 0:
            continue
        y = np.arange(1, n + 1) / n
        ax.plot(x, y * 100.0, color=colors[i % len(colors)], linewidth=1.8, label=f"config {cfg} (n={n})")
    ax.axvline(SLA_MS, color="#c44e52", linestyle=":", linewidth=2, label=f"SLA {SLA_MS} ms")
    ax.set_xlabel("Время отклика, ms")
    ax.set_ylabel("Накопленная доля запросов, %")
    ax.set_title("Нагрузочный тест: сравнение распределений задержки (CDF)")
    ax.legend(loc="lower right")
    ax.grid(True, alpha=0.35)
    ax.set_xlim(left=0)
    fig.tight_layout()
    fig.savefig(out_dir / "load_latency_cdf.png", dpi=160, bbox_inches="tight")
    plt.close(fig)


def main() -> None:
    parser = argparse.ArgumentParser(description="Графики пропускной способности и стресс-теста из JMeter CSV.")
    parser.add_argument("--results-dir", type=Path, default=Path("jmeter/results"))
    parser.add_argument("--out-dir", type=Path, default=None, help="По умолчанию: <results-dir>/charts")
    parser.add_argument("--bin-s", type=float, default=1.0, help="Ширина корзины для RPS (секунды)")
    parser.add_argument("--rolling", type=int, default=3, help="Скользящее среднее по числу корзин (сглаживание)")
    args = parser.parse_args()

    results_dir = args.results_dir.resolve()
    out_dir = (args.out_dir or (results_dir / "charts")).resolve()

    load_files = discover_load_files(results_dir)
    if not load_files:
        raise SystemExit(
            f"Не найдены файлы load-config-<n>.csv в {results_dir}. "
            "Ожидаются имена вида load-config-1.csv, load-config-2.csv, …"
        )

    plot_load_throughput(load_files, out_dir, bin_s=args.bin_s, rolling=max(1, args.rolling))
    plot_load_latency_over_time(load_files, out_dir, bin_s=args.bin_s, rolling=max(1, args.rolling))
    plot_load_sla_compliance_timeline(load_files, out_dir, bin_s=args.bin_s, rolling=max(1, args.rolling))
    plot_load_latency_cdf(load_files, out_dir)

    summary = discover_load_summary(results_dir)
    if summary:
        plot_load_summary_chart(summary, out_dir)
    else:
        print("Предупреждение: нет load_summary.csv — график сводки по конфигурациям пропущен.")

    stress = discover_stress_file(results_dir)
    if stress:
        plot_stress_response_vs_load(stress.resolve(), out_dir)
    else:
        print("Предупреждение: не найден файл стресс-теста (*stress*.csv). График стресс-теста пропущен.")

    print(f"Готово. Графики сохранены в: {out_dir}")


if __name__ == "__main__":
    main()
