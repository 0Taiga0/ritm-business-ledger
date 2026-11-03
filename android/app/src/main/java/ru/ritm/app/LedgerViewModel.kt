package ru.ritm.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.ritm.app.domain.Entry

class LedgerViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as RitmApplication).repository
    val entries =
        repository.entries.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList(),
        )
    private val _ready = MutableStateFlow(false)
    val ready = _ready.asStateFlow()
    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { repository.seedIfNeeded(application) }
                .onSuccess { _ready.value = true }
                .onFailure { _message.value = "Не удалось открыть базу: ${it.message}" }
        }
    }

    fun save(entry: Entry) =
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { repository.save(entry) }
                .onSuccess { _message.value = "Операция сохранена" }
                .onFailure { _message.value = "Не удалось сохранить: ${it.message}" }
        }

    fun delete(id: String) =
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { repository.delete(id) }
                .onSuccess { _message.value = "Операция удалена" }
                .onFailure { _message.value = "Не удалось удалить: ${it.message}" }
        }

    fun importEntries(entries: List<Entry>) =
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { repository.importEntries(entries) }
                .onSuccess { _message.value = "Добавлено операций: $it. Повторы пропущены." }
                .onFailure { _message.value = "Ошибка импорта: ${it.message}" }
        }

    fun notify(message: String) {
        _message.value = message
    }

    fun clearMessage() {
        _message.value = null
    }
}
