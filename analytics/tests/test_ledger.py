from pathlib import Path
import json

import pytest

from ritm_analytics.ledger import load_ledger, monthly_report

ROOT = Path(__file__).resolve().parents[2]


def test_shared_october_reference():
    result = monthly_report(load_ledger(ROOT / "shared/seed.json"), "2026-10")
    assert result["count"] == 19
    assert result["incomeMinor"] == 91_450_000
    assert result["expenseMinor"] == 41_340_000
    assert result["profitMinor"] == 50_110_000
    assert result["balanceMinor"] == 153_939_100


def test_empty_month_has_no_infinite_margin():
    result = monthly_report(load_ledger(ROOT / "shared/seed.json"), "2026-07")
    assert result["marginPercent"] is None
    assert result["balanceMinor"] == 28_000_000


@pytest.mark.parametrize(
    "field,value",
    [
        ("date", "2026-02-30"),
        ("amountMinor", -1),
        ("amountMinor", 1.2),
        ("category", "rent"),
    ],
)
def test_rejects_invalid_input(tmp_path, field, value):
    data = json.loads((ROOT / "shared/seed.json").read_text(encoding="utf-8"))[:1]
    data[0][field] = value
    path = tmp_path / "invalid.json"
    path.write_text(json.dumps(data), encoding="utf-8")
    with pytest.raises(ValueError):
        load_ledger(path)
