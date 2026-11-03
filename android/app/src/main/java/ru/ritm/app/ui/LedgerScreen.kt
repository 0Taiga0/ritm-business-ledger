package ru.ritm.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.ritm.app.domain.Entry
import ru.ritm.app.domain.money
import ru.ritm.app.domain.totals

@Composable
internal fun LedgerScreen(entries: List<Entry>, onEdit: (Entry) -> Unit) {
    var filter by rememberSaveable { mutableStateOf("all") }
    var search by rememberSaveable { mutableStateOf("") }
    val filtered =
        entries
            .filter { entry ->
                (filter == "all" || entry.type.name == filter) &&
                    (entry.counterparty + " " + entry.note).contains(search, ignoreCase = true)
            }
            .sortedByDescending { it.date }
    LazyColumn(
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Text(
                "Операции",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }
        item {
            OutlinedTextField(
                search,
                { search = it },
                Modifier.fillMaxWidth(),
                placeholder = { Text("Найти операцию") },
                leadingIcon = { Icon(Icons.Outlined.Search, null) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("all" to "Все", "INCOME" to "Доходы", "EXPENSE" to "Расходы").forEach {
                    (id, title) ->
                    FilterChip(filter == id, { filter = id }, label = { Text(title) })
                }
            }
        }
        item {
            Text(
                "${filtered.size} операций · итог ${money(filtered.totals().profit)}",
                color = Muted,
                fontSize = 10.sp,
                modifier = Modifier.padding(bottom = 7.dp),
            )
        }
        if (filtered.isEmpty()) item { EmptyState() }
        items(filtered, key = { it.id }) { entry -> EntryRow(entry) { onEdit(entry) } }
    }
}
