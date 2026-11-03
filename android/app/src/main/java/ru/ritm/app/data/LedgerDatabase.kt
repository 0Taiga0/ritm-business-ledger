package ru.ritm.app.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow
import ru.ritm.app.domain.Entry
import ru.ritm.app.domain.EntryType

@Entity(tableName = "entries")
data class EntryEntity(
    @PrimaryKey val id: String,
    val date: String,
    val type: String,
    val amountMinor: Long,
    val category: String,
    val counterparty: String,
    val note: String,
) {
    fun toDomain() =
        Entry(id, date, EntryType.valueOf(type), amountMinor, category, counterparty, note)

    companion object {
        fun fromDomain(entry: Entry) =
            EntryEntity(
                entry.id,
                entry.date,
                entry.type.name,
                entry.amountMinor,
                entry.category,
                entry.counterparty,
                entry.note,
            )
    }
}

@Entity(tableName = "metadata") data class Metadata(@PrimaryKey val key: String, val value: String)

@Dao
interface LedgerDao {
    @Query("SELECT * FROM entries ORDER BY date DESC, id ASC")
    fun observeAll(): Flow<List<EntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun save(entry: EntryEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMissing(entries: List<EntryEntity>): List<Long>

    @Query("DELETE FROM entries WHERE id = :id") suspend fun delete(id: String)

    @Query("SELECT value FROM metadata WHERE `key` = :key")
    suspend fun metadata(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun setMetadata(metadata: Metadata)
}

@Database(entities = [EntryEntity::class, Metadata::class], version = 1, exportSchema = true)
abstract class LedgerDatabase : RoomDatabase() {
    abstract fun ledgerDao(): LedgerDao
}
