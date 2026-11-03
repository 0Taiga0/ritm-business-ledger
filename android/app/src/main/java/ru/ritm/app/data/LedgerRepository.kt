package ru.ritm.app.data

import android.content.Context
import androidx.room.withTransaction
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import ru.ritm.app.domain.Entry
import ru.ritm.app.domain.EntryType
import ru.ritm.app.domain.validate

class LedgerRepository(private val database: LedgerDatabase) {
    private val dao = database.ledgerDao()
    val entries = dao.observeAll().map { rows -> rows.map { it.toDomain() } }

    suspend fun seedIfNeeded(context: Context) {
        database.withTransaction {
            if (dao.metadata("initialized") != null) return@withTransaction
            val source = context.assets.open("seed.json").bufferedReader().use { it.readText() }
            val array = JSONArray(source)
            val entries =
                (0 until array.length()).map { index ->
                    val item = array.getJSONObject(index)
                    Entry(
                            id = item.getString("id"),
                            date = item.getString("date"),
                            type = EntryType.valueOf(item.getString("type").uppercase()),
                            amountMinor = item.getLong("amountMinor"),
                            category = item.getString("category"),
                            counterparty = item.getString("counterparty"),
                            note = item.getString("note"),
                        )
                        .validate()
                }
            dao.insertMissing(entries.map(EntryEntity::fromDomain))
            dao.setMetadata(Metadata("initialized", "true"))
        }
    }

    suspend fun save(entry: Entry) = dao.save(EntryEntity.fromDomain(entry.validate()))

    suspend fun delete(id: String) = dao.delete(id)

    suspend fun importEntries(entries: List<Entry>): Int =
        database.withTransaction {
            require(entries.size <= 50_000) { "Максимум 50 000 операций" }
            require(entries.map { it.id }.distinct().size == entries.size) {
                "Повторяющиеся ID в файле"
            }
            dao.insertMissing(entries.map { EntryEntity.fromDomain(it.validate()) }).count {
                it != -1L
            }
        }
}
