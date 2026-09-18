package ru.finpet.kids.core.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.finpet.kids.core.data.local.entity.GoalEntity
import ru.finpet.kids.core.data.local.entity.PeriodEntity
import ru.finpet.kids.core.data.local.entity.ProfileEntity
import ru.finpet.kids.core.data.local.entity.PurchaseEntity
import ru.finpet.kids.core.data.local.entity.QuestProgressEntity

interface FinPetRepository {
    fun getProfile(): Flow<ProfileEntity?>
    suspend fun getProfileSync(): ProfileEntity?
    suspend fun saveProfile(profile: ProfileEntity)
    suspend fun updateBalance(newBalance: Int)

    fun getPeriod(index: Int): Flow<PeriodEntity?>
    suspend fun getPeriodSync(index: Int): PeriodEntity?
    suspend fun savePeriod(period: PeriodEntity)

    fun getPurchasesForPeriod(periodIndex: Int): Flow<List<PurchaseEntity>>
    suspend fun recordPurchase(purchase: PurchaseEntity)

    fun getAllGoals(): Flow<List<GoalEntity>>
    fun getGoalById(goalId: String): Flow<GoalEntity?>
    suspend fun updateGoal(goal: GoalEntity)
    suspend fun initDefaultGoals(goals: List<GoalEntity>)

    fun getQuestProgress(): Flow<List<QuestProgressEntity>>
    suspend fun saveQuestProgress(progress: QuestProgressEntity)

    suspend fun resetAllData()
}
