from pathlib import Path
import json
import re

import pandas as pd

CATEGORIES = {
    "projects": ("Проекты", "income"),
    "support": ("Сопровождение", "income"),
    "team": ("Команда", "expense"),
    "rent": ("Аренда", "expense"),
    "marketing": ("Маркетинг", "expense"),
    "services": ("Сервисы", "expense"),
    "tax": ("Налоги", "expense"),
    "other": ("Прочее", "expense"),
}
COLUMNS = ["id", "date", "type", "amountMinor", "category", "counterparty", "note"]


def load_ledger(path: Path) -> pd.DataFrame:
    if path.stat().st_size > 10_000_000:
        raise ValueError("Файл больше 10 МБ")
    if path.suffix.lower() == ".json":
        frame = pd.DataFrame(json.loads(path.read_text(encoding="utf-8-sig")))
    else:
        frame = pd.read_csv(
            path, encoding="utf-8-sig", dtype=str, keep_default_na=False
        )
    if not set(COLUMNS).issubset(frame.columns) or len(frame) > 50_000:
        raise ValueError("Некорректные колонки или количество операций")
    frame = frame[COLUMNS].copy()
    if (
        frame["id"].duplicated().any()
        or (frame["id"].astype(str).str.strip() == "").any()
    ):
        raise ValueError("Повторяющиеся или пустые ID")
    amounts = frame["amountMinor"].astype(str)
    if not amounts.str.fullmatch(r"[1-9]\d{0,10}").all():
        raise ValueError("Суммы должны быть целыми положительными копейками")
    frame["amountMinor"] = amounts.astype("int64")
    dates = frame["date"].astype(str)
    if not dates.str.fullmatch(r"\d{4}-\d{2}-\d{2}").all():
        raise ValueError("Используйте дату ГГГГ-ММ-ДД")
    pd.to_datetime(dates, format="%Y-%m-%d", errors="raise")
    for row in frame.itertuples():
        if row.category not in CATEGORIES or CATEGORIES[row.category][1] != row.type:
            raise ValueError("Тип и категория операции не совпадают")
        if (
            not isinstance(row.counterparty, str)
            or not row.counterparty.strip()
            or len(row.counterparty) > 120
        ):
            raise ValueError("Некорректный контрагент")
        if not isinstance(row.note, str) or len(row.note) > 500:
            raise ValueError("Некорректный комментарий")
    return frame


def monthly_report(frame: pd.DataFrame, month: str) -> dict:
    if not re.fullmatch(r"\d{4}-\d{2}", month):
        raise ValueError("Месяц должен быть в формате ГГГГ-ММ")
    pd.Period(month, freq="M")
    selected = frame[frame["date"].str.startswith(f"{month}-")]
    income = int(selected.loc[selected["type"] == "income", "amountMinor"].sum())
    expense = int(selected.loc[selected["type"] == "expense", "amountMinor"].sum())
    preceding = frame[frame["date"].str[:7] <= month]
    balance = 28_000_000 + int(
        preceding.loc[preceding["type"] == "income", "amountMinor"].sum()
    )
    balance -= int(preceding.loc[preceding["type"] == "expense", "amountMinor"].sum())
    return {
        "month": month,
        "count": len(selected),
        "incomeMinor": income,
        "expenseMinor": expense,
        "profitMinor": income - expense,
        "balanceMinor": balance,
        "marginPercent": (income - expense) / income * 100 if income else None,
    }
