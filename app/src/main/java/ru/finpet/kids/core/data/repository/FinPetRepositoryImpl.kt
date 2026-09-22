package ru.finpet.kids.core.data.repository

import kotlinx.coroutines.flow.Flow
import ru.finpet.kids.core.data.local.dao.GoalDao
import ru.finpet.kids.core.data.local.dao.PeriodDao
import ru.finpet.kids.core.data.local.dao.ProfileDao
import ru.finpet.kids.core.data.local.dao.PurchaseDao
import ru.finpet.kids.core.data.local.dao.QuestProgressDao
import ru.finpet.kids.core.data.local.entity.GoalEntity
import ru.finpet.kids.core.data.local.entity.PeriodEntity
import ru.finpet.kids.core.data.local.entity.ProfileEntity
import ru.finpet.kids.core.data.local.entity.PurchaseEntity
import ru.finpet.kids.core.data.local.entity.QuestProgressEntity
import ru.finpet.kids.core.domain.repository.FinPetRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinPetRepositoryImpl @Inject constructor(
    private val profileDao: ProfileDao,
    private val periodDao: PeriodDao,
    private val purchaseDao: PurchaseDao,
    private val goalDao: GoalDao,
    private val questProgressDao: QuestProgressDao
) : FinPetRepository {

    override fun getProfile(): Flow<ProfileEntity?> = profileDao.getProfile()

    override suspend fun getProfileSync(): ProfileEntity? = profileDao.getProfileSync()

    override suspend fun saveProfile(profile: ProfileEntity) {
        profileDao.insertOrUpdate(profile)
    }

    override suspend fun updateBalance(newBalance: Int) {
        profileDao.updateBalance(newBalance)
    }

    override fun getPeriod(index: Int): Flow<PeriodEntity?> = periodDao.getPeriod(index)

    override suspend fun getPeriodSync(index: Int): PeriodEntity? = periodDao.getPeriodSync(index)

    override fun getAllPeriods(): Flow<List<PeriodEntity>> = periodDao.getAllPeriods()

    override suspend fun savePeriod(period: PeriodEntity) {
        periodDao.insertOrUpdate(period)
    }

    override fun getPurchasesForPeriod(periodIndex: Int): Flow<List<PurchaseEntity>> =
        purchaseDao.getPurchasesForPeriod(periodIndex)

    override fun getAllPurchases(): Flow<List<PurchaseEntity>> =
        purchaseDao.getAllPurchases()

    override suspend fun recordPurchase(purchase: PurchaseEntity) {
        purchaseDao.insertPurchase(purchase)
    }

    override fun getAllGoals(): Flow<List<GoalEntity>> = goalDao.getAllGoals()

    override fun getGoalById(goalId: String): Flow<GoalEntity?> = goalDao.getGoalById(goalId)

    override suspend fun getGoalByIdSync(goalId: String): GoalEntity? = goalDao.getGoalByIdSync(goalId)

    override suspend fun updateGoal(goal: GoalEntity) {
        goalDao.updateGoal(goal)
    }

    override suspend fun initDefaultGoals(goals: List<GoalEntity>) {
        goalDao.insertGoals(goals)
    }

    override fun getQuestProgress(): Flow<List<QuestProgressEntity>> =
        questProgressDao.getAllProgress()

    override suspend fun saveQuestProgress(progress: QuestProgressEntity) {
        questProgressDao.insertOrUpdate(progress)
    }

    override suspend fun resetAllData() {
        profileDao.clear()
        periodDao.clear()
        purchaseDao.clear()
        goalDao.clear()
        questProgressDao.clear()
    }
}
