import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
TEMPLATE = [
    (2, "income", 120000, "projects", "Бюро Север", "Айдентика и брендбук"),
    (3, "expense", 65000, "rent", "Пространство Фабрика", "Аренда студии"),
    (5, "income", 84000, "projects", "Лесная линия", "Дизайн интернет-магазина"),
    (6, "expense", 12400, "services", "Рабочие сервисы", "Подписки команды"),
    (8, "income", 156000, "projects", "Керамика 22", "Сайт и каталог"),
    (9, "expense", 35000, "marketing", "Медиа Лаб", "Рекламная кампания"),
    (10, "expense", 110000, "team", "Команда студии", "Первая часть оплаты"),
    (12, "income", 94500, "projects", "Сад и город", "Лендинг нового проекта"),
    (14, "expense", 8600, "other", "Печатный цех", "Образцы и презентации"),
    (16, "income", 87000, "support", "Тихий берег", "Поддержка продукта"),
    (17, "expense", 11600, "services", "Облако", "Хостинг и инфраструктура"),
    (19, "income", 122000, "projects", "Moss Coffee", "Упаковка и меню"),
    (20, "expense", 15000, "marketing", "Фотостудия Свет", "Предметная съемка"),
    (22, "income", 65000, "support", "Бюро Север", "Развитие сайта"),
    (24, "expense", 110000, "team", "Команда студии", "Вторая часть оплаты"),
    (26, "expense", 38000, "tax", "Налоги", "Налоговый резерв"),
    (28, "income", 144000, "projects", "Ателье Нить", "Сайт и личный кабинет"),
    (29, "expense", 7800, "other", "Городские поездки", "Встречи с клиентами"),
    (30, "income", 42000, "support", "Керамика 22", "Сопровождение каталога"),
]


def build_seed():
    records = []
    for month, factor in [(8, 0.78), (9, 0.89), (10, 1.0)]:
        for index, (day, kind, rubles, category, counterparty, note) in enumerate(
            TEMPLATE
        ):
            multiplier = factor if kind == "income" else (0.93 if month != 10 else 1.0)
            records.append(
                {
                    "id": f"seed-{month}-{index}",
                    "date": f"2026-{month:02}-{day:02}",
                    "type": kind,
                    "amountMinor": round(rubles * multiplier) * 100,
                    "category": category,
                    "counterparty": counterparty,
                    "note": note,
                }
            )
    for target in [
        ROOT / "shared/seed.json",
        ROOT / "web/public/seed.json",
        ROOT / "android/app/src/main/assets/seed.json",
    ]:
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_text(
            json.dumps(records, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
        )


if __name__ == "__main__":
    build_seed()
