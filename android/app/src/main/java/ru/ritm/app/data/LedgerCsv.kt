package ru.ritm.app.data

import ru.ritm.app.domain.Entry
import ru.ritm.app.domain.EntryType
import ru.ritm.app.domain.validate

object LedgerCsv {
    private val columns =
        listOf("id", "date", "type", "amountMinor", "category", "counterparty", "note")

    fun encode(entries: List<Entry>): String {
        val rows =
            entries.map { entry ->
                listOf(
                    entry.id,
                    entry.date,
                    entry.type.name.lowercase(),
                    entry.amountMinor.toString(),
                    entry.category,
                    entry.counterparty,
                    entry.note,
                )
            }
        return "\uFEFF" +
            (listOf(columns) + rows).joinToString("\r\n") { row ->
                row.joinToString(",") { cell ->
                    val safe =
                        if (cell.firstOrNull() in listOf('=', '+', '-', '@', '\t', '\r')) "'$cell"
                        else cell
                    "\"${safe.replace("\"", "\"\"")}\""
                }
            }
    }

    fun decode(text: String): List<Entry> {
        require(text.length <= 10_000_000) { "Файл больше 10 МБ" }
        val rows = parseRows(text.removePrefix("\uFEFF"))
        require(rows.isNotEmpty() && rows.first().toSet() == columns.toSet()) {
            "Неверные колонки CSV"
        }
        val header = rows.first()
        val entries =
            rows
                .drop(1)
                .filter { it.any(String::isNotBlank) }
                .map { row ->
                    require(row.size == header.size) { "Поврежденная строка CSV" }
                    val record = header.zip(row).toMap()
                    Entry(
                            id = record.getValue("id"),
                            date = record.getValue("date"),
                            type = EntryType.valueOf(record.getValue("type").uppercase()),
                            amountMinor = record.getValue("amountMinor").toLong(),
                            category = record.getValue("category"),
                            counterparty = record.getValue("counterparty"),
                            note = record.getValue("note"),
                        )
                        .validate()
                }
        require(entries.size <= 50_000) { "Максимум 50 000 операций" }
        require(entries.map { it.id }.distinct().size == entries.size) {
            "Повторяющиеся ID в файле"
        }
        return entries
    }

    private fun parseRows(text: String): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        var row = mutableListOf<String>()
        val cell = StringBuilder()
        var quoted = false
        var index = 0
        while (index < text.length) {
            val char = text[index]
            when {
                char == '"' && quoted && text.getOrNull(index + 1) == '"' -> {
                    cell.append('"')
                    index++
                }
                char == '"' -> quoted = !quoted
                char == ',' && !quoted -> {
                    row.add(cell.toString())
                    cell.clear()
                }
                char == '\n' && !quoted -> {
                    row.add(cell.toString().removeSuffix("\r"))
                    rows.add(row)
                    row = mutableListOf()
                    cell.clear()
                }
                else -> cell.append(char)
            }
            index++
        }
        require(!quoted) { "Незакрытые кавычки CSV" }
        if (cell.isNotEmpty() || row.isNotEmpty()) {
            row.add(cell.toString().removeSuffix("\r"))
            rows.add(row)
        }
        return rows
    }
}
