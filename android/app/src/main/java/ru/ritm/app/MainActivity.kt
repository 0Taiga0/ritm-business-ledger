package ru.ritm.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.ritm.app.data.LedgerCsv
import ru.ritm.app.ui.RitmApp
import ru.ritm.app.ui.RitmTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val model: LedgerViewModel = viewModel()
            val entries by model.entries.collectAsStateWithLifecycle()
            val ready by model.ready.collectAsStateWithLifecycle()
            val message by model.message.collectAsStateWithLifecycle()
            val scope = rememberCoroutineScope()
            val export =
                rememberLauncherForActivityResult(
                    ActivityResultContracts.CreateDocument("text/csv")
                ) { uri ->
                    if (uri != null)
                        scope.launch {
                            runCatching {
                                    withContext(Dispatchers.IO) {
                                        val output =
                                            contentResolver.openOutputStream(uri)
                                                ?: error("Файл недоступен")
                                        output.bufferedWriter(Charsets.UTF_8).use {
                                            it.write(LedgerCsv.encode(entries))
                                        }
                                    }
                                }
                                .onSuccess { model.notify("Все операции экспортированы") }
                                .onFailure {
                                    model.notify("Не удалось сохранить CSV: ${it.message}")
                                }
                        }
                }
            val import =
                rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                    if (uri != null)
                        scope.launch {
                            runCatching {
                                    withContext(Dispatchers.IO) {
                                        val input =
                                            contentResolver.openInputStream(uri)
                                                ?: error("Файл недоступен")
                                        val text =
                                            input.bufferedReader(Charsets.UTF_8).use { reader ->
                                                val result = StringBuilder()
                                                val buffer = CharArray(4096)
                                                while (true) {
                                                    val count = reader.read(buffer)
                                                    if (count < 0) break
                                                    result.append(buffer, 0, count)
                                                    require(result.length <= 10_000_000) {
                                                        "Файл больше 10 МБ"
                                                    }
                                                }
                                                result.toString()
                                            }
                                        LedgerCsv.decode(text)
                                    }
                                }
                                .onSuccess(model::importEntries)
                                .onFailure {
                                    model.notify("Не удалось импортировать CSV: ${it.message}")
                                }
                        }
                }
            RitmTheme {
                RitmApp(
                    entries,
                    ready,
                    message,
                    model::clearMessage,
                    onSave = { model.save(it) },
                    onDelete = { model.delete(it) },
                    onExport = { export.launch("ritm-operations.csv") },
                    onImport = {
                        import.launch(
                            arrayOf("text/*", "application/csv", "application/octet-stream")
                        )
                    },
                )
            }
        }
    }
}
