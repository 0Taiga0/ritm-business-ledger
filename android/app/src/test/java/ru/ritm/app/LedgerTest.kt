package ru.ritm.app

import org.junit.Assert.*
import org.junit.Test
import ru.ritm.app.data.LedgerCsv
import ru.ritm.app.domain.*

class LedgerTest {
    private val sale =
        Entry(
            "one",
            "2026-10-12",
            EntryType.INCOME,
            125050,
            "projects",
            "Кедр, студия",
            "Первый этап\nОплачен",
        )
    private val expense =
        sale.copy(id = "two", type = EntryType.EXPENSE, category = "rent", amountMinor = 25050)

    @Test
    fun moneyIsCalculatedInIntegerKopecks() {
        assertEquals(125050L, parseAmount("1 250,50"))
        assertEquals(1L, parseAmount("0.01"))
        listOf("0", "-5", "1.999", "1e4", "NaN", "").forEach { assertNull(parseAmount(it)) }
        assertEquals(100000L, listOf(sale, expense).totals().profit)
    }

    @Test
    fun futurePaymentsDoNotChangePastBalance() {
        assertEquals(
            28125050L,
            listOf(sale, expense.copy(date = "2026-11-01")).balanceThrough("2026-10"),
        )
        assertNull(emptyList<Entry>().totals().margin)
    }

    @Test
    fun malformedDatesAndCategoriesAreRejected() {
        assertTrue(runCatching { sale.copy(date = "2026-02-30").validate() }.isFailure)
        assertTrue(runCatching { sale.copy(category = "rent").validate() }.isFailure)
    }

    @Test
    fun csvRoundTripsQuotesCommasNewlinesAndCyrillic() {
        val data = listOf(sale.copy(note = "Текст \"в кавычках\"\nНовая строка"), expense)
        assertEquals(data, LedgerCsv.decode(LedgerCsv.encode(data)))
    }

    @Test
    fun csvBlocksDuplicateIdsAndEscapesFormulae() {
        assertTrue(runCatching { LedgerCsv.decode(LedgerCsv.encode(listOf(sale, sale))) }.isFailure)
        assertTrue(
            LedgerCsv.encode(listOf(sale.copy(counterparty = "=SUM(1,2)"))).contains("'=SUM")
        )
    }
}
