package ru.ritm.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import ru.ritm.app.domain.balanceThrough
import ru.ritm.app.domain.money
import ru.ritm.app.domain.totals

@Composable
internal fun HomeScreen(
    all: List<Entry>,
    selected: List<Entry>,
    month: String,
    onNew: () -> Unit,
    onLedger: () -> Unit,
    onEdit: (Entry) -> Unit,
) {
    val totals = selected.totals()
    LazyColumn(
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF243E3E)),
                shape = RoundedCornerShape(20.dp),
            ) {
                Column(Modifier.fillMaxWidth().padding(22.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Остаток на конец месяца", color = Color(0xFFB0C9BE), fontSize = 11.sp)
                        Icon(
                            Icons.Outlined.AccountBalanceWallet,
                            null,
                            Modifier.size(18.dp),
                            tint = Color(0xFFB0C9BE),
                        )
                    }
                    Text(
                        money(all.balanceThrough(month)),
                        color = Color.White,
                        fontSize = 35.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-1.3).sp,
                        modifier = Modifier.padding(top = 12.dp, bottom = 16.dp),
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(5.dp).clip(CircleShape).background(Mint))
                        Text(
                            "Начальный остаток + все операции",
                            color = Color(0xFFB0C9BE),
                            fontSize = 10.sp,
                            modifier = Modifier.padding(start = 7.dp),
                        )
                    }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard("Доходы", totals.income, true, Modifier.weight(1f))
                MetricCard("Расходы", totals.expense, false, Modifier.weight(1f))
            }
        }
        item {
            SurfaceCard {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text("Движение денег", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            "Каждый день в цифрах",
                            color = Muted,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(top = 3.dp),
                        )
                    }
                    Text("RUB", color = Muted, fontSize = 9.sp)
                }
                Row(
                    Modifier.padding(top = 14.dp, bottom = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(15.dp),
                ) {
                    Legend("Доходы", Color(0xFF4C9A81))
                    Legend("Расходы", Sand)
                }
                FlowChart(selected, month)
                Row(
                    Modifier.fillMaxWidth().padding(top = 17.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Результат месяца", color = Muted, fontSize = 10.sp)
                    Text(
                        money(totals.profit),
                        color = Forest,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                    )
                }
            }
        }
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Последние операции", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                TextButton(onClick = onLedger) {
                    Text("Все", fontSize = 11.sp)
                    Icon(
                        Icons.AutoMirrored.Outlined.ArrowForward,
                        null,
                        Modifier.padding(start = 5.dp).size(14.dp),
                    )
                }
            }
        }
        items(selected.sortedByDescending { it.date }.take(3), key = { it.id }) { entry ->
            EntryRow(entry) { onEdit(entry) }
        }
        item {
            OutlinedButton(
                onClick = onNew,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            ) {
                Icon(Icons.Outlined.Add, null, Modifier.size(16.dp))
                Text("Добавить операцию", Modifier.padding(7.dp))
            }
        }
    }
}
