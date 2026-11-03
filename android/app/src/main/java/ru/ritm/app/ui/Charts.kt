package ru.ritm.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.YearMonth
import ru.ritm.app.domain.Entry
import ru.ritm.app.domain.EntryType
import ru.ritm.app.domain.categories
import ru.ritm.app.domain.money
import ru.ritm.app.domain.totals

@Composable
fun FlowChart(entries: List<Entry>, month: String, modifier: Modifier = Modifier) {
    val days = YearMonth.parse(month).lengthOfMonth()
    val totals =
        (1..days).map { day -> entries.filter { it.date.takeLast(2).toInt() == day }.totals() }
    val maximum = totals.maxOfOrNull { maxOf(it.income, it.expense) }?.coerceAtLeast(1) ?: 1
    Column(modifier) {
        Canvas(
            Modifier.fillMaxWidth().height(124.dp).semantics {
                contentDescription = "Доходы и расходы по дням месяца"
            }
        ) {
            for (line in 0..3) {
                val y = size.height * line / 3
                drawLine(Line, Offset(0f, y), Offset(size.width, y), 1.dp.toPx())
            }
            val slot = size.width / days
            val barWidth = slot * 0.32f
            totals.forEachIndexed { index, value ->
                listOf(value.income to Color(0xFF4C9A81), value.expense to Sand).forEachIndexed {
                    side,
                    (amount, color) ->
                    if (amount > 0) {
                        val height = (amount.toFloat() / maximum) * (size.height - 5.dp.toPx())
                        drawRoundRect(
                            color,
                            Offset(
                                index * slot + side * (barWidth + 1.dp.toPx()),
                                size.height - height,
                            ),
                            Size(barWidth, height),
                            CornerRadius(2.dp.toPx()),
                        )
                    }
                }
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(top = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            listOf("01", "08", "15", "22", days.toString()).forEach {
                Text(it, color = Muted, fontSize = 9.sp)
            }
        }
    }
}

@Composable
fun ExpenseDonut(entries: List<Entry>) {
    val total = entries.totals().expense
    val groups =
        categories
            .filter { it.type == EntryType.EXPENSE }
            .map { category ->
                category to entries.filter { it.category == category.id }.sumOf { it.amountMinor }
            }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
    Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
        Canvas(
            Modifier.size(175.dp).semantics {
                contentDescription = "Структура расходов по категориям"
            }
        ) {
            var start = -90f
            groups.forEach { (category, amount) ->
                val sweep = amount.toFloat() / total.coerceAtLeast(1) * 360f
                drawArc(
                    Color(category.color),
                    start + 2,
                    (sweep - 4).coerceAtLeast(0f),
                    false,
                    Offset(12.dp.toPx(), 12.dp.toPx()),
                    Size(size.width - 24.dp.toPx(), size.height - 24.dp.toPx()),
                    style = Stroke(18.dp.toPx(), cap = StrokeCap.Round),
                )
                start += sweep
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Всего расходов", color = Muted, fontSize = 10.sp)
            Text(money(total), fontSize = 23.sp, fontWeight = FontWeight.Bold)
        }
    }
}
