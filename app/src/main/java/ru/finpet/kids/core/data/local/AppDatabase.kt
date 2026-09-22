package ru.finpet.kids.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.finpet.kids.core.data.local.dao.CalendarDao
import ru.finpet.kids.core.data.local.dao.GoalDao
import ru.finpet.kids.core.data.local.dao.PeriodDao
import ru.finpet.kids.core.data.local.dao.ProfileDao
import ru.finpet.kids.core.data.local.dao.PurchaseDao
import ru.finpet.kids.core.data.local.dao.QuestProgressDao
import ru.finpet.kids.core.data.local.entity.CalendarNoteEntity
import ru.finpet.kids.core.data.local.entity.GoalEntity
import ru.finpet.kids.core.data.local.entity.PeriodEntity
import ru.finpet.kids.core.data.local.entity.ProfileEntity
import ru.finpet.kids.core.data.local.entity.PurchaseEntity
import ru.finpet.kids.core.data.local.entity.QuestProgressEntity
import ru.finpet.kids.core.data.local.entity.RecurringExpenseEntity

@Database(
    entities = [
        ProfileEntity::class,
        PeriodEntity::class,
        PurchaseEntity::class,
        GoalEntity::class,
        QuestProgressEntity::class,
        CalendarNoteEntity::class,
        RecurringExpenseEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun periodDao(): PeriodDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun goalDao(): GoalDao
    abstract fun questProgressDao(): QuestProgressDao
    abstract fun calendarDao(): CalendarDao

    companion object {
        const val DATABASE_NAME = "finpet_database.db"
    }
}
