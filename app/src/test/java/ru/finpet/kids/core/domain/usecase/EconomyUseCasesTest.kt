package ru.finpet.kids.core.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import ru.finpet.kids.core.data.local.entity.GoalEntity
import ru.finpet.kids.core.data.local.entity.PeriodEntity
import ru.finpet.kids.core.data.local.entity.ProfileEntity
import ru.finpet.kids.core.data.local.entity.PurchaseEntity
import ru.finpet.kids.core.data.local.entity.QuestProgressEntity
import ru.finpet.kids.core.domain.model.BudgetPlan
import ru.finpet.kids.core.domain.repository.FinPetRepository

class FakeFinPetRepository : FinPetRepository {
    var profile: ProfileEntity? = ProfileEntity(
        id = "default_player",
        balance = 300,
        currentPeriodIndex = 1,
        satiety = 60,
        mood = 60,
        health = 60
    )
    val periods = mutableMapOf<Int, PeriodEntity>()
    val purchases = mutableListOf<PurchaseEntity>()
    val goals = mutableMapOf<String, GoalEntity>()
    val quests = mutableMapOf<String, QuestProgressEntity>()

    override fun getProfile(): Flow<ProfileEntity?> = flowOf(profile)
    override suspend fun getProfileSync(): ProfileEntity? = profile
    override suspend fun saveProfile(profile: ProfileEntity) {
        this.profile = profile
    }
    override suspend fun updateBalance(newBalance: Int) {
        profile = profile?.copy(balance = newBalance)
    }

    override fun getPeriod(index: Int): Flow<PeriodEntity?> = flowOf(periods[index])
    override suspend fun getPeriodSync(index: Int): PeriodEntity? = periods[index]
    override fun getAllPeriods(): Flow<List<PeriodEntity>> = flowOf(periods.values.toList())
    override suspend fun savePeriod(period: PeriodEntity) {
        periods[period.periodIndex] = period
    }

    override fun getPurchasesForPeriod(periodIndex: Int): Flow<List<PurchaseEntity>> =
        flowOf(purchases.filter { it.periodIndex == periodIndex })
    override fun getAllPurchases(): Flow<List<PurchaseEntity>> = flowOf(purchases.toList())

    override suspend fun recordPurchase(purchase: PurchaseEntity) {
        purchases.add(purchase)
    }

    override fun getAllGoals(): Flow<List<GoalEntity>> = flowOf(goals.values.toList())
    override fun getGoalById(goalId: String): Flow<GoalEntity?> = flowOf(goals[goalId])
    override suspend fun getGoalByIdSync(goalId: String): GoalEntity? = goals[goalId]
    override suspend fun updateGoal(goal: GoalEntity) {
        goals[goal.id] = goal
    }
    override suspend fun initDefaultGoals(goals: List<GoalEntity>) {
        goals.forEach { this.goals[it.id] = it }
    }

    override fun getQuestProgress(): Flow<List<QuestProgressEntity>> = flowOf(quests.values.toList())
    override suspend fun saveQuestProgress(progress: QuestProgressEntity) {
        quests[progress.questId] = progress
    }

    override suspend fun resetAllData() {
        profile = null
        periods.clear()
        purchases.clear()
        goals.clear()
        quests.clear()
    }
}

class EconomyUseCasesTest {

    private lateinit var repository: FakeFinPetRepository
    private lateinit var confirmBudgetUseCase: ConfirmBudgetUseCase
    private lateinit var makePurchaseUseCase: MakePurchaseUseCase
    private lateinit var depositToGoalUseCase: DepositToGoalUseCase
    private lateinit var completePeriodUseCase: CompletePeriodUseCase

    @Before
    fun setUp() {
        repository = FakeFinPetRepository()
        confirmBudgetUseCase = ConfirmBudgetUseCase(repository)
        makePurchaseUseCase = MakePurchaseUseCase(repository)
        depositToGoalUseCase = DepositToGoalUseCase(repository)
        completePeriodUseCase = CompletePeriodUseCase(repository)
    }

