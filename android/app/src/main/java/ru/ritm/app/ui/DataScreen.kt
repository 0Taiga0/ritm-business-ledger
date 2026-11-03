package ru.ritm.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun DataScreen(count: Int, onExport: () -> Unit, onImport: () -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp),
    ) {
        item { Text("Ваши данные", style = MaterialTheme.typography.headlineMedium) }
        item {
            SurfaceCard {
                Icon(Icons.Outlined.Shield, null, Modifier.size(30.dp), tint = Forest)
                Text(
                    "Локальное пространство",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 16.dp, bottom = 10.dp),
                )
                Text(
                    "Записи хранятся в базе на этом устройстве. Приложение не запрашивает доступ к сети. Всего операций: $count.",
                    color = Muted,
                )
                Text(
                    "Начальный остаток: 280 000 ₽",
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 15.dp),
                )
            }
        }
        item {
            SurfaceCard {
                Text("Перенос и резервная копия", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Web и Android используют один формат CSV. Экспортируйте записи и импортируйте их на другом устройстве. Повторяющиеся ID будут пропущены.",
                    color = Muted,
                    modifier = Modifier.padding(vertical = 14.dp),
                )
                Button(onExport, Modifier.fillMaxWidth()) {
                    Icon(Icons.Outlined.FileDownload, null, Modifier.size(17.dp))
                    Text("Экспортировать CSV", Modifier.padding(8.dp))
                }
                OutlinedButton(onImport, Modifier.fillMaxWidth().padding(top = 7.dp)) {
                    Icon(Icons.Outlined.FileUpload, null, Modifier.size(17.dp))
                    Text("Импортировать CSV", Modifier.padding(8.dp))
                }
                Text(
                    "Удаление приложения удалит и его данные. Сохраняйте резервную копию регулярно.",
                    color = Muted,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(top = 14.dp),
                )
            }
        }
        item {
            Text(
                "Ритм 1.0 · учет без лишнего",
                color = Muted,
                fontSize = 10.sp,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}
