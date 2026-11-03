package ru.ritm.app.ui

import kotlin.math.roundToInt

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.ritm.app.domain.Entry
import ru.ritm.app.domain.EntryType
import ru.ritm.app.domain.MONTHLY_BUDGET
import ru.ritm.app.domain.categories
import ru.ritm.app.domain.money
import ru.ritm.app.domain.totals

@Composable
internal fun AnalyticsScreen(entries: List<Entry>, month: String) {
    val totals = entries.totals()
    val groups =
        categories
            .filter { it.type == EntryType.EXPENSE }
            .map { category ->
                category to entries.filter { it.category == category.id }.sumOf { it.amountMinor }
            }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
    LazyColumn(
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 25.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp),
    ) {
        item {
            Text(
                "За цифрами видно больше",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 3.dp),
            )
        }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE4EFE6)),
                shape = RoundedCornerShape(17.dp),
            ) {
                Column(Modifier.fillMaxWidth().padding(21.dp)) {
                    Text(
                        "ДЕНЕЖНЫЙ РЕЗУЛЬТАТ",
                        fontSize = 9.sp,
                        color = Forest,
                        letterSpacing = 1.sp,
                    )
                    Text(
                        money(totals.profit),
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-1).sp,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                    Text(
                        totals.margin?.let { "%.1f%% от доходов".format(it) }
                            ?: "Доходов в этом месяце нет",
                        color = Forest,
                        fontSize = 12.sp,
                    )
                    Text(
                        "Поступления минус платежи. Без учета начислений и обязательств.",
                        fontSize = 10.sp,
                        color = Muted,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
            }
        }
        item {
            SurfaceCard {
                Text("Структура расходов", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                ExpenseDonut(entries)
                groups.forEach { (category, amount) ->
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            Modifier.size(7.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color(category.color))
                        )
                        Text(
                            category.title,
                            Modifier.weight(1f).padding(start = 9.dp),
                            fontSize = 11.sp,
                        )
                        Text(money(amount), fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                        Text(
                            "${(amount * 100.0 / totals.expense.coerceAtLeast(1)).roundToInt()}%",
                            Modifier.width(36.dp),
                            color = Muted,
                            fontSize = 10.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.End,
                        )
                    }
                }
            }
        }
        item {
            SurfaceCard {
                Text("Динамика месяца", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(18.dp))
                FlowChart(entries, month)
            }
        }
        item {
            SurfaceCard {
                Text("Расходный план", fontWeight = FontWeight.Bold)
                Text(
                    "${money(totals.expense)} из ${money(MONTHLY_BUDGET)}",
                    fontSize = 12.sp,
                    color = Muted,
                    modifier = Modifier.padding(vertical = 13.dp),
                )
                LinearProgressIndicator(
                    progress = { (totals.expense.toFloat() / MONTHLY_BUDGET).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(7.dp),
                    color = Color(0xFF869B6C),
                    trackColor = Color(0xFFE4E9D8),
                )
            }
        }
    }
}
