package ru.finpet.kids.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey val id: String = "default_player",
    val petName: String = "Финни",
    val petType: String = "CAT",             // CAT, DOG, DRAGON
    val bodyColor: Int = 0xFFFFA726.toInt(), // Orange
    val eyesType: Int = 1,                   // 1..3
    val accessoryId: String = "none",        // none, scarf, bow, cap
    val balance: Int = 50,                   // Стартовый баланс
    val currentPeriodIndex: Int = 1,         // 1..5+
    val carePoints: Int = 0,                 // Очки эволюции
    val growthStage: String = "BABY",        // BABY, TEEN, ADULT
    val satiety: Int = 100,                  // 0..100
    val health: Int = 100,                   // 0..100
    val mood: Int = 70,                      // 0..100
    val activeGoalId: String? = "goal_house",
    val isDemoMode: Boolean = false
)

@Entity(tableName = "periods")
data class PeriodEntity(
    @PrimaryKey val periodIndex: Int,
    val plannedMandatory: Int = 0,
    val plannedOptional: Int = 0,
    val plannedSavings: Int = 0,
    val actualMandatory: Int = 0,
    val actualOptional: Int = 0,
    val actualSavings: Int = 0,
    val isBudgetConfirmed: Boolean = false,
    val isPeriodClosed: Boolean = false,
    val compliancePercent: Int = 0,
    val carePointsEarned: Int = 0
)

@Entity(tableName = "purchases")
data class PurchaseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val periodIndex: Int,
    val itemId: String,
    val itemName: String,
    val category: String,                   // MANDATORY, OPTIONAL
    val price: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey val id: String,
    val title: String,
    val targetCost: Int,
    val savedAmount: Int = 0,
    val isReached: Boolean = false,
    val iconName: String = "ic_house"
)

@Entity(tableName = "quest_progress")
data class QuestProgressEntity(
    @PrimaryKey val questId: String,
    val isCompleted: Boolean = false,
    val selectedOptionId: String? = null,
    val rewardClaimed: Boolean = false,
    val completedInPeriod: Int = 0
)
