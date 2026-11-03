package ru.ritm.app.domain

import java.math.BigDecimal
import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class EntryType {
    INCOME,
    EXPENSE,
}

data class Entry(
    val id: String,
    val date: String,
    val type: EntryType,
    val amountMinor: Long,
    val category: String,
    val counterparty: String,
    val note: String,
)

data class Category(val id: String, val title: String, val type: EntryType, val color: Long)

val categories =
    listOf(
        Category("projects", "Проекты", EntryType.INCOME, 0xFF48A88C),
        Category("support", "Сопровождение", EntryType.INCOME, 0xFF8CC3B5),
        Category("team", "Команда", EntryType.EXPENSE, 0xFF497F73),
        Category("rent", "Аренда", EntryType.EXPENSE, 0xFF8DB9AB),
        Category("marketing", "Маркетинг", EntryType.EXPENSE, 0xFFD6B690),
        Category("services", "Сервисы", EntryType.EXPENSE, 0xFFA6AFC8),
        Category("tax", "Налоги", EntryType.EXPENSE, 0xFFCC9178),
        Category("other", "Прочее", EntryType.EXPENSE, 0xFFBBC4C1),
    )

data class Totals(val income: Long, val expense: Long) {
    val profit: Long
        get() = income - expense

    val margin: Double?
        get() = if (income == 0L) null else profit.toDouble() / income * 100
}

const val OPENING_BALANCE = 28_000_000L
const val MONTHLY_BUDGET = 50_000_000L

fun List<Entry>.inMonth(month: String) = filter { it.date.startsWith("$month-") }

fun List<Entry>.totals() =
    Totals(
        income = filter { it.type == EntryType.INCOME }.sumOf { it.amountMinor },
        expense = filter { it.type == EntryType.EXPENSE }.sumOf { it.amountMinor },
    )

fun List<Entry>.balanceThrough(month: String) =
    OPENING_BALANCE + filter { it.date.take(7) <= month }.totals().profit

fun parseAmount(text: String): Long? {
    val normalized = text.trim().replace(Regex("\\s"), "").replace(',', '.')
    if (!Regex("\\d{1,9}(\\.\\d{1,2})?").matches(normalized)) return null
    return runCatching {
            BigDecimal(normalized).movePointRight(2).longValueExact().takeIf { it > 0 }
        }
        .getOrNull()
}

fun Entry.validate(): Entry {
    require(id.isNotBlank()) { "Пустой ID операции" }
    require(runCatching { LocalDate.parse(date).toString() == date }.getOrDefault(false)) {
        "Некорректная дата"
    }
    require(amountMinor in 1..99_999_999_999) { "Некорректная сумма" }
    require(categories.any { it.id == category && it.type == type }) {
        "Категория не соответствует операции"
    }
    require(counterparty.isNotBlank() && counterparty.length <= 120 && note.length <= 500) {
        "Проверьте описание"
    }
    return this
}

fun money(amountMinor: Long): String =
    NumberFormat.getNumberInstance(Locale.forLanguageTag("ru-RU"))
        .apply { maximumFractionDigits = if (amountMinor % 100 == 0L) 0 else 2 }
        .format(BigDecimal.valueOf(amountMinor, 2)) + " ₽"

fun monthTitle(month: String): String =
    YearMonth.parse(month)
        .format(DateTimeFormatter.ofPattern("LLLL yyyy", Locale.forLanguageTag("ru-RU")))
        .replaceFirstChar { it.uppercase() }

fun shortDate(date: String): String =
    LocalDate.parse(date)
        .format(DateTimeFormatter.ofPattern("d MMM", Locale.forLanguageTag("ru-RU")))

fun categoryTitle(id: String): String = categories.firstOrNull { it.id == id }?.title ?: "Прочее"
