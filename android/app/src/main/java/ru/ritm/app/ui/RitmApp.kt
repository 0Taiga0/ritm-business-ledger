package ru.ritm.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.ritm.app.domain.Entry
import ru.ritm.app.domain.inMonth
import ru.ritm.app.domain.monthTitle

private enum class Screen(val label: String, val icon: ImageVector) {
    HOME("Обзор", Icons.Outlined.Dashboard),
    LEDGER("Операции", Icons.Outlined.AccountBalanceWallet),
    ANALYTICS("Аналитика", Icons.Outlined.BarChart),
    DATA("Данные", Icons.Outlined.Settings),
}

@Composable
fun RitmApp(
    entries: List<Entry>,
    ready: Boolean,
    message: String?,
    onMessageSeen: () -> Unit,
    onSave: (Entry) -> Unit,
    onDelete: (String) -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
) {
    var screen by rememberSaveable { mutableStateOf(Screen.HOME) }
    var month by rememberSaveable { mutableStateOf("2026-10") }
    var showEditor by remember { mutableStateOf(false) }
    var editedEntry by remember { mutableStateOf<Entry?>(null) }
    val snackbar = remember { SnackbarHostState() }
    val selected = entries.inMonth(month)
    val months = (entries.map { it.date.take(7) } + month).distinct().sortedDescending()

    LaunchedEffect(message) {
        message?.let {
            snackbar.showSnackbar(it)
            onMessageSeen()
        }
    }

    Scaffold(
        containerColor = Paper,
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 0.dp) {
                Screen.entries.forEach { item ->
                    NavigationBarItem(
                        selected = screen == item,
                        onClick = { screen = item },
                        icon = {
                            Icon(
                                item.icon,
                                contentDescription = null,
                                modifier = Modifier.size(21.dp),
                            )
                        },
                        label = { Text(item.label, fontSize = 10.sp) },
                        colors =
                            NavigationBarItemDefaults.colors(
                                indicatorColor = Color(0xFFE4F1E7),
                                selectedIconColor = Forest,
                                selectedTextColor = Forest,
                                unselectedTextColor = Muted,
                                unselectedIconColor = Muted,
                            ),
                    )
                }
            }
        },
        floatingActionButton = {
            if (screen == Screen.LEDGER)
                FloatingActionButton(
                    onClick = {
                        editedEntry = null
                        showEditor = true
                    },
                    containerColor = Forest,
                    contentColor = Color.White,
                ) {
                    Icon(Icons.Outlined.Add, "Добавить операцию")
                }
        },
    ) { insets ->
        Box(Modifier.fillMaxSize().padding(insets), contentAlignment = Alignment.TopCenter) {
            if (!ready) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            } else {
                Column(Modifier.fillMaxWidth().widthIn(max = 720.dp)) {
                    AppHeader()
                    Row(
                        Modifier.fillMaxWidth()
                            .padding(horizontal = 22.dp)
                            .padding(top = 6.dp, bottom = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        MonthSelector(month, months) { month = it }
                        Text("СТУДИЯ КЕДР", color = Muted, fontSize = 9.sp, letterSpacing = 1.2.sp)
                    }
                    when (screen) {
                        Screen.HOME ->
                            HomeScreen(
                                entries,
                                selected,
                                month,
                                onNew = {
                                    editedEntry = null
                                    showEditor = true
                                },
                                onLedger = { screen = Screen.LEDGER },
                                onEdit = {
                                    editedEntry = it
                                    showEditor = true
                                },
                            )
                        Screen.LEDGER ->
                            LedgerScreen(selected) {
                                editedEntry = it
                                showEditor = true
                            }
                        Screen.ANALYTICS -> AnalyticsScreen(selected, month)
                        Screen.DATA -> DataScreen(entries.size, onExport, onImport)
                    }
                }
            }
        }
    }

    if (showEditor)
        EntryEditor(
            editedEntry,
            month,
            onSave = {
                onSave(it)
                month = it.date.take(7)
            },
            onDelete = onDelete,
            onClose = { showEditor = false },
        )
}

@Composable
private fun AppHeader() {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 23.dp, vertical = 15.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(
                Modifier.height(26.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                listOf(14, 25, 19).forEach { height ->
                    Box(
                        Modifier.width(4.dp)
                            .height(height.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Forest)
                    )
                }
            }
            Text(
                "ритм.",
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-1.4).sp,
                modifier = Modifier.padding(start = 9.dp),
            )
        }
        Box(
            Modifier.size(37.dp).clip(CircleShape).background(Color(0xFFE5DECD)),
            contentAlignment = Alignment.Center,
        ) {
            Text("ЕК", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5B6753))
        }
    }
}

@Composable
private fun MonthSelector(month: String, months: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Row(
            Modifier.clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .clickable { expanded = true }
                .padding(horizontal = 11.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.CalendarMonth, null, Modifier.size(15.dp), tint = Muted)
            Text(
                monthTitle(month),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 7.dp),
            )
            Icon(Icons.Outlined.ExpandMore, "Выбрать месяц", Modifier.size(14.dp))
        }
        DropdownMenu(expanded, { expanded = false }) {
            months.forEach { candidate ->
                DropdownMenuItem(
                    text = { Text(monthTitle(candidate)) },
                    onClick = {
                        onSelect(candidate)
                        expanded = false
                    },
                )
            }
        }
    }
}
