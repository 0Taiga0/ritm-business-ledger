import argparse
import json
from pathlib import Path

import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib.ticker import FuncFormatter
import pandas as pd

from .ledger import CATEGORIES, load_ledger, monthly_report

CATEGORY_COLORS = {
    "team": "#497f73",
    "rent": "#8db9ab",
    "marketing": "#d6b690",
    "tax": "#cc9178",
    "services": "#a6afc8",
    "other": "#bbc4c1",
}


def render_report(source: Path, month: str, output: Path) -> dict:
    frame = load_ledger(source)
    summary = monthly_report(frame, month)
    selected = frame[frame["date"].str.startswith(f"{month}-")].copy()
    output.mkdir(parents=True, exist_ok=True)

    plt.rcParams.update(
        {
            "font.family": "DejaVu Sans",
            "font.size": 10,
            "text.color": "#233b39",
            "axes.labelcolor": "#72857b",
            "xtick.color": "#72857b",
            "ytick.color": "#72857b",
            "axes.spines.top": False,
            "axes.spines.right": False,
            "axes.spines.left": False,
            "axes.spines.bottom": False,
        }
    )
    fig = plt.figure(figsize=(14, 8), facecolor="#f4f6f4")
    fig.text(0.06, 0.93, "ритм.", fontsize=25, fontweight="bold")
    fig.text(
        0.94,
        0.94,
        "СТУДИЯ КЕДР  /  ФИНАНСОВЫЙ ОБЗОР",
        ha="right",
        fontsize=9,
        color="#72857b",
    )
    fig.text(0.06, 0.865, f"Движение денег · {month}", fontsize=20, fontweight="bold")
    metrics = [
        ("Доходы", "incomeMinor"),
        ("Расходы", "expenseMinor"),
        ("Денежный результат", "profitMinor"),
        ("Остаток на конец месяца", "balanceMinor"),
    ]
    for index, (label, key) in enumerate(metrics):
        x = 0.06 + index * 0.23
        value = f"{summary[key] / 100:,.0f}".replace(",", " ")
        fig.text(x, 0.785, label, fontsize=10, color="#72857b")
        fig.text(x, 0.735, f"{value} ₽", fontsize=22, fontweight="bold")

    axes = fig.subplots(1, 2, gridspec_kw={"width_ratios": [1.65, 1]})
    fig.subplots_adjust(left=0.065, right=0.95, bottom=0.18, top=0.62, wspace=0.4)
    for axis in axes:
        axis.set_facecolor("#f4f6f4")
    period = pd.Period(month, freq="M")
    dates = pd.date_range(period.start_time, period.end_time, freq="D")
    selected["day"] = pd.to_datetime(selected["date"])
    daily = selected.pivot_table(
        index="day", columns="type", values="amountMinor", aggfunc="sum", fill_value=0
    ).reindex(dates, fill_value=0)
    x = range(1, len(dates) + 1)
    incomes = daily["income"] / 100 if "income" in daily else [0] * len(dates)
    expenses = daily["expense"] / 100 if "expense" in daily else [0] * len(dates)
    axes[0].bar(
        [day - 0.2 for day in x],
        incomes,
        width=0.38,
        color="#4c9a81",
        label="Доходы",
        zorder=3,
    )
    axes[0].bar(
        [day + 0.2 for day in x],
        expenses,
        width=0.38,
        color="#d9bba1",
        label="Расходы",
        zorder=3,
    )
    axes[0].set_title(
        "Поступления и платежи по дням",
        loc="left",
        fontsize=12,
        fontweight="bold",
        pad=22,
    )
    axes[0].yaxis.set_major_formatter(
        FuncFormatter(lambda amount, _: f"{amount / 1000:.0f} тыс.")
    )
    axes[0].grid(axis="y", color="#e3e9e2", zorder=0)
    axes[0].tick_params(length=0, pad=10)
    axes[0].legend(loc="upper right", frameon=False, fontsize=8)
    groups = (
        selected[selected["type"] == "expense"]
        .groupby("category")["amountMinor"]
        .sum()
        .sort_values()
    )
    if not groups.empty:
        axes[1].barh(
            [CATEGORIES[key][0] for key in groups.index],
            groups.values / 100,
            color=[CATEGORY_COLORS[key] for key in groups.index],
            height=0.55,
        )
    else:
        axes[1].text(0.5, 0.5, "Расходов нет", ha="center", transform=axes[1].transAxes)
    axes[1].set_title(
        "Структура расходов", loc="left", fontsize=12, fontweight="bold", pad=22
    )
    axes[1].xaxis.set_major_formatter(
        FuncFormatter(lambda amount, _: f"{amount / 1000:.0f} тыс.")
    )
    axes[1].tick_params(length=0, pad=10)
    fig.text(
        0.06,
        0.09,
        "Кассовый метод: поступления минус платежи. Начальный остаток 280 000 ₽.\nСуммы рассчитаны из операций; начисления и обязательства не учитываются.",
        fontsize=9,
        color="#72857b",
        linespacing=1.7,
    )
    fig.text(
        0.94,
        0.08,
        f"{summary['count']} операций  ·  RUB",
        fontsize=9,
        color="#72857b",
        ha="right",
    )
    fig.savefig(output / f"ritm-{month}.png", dpi=160, facecolor=fig.get_facecolor())
    fig.savefig(output / f"ritm-{month}.pdf", facecolor=fig.get_facecolor())
    plt.close(fig)
    (output / f"ritm-{month}.json").write_text(
        json.dumps(summary, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
    )
    return summary


def main():
    parser = argparse.ArgumentParser(description="Отчет Ритма из JSON или CSV")
    parser.add_argument("--input", type=Path, required=True)
    parser.add_argument("--month", required=True)
    parser.add_argument("--output", type=Path, default=Path("reports"))
    args = parser.parse_args()
    try:
        result = render_report(args.input, args.month, args.output)
    except (ValueError, OSError) as error:
        parser.exit(2, f"Ошибка: {error}\n")
    print(json.dumps(result, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
