package ru.finpet.kids.core.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.finpet.kids.core.data.local.AppDatabase
import ru.finpet.kids.core.data.local.dao.GoalDao
import ru.finpet.kids.core.data.local.dao.PeriodDao
import ru.finpet.kids.core.data.local.dao.ProfileDao
import ru.finpet.kids.core.data.local.dao.PurchaseDao
import ru.finpet.kids.core.data.local.dao.QuestProgressDao
import ru.finpet.kids.core.data.repository.FinPetRepositoryImpl
import ru.finpet.kids.core.domain.repository.FinPetRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    // v2 → v3: добавлены индексы для purchases и calendar_notes.
    // Без миграции Room не найдёт их в ожидаемой схеме и без
    // fallbackToDestructiveMigration стёр бы данные пользователя.
    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_purchases_periodIndex_timestamp` ON `purchases` (`periodIndex`, `timestamp`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_purchases_timestamp` ON `purchases` (`timestamp`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_calendar_notes_dayIndex` ON `calendar_notes` (`dayIndex`)")
        }
    }

    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE profiles ADD COLUMN isPetRunaway INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE profiles ADD COLUMN runawayDaysLeft INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE profiles ADD COLUMN bowlPlacedToday INTEGER NOT NULL DEFAULT 0")
        }
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideProfileDao(db: AppDatabase): ProfileDao = db.profileDao()

    @Provides
    fun providePeriodDao(db: AppDatabase): PeriodDao = db.periodDao()

    @Provides
    fun providePurchaseDao(db: AppDatabase): PurchaseDao = db.purchaseDao()

    @Provides
    fun provideGoalDao(db: AppDatabase): GoalDao = db.goalDao()

    @Provides
    fun provideQuestProgressDao(db: AppDatabase): QuestProgressDao = db.questProgressDao()

    @Provides
    fun provideCalendarDao(db: AppDatabase): ru.finpet.kids.core.data.local.dao.CalendarDao = db.calendarDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindFinPetRepository(impl: FinPetRepositoryImpl): FinPetRepository
}
