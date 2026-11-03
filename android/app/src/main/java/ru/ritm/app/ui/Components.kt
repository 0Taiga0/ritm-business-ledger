package ru.ritm.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.ritm.app.domain.Entry
import ru.ritm.app.domain.EntryType
import ru.ritm.app.domain.categoryTitle
import ru.ritm.app.domain.money
import ru.ritm.app.domain.shortDate

@Composable
internal fun MetricCard(label: String, amount: Long, income: Boolean, modifier: Modifier) {
    Card(
        modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(15.dp),
        border = BorderStroke(1.dp, Line),
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(label, color = Muted, fontSize = 11.sp)
                Icon(
                    if (income) Icons.Outlined.ArrowDownward else Icons.Outlined.ArrowUpward,
                    null,
                    Modifier.size(16.dp),
                    tint = if (income) Forest else Color(0xFFAB8A67),
                )
            }
            Text(
                money(amount),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp,
                modifier = Modifier.padding(top = 10.dp),
                maxLines = 1,
            )
        }
    }
}

@Composable
fun SurfaceCard(content: @Composable () -> Unit) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(17.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Line),
    ) {
        Column(Modifier.padding(18.dp)) { content() }
    }
}

@Composable
internal fun Legend(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(6.dp).clip(RoundedCornerShape(2.dp)).background(color))
        Text(label, fontSize = 9.sp, color = Muted, modifier = Modifier.padding(start = 5.dp))
    }
}

@Composable
internal fun EntryRow(entry: Entry, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(13.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val income = entry.type == EntryType.INCOME
        Box(
            Modifier.size(37.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (income) Color(0xFFEAF4EF) else Color(0xFFF8F0E6)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                if (income) Icons.Outlined.ArrowDownward else Icons.Outlined.ArrowUpward,
                null,
                Modifier.size(18.dp),
                tint = if (income) Forest else Color(0xFFA08062),
            )
        }
        Column(Modifier.weight(1f).padding(horizontal = 11.dp)) {
            Text(
                entry.counterparty,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                "${categoryTitle(entry.category)} · ${shortDate(entry.date)}",
                fontSize = 9.sp,
                color = Muted,
                modifier = Modifier.padding(top = 4.dp),
                maxLines = 1,
            )
        }
        Text(
            (if (income) "+" else "−") + money(entry.amountMinor),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (income) Forest else Ink,
        )
    }
}

@Composable
internal fun EmptyState() {
    Column(
        Modifier.fillMaxWidth().padding(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(Icons.Outlined.AccountBalanceWallet, null, Modifier.size(30.dp), tint = Muted)
        Text(
            "Пока нет операций",
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 12.dp),
        )
        Text("Измените фильтры или добавьте запись", color = Muted, fontSize = 11.sp)
    }
}
