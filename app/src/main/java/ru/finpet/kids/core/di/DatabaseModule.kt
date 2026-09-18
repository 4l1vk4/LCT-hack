package ru.finpet.kids.core.di

import android.content.Context
import androidx.room.Room
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

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration().build()
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
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindFinPetRepository(impl: FinPetRepositoryImpl): FinPetRepository
}
