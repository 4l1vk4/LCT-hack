package ru.finpet.kids.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ru.finpet.kids.core.data.local.entity.GoalEntity
import ru.finpet.kids.core.data.local.entity.PeriodEntity
import ru.finpet.kids.core.data.local.entity.ProfileEntity
import ru.finpet.kids.core.data.local.entity.PurchaseEntity
import ru.finpet.kids.core.data.local.entity.QuestProgressEntity

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles WHERE id = :id LIMIT 1")
    fun getProfile(id: String = "default_player"): Flow<ProfileEntity?>

    @Query("SELECT * FROM profiles WHERE id = :id LIMIT 1")
    suspend fun getProfileSync(id: String = "default_player"): ProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(profile: ProfileEntity)

    @Query("UPDATE profiles SET balance = :newBalance WHERE id = :id")
    suspend fun updateBalance(newBalance: Int, id: String = "default_player")

    @Query("""
        UPDATE profiles 
        SET satiety = :satiety, health = :health, mood = :mood, carePoints = :carePoints, growthStage = :growthStage
        WHERE id = :id
    """)
    suspend fun updateStats(
        satiety: Int,
        health: Int,
        mood: Int,
        carePoints: Int,
        growthStage: String,
        id: String = "default_player"
    )

    @Query("DELETE FROM profiles")
    suspend fun clear()
}

@Dao
interface PeriodDao {
    @Query("SELECT * FROM periods WHERE periodIndex = :index LIMIT 1")
    fun getPeriod(index: Int): Flow<PeriodEntity?>

    @Query("SELECT * FROM periods WHERE periodIndex = :index LIMIT 1")
    suspend fun getPeriodSync(index: Int): PeriodEntity?

    @Query("SELECT * FROM periods ORDER BY periodIndex ASC")
    fun getAllPeriods(): Flow<List<PeriodEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(period: PeriodEntity)

    @Query("DELETE FROM periods")
    suspend fun clear()
}

@Dao
interface PurchaseDao {
    @Query("SELECT * FROM purchases WHERE periodIndex = :periodIndex ORDER BY timestamp DESC")
    fun getPurchasesForPeriod(periodIndex: Int): Flow<List<PurchaseEntity>>

    @Query("SELECT * FROM purchases ORDER BY timestamp DESC")
    fun getAllPurchases(): Flow<List<PurchaseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: PurchaseEntity)

    @Query("DELETE FROM purchases")
    suspend fun clear()
}

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals")
    fun getAllGoals(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE id = :id LIMIT 1")
    fun getGoalById(id: String): Flow<GoalEntity?>

    @Query("SELECT * FROM goals WHERE id = :id LIMIT 1")
    suspend fun getGoalByIdSync(id: String): GoalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoals(goals: List<GoalEntity>)

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Query("DELETE FROM goals")
    suspend fun clear()
}

@Dao
interface QuestProgressDao {
    @Query("SELECT * FROM quest_progress")
    fun getAllProgress(): Flow<List<QuestProgressEntity>>

    @Query("SELECT * FROM quest_progress WHERE questId = :questId LIMIT 1")
    fun getProgress(questId: String): Flow<QuestProgressEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(progress: QuestProgressEntity)

    @Query("DELETE FROM quest_progress")
    suspend fun clear()
}
