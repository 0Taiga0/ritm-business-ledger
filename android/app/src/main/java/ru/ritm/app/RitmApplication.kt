package ru.ritm.app

import android.app.Application
import androidx.room.Room
import ru.ritm.app.data.LedgerDatabase
import ru.ritm.app.data.LedgerRepository

class RitmApplication : Application() {
    val repository by lazy {
        LedgerRepository(Room.databaseBuilder(this, LedgerDatabase::class.java, "ritm.db").build())
    }
}