    @Test
    fun `ConfirmBudgetUseCase succeeds when plan is within balance`() = runTest {
        val plan = BudgetPlan(plannedMandatory = 50, plannedOptional = 50, plannedSavings = 50)
        val result = confirmBudgetUseCase(periodIndex = 1, plan = plan)

        assertTrue(result is BudgetValidationResult.Success)
        val savedPeriod = repository.getPeriodSync(1)
        assertEquals(50, savedPeriod?.plannedMandatory)
        assertTrue(savedPeriod?.isBudgetConfirmed == true)
    }

    @Test
    fun `ConfirmBudgetUseCase rejects plan when total exceeds balance`() = runTest {
        val plan = BudgetPlan(plannedMandatory = 200, plannedOptional = 100, plannedSavings = 100) // 400 > 300
        val result = confirmBudgetUseCase(periodIndex = 1, plan = plan)

        assertTrue(result is BudgetValidationResult.ExceedsBalance)
        assertEquals(400, (result as BudgetValidationResult.ExceedsBalance).planned)
    }

    @Test
    fun `MakePurchaseUseCase succeeds and deducts balance when funds are sufficient`() = runTest {
        val result = makePurchaseUseCase(
            itemId = "food_basic",
            itemName = "Сытный обед",
            category = "MANDATORY",
            price = 30,
            satietyBonus = 40
        )

        assertTrue(result is PurchaseResult.Success)
        assertEquals(270, (result as PurchaseResult.Success).newBalance)
        assertEquals(270, repository.getProfileSync()?.balance)
        assertEquals(100, repository.getProfileSync()?.satiety) // 60 + 40 = 100
        assertEquals(1, repository.purchases.size)
    }

    @Test
    fun `MakePurchaseUseCase returns NotEnoughMoney when balance is low`() = runTest {
        repository.updateBalance(20) // Only 20 coins
        val result = makePurchaseUseCase(
            itemId = "toy_ball",
            itemName = "Яркий мячик",
            category = "OPTIONAL",
            price = 50
        )

        assertTrue(result is PurchaseResult.NotEnoughMoney)
        assertEquals(30, (result as PurchaseResult.NotEnoughMoney).missingCoins)
        assertEquals(20, repository.getProfileSync()?.balance) // Balance untouched
    }

    @Test
    fun `CompletePeriodUseCase closes period, advances day and awards income`() = runTest {
        // Buy food in period 1
        makePurchaseUseCase(
            itemId = "food_basic",
            itemName = "Сытный обед",
            category = "MANDATORY",
            price = 30
        )

        val resolution = completePeriodUseCase()
        assertTrue(resolution != null)
        val updatedProfile = repository.getProfileSync()

        assertEquals(2, updatedProfile?.currentPeriodIndex) // Period 1 -> Period 2
        assertEquals(370, updatedProfile?.balance) // 300 - 30 + 100 = 370
        assertTrue(repository.getPeriodSync(1)?.isPeriodClosed == true)
    }

    @Test
    fun `DepositToGoalUseCase deducts balance and updates saved amount`() = runTest {
        repository.goals["goal_house"] = GoalEntity("goal_house", "Уютный домик", targetCost = 150, savedAmount = 0)
        
        val result = depositToGoalUseCase("goal_house", 50)
        assertTrue(result is GoalDepositResult.Success)
        val success = result as GoalDepositResult.Success
        assertEquals(250, success.newBalance)
        assertEquals(50, success.newSaved)
        assertEquals(50, repository.getGoalByIdSync("goal_house")?.savedAmount)
    }

    @Test
    fun `WithdrawFromGoalUseCase returns funds to balance and reduces savings`() = runTest {
        repository.goals["goal_house"] = GoalEntity("goal_house", "Уютный домик", targetCost = 150, savedAmount = 50)
        val withdrawUseCase = WithdrawFromGoalUseCase(repository)

        val result = withdrawUseCase("goal_house", 30)
        assertTrue(result is GoalWithdrawResult.Success)
        val success = result as GoalWithdrawResult.Success
        assertEquals(330, success.newBalance)
        assertEquals(20, success.newSaved)
        assertEquals(20, repository.getGoalByIdSync("goal_house")?.savedAmount)
    }
}
