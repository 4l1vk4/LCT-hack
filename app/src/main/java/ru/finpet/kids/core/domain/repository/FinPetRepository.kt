package ru.finpet.kids.core.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.finpet.kids.core.data.local.entity.GoalEntity
import ru.finpet.kids.core.data.local.entity.PeriodEntity
import ru.finpet.kids.core.data.local.entity.ProfileEntity
import ru.finpet.kids.core.data.local.entity.PurchaseEntity
import ru.finpet.kids.core.data.local.entity.QuestProgressEntity

interface FinPetRepository {
    /**
     * Выполняет block в одной транзакции Room: все чтения и записи внутри
     * видят согласованный снапшот, а внешние подписчики получают ровно одну
     * волну инвалидации вместо отдельной на каждый DAO-вызов.
     */
    suspend fun <T> inTransaction(block: suspend () -> T): T

    fun getProfile(): Flow<ProfileEntity?>
    suspend fun getProfileSync(): ProfileEntity?
    suspend fun saveProfile(profile: ProfileEntity)
    suspend fun updateBalance(newBalance: Int)

    fun getPeriod(index: Int): Flow<PeriodEntity?>
    suspend fun getPeriodSync(index: Int): PeriodEntity?
    fun getAllPeriods(): Flow<List<PeriodEntity>>
    suspend fun savePeriod(period: PeriodEntity)

    fun getPurchasesForPeriod(periodIndex: Int): Flow<List<PurchaseEntity>>
    fun getAllPurchases(): Flow<List<PurchaseEntity>>
    suspend fun recordPurchase(purchase: PurchaseEntity)

    // Агрегаты SQL: отчёт считается одним запросом к таблице,
    // а не выборкой всей таблицы в память Kotlin.
    suspend fun getAvgClosedCompliance(): Double?
    suspend fun getCategorySpending(category: String): Int
    suspend fun getTotalSavedAmount(): Int
    suspend fun getCompletedQuestCount(): Int

    fun getAllGoals(): Flow<List<GoalEntity>>
    fun getGoalById(goalId: String): Flow<GoalEntity?>
    suspend fun getGoalByIdSync(goalId: String): GoalEntity?
    suspend fun updateGoal(goal: GoalEntity)
    suspend fun initDefaultGoals(goals: List<GoalEntity>)

    fun getQuestProgress(): Flow<List<QuestProgressEntity>>
    suspend fun saveQuestProgress(progress: QuestProgressEntity)

    suspend fun resetAllData()
}
