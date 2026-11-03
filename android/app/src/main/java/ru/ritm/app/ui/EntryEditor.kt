package ru.ritm.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.math.BigDecimal
import java.util.UUID
import ru.ritm.app.domain.Entry
import ru.ritm.app.domain.EntryType
import ru.ritm.app.domain.categories
import ru.ritm.app.domain.categoryTitle
import ru.ritm.app.domain.parseAmount
import ru.ritm.app.domain.validate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryEditor(
    entry: Entry?,
    month: String,
    onSave: (Entry) -> Unit,
    onDelete: (String) -> Unit,
    onClose: () -> Unit,
) {
    var type by remember { mutableStateOf(entry?.type ?: EntryType.EXPENSE) }
    var amount by remember {
        mutableStateOf(entry?.let { BigDecimal.valueOf(it.amountMinor, 2).toPlainString() } ?: "")
    }
    var date by remember { mutableStateOf(entry?.date ?: "$month-15") }
    var category by remember { mutableStateOf(entry?.category ?: "services") }
    var counterparty by remember { mutableStateOf(entry?.counterparty ?: "") }
    var note by remember { mutableStateOf(entry?.note ?: "") }
    var expanded by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var deleteConfirmation by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            Modifier.fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 30.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                if (entry == null) "Новая операция" else "Детали операции",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                EntryType.entries.forEach { candidate ->
                    FilterChip(
                        selected = type == candidate,
                        onClick = {
                            type = candidate
                            category = if (candidate == EntryType.INCOME) "projects" else "services"
                        },
                        label = { Text(if (candidate == EntryType.INCOME) "Доход" else "Расход") },
                    )
                }
            }
            OutlinedTextField(
                amount,
                { amount = it },
                Modifier.fillMaxWidth(),
                label = { Text("Сумма, ₽") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
            )
            OutlinedTextField(
                date,
                { date = it },
                Modifier.fillMaxWidth(),
                label = { Text("Дата: ГГГГ-ММ-ДД") },
                singleLine = true,
            )
            ExposedDropdownMenuBox(expanded, { expanded = !expanded }) {
                OutlinedTextField(
                    categoryTitle(category),
                    {},
                    Modifier.menuAnchor().fillMaxWidth(),
                    readOnly = true,
                    label = { Text("Категория") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                )
                ExposedDropdownMenu(expanded, { expanded = false }) {
                    categories
                        .filter { it.type == type }
                        .forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item.title) },
                                onClick = {
                                    category = item.id
                                    expanded = false
                                },
                            )
                        }
                }
            }
            OutlinedTextField(
                counterparty,
                { if (it.length <= 120) counterparty = it },
                Modifier.fillMaxWidth(),
                label = { Text("Контрагент") },
                singleLine = true,
            )
            OutlinedTextField(
                note,
                { if (it.length <= 500) note = it },
                Modifier.fillMaxWidth(),
                label = { Text("Комментарий") },
                maxLines = 3,
            )
            error?.let {
                Text(it, color = androidx.compose.material3.MaterialTheme.colorScheme.error)
            }
            Button(
                onClick = {
                    val minor = parseAmount(amount)
                    if (minor == null) {
                        error = "Введите положительную сумму с точностью до копеек"
                        return@Button
                    }
                    runCatching {
                            Entry(
                                    entry?.id ?: UUID.randomUUID().toString(),
                                    date,
                                    type,
                                    minor,
                                    category,
                                    counterparty.trim(),
                                    note.trim(),
                                )
                                .validate()
                        }
                        .onSuccess {
                            onSave(it)
                            onClose()
                        }
                        .onFailure { error = it.message ?: "Проверьте поля" }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Сохранить операцию", Modifier.padding(6.dp))
            }
            if (entry != null)
                TextButton(
                    onClick = { deleteConfirmation = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        "Удалить операцию",
                        color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                    )
                }
        }
    }
    if (deleteConfirmation)
        AlertDialog(
            onDismissRequest = { deleteConfirmation = false },
            title = { Text("Удалить операцию?") },
            text = { Text("Отменить это действие нельзя.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        entry?.let { onDelete(it.id) }
                        onClose()
                    }
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmation = false }) { Text("Оставить") }
            },
        )
}
