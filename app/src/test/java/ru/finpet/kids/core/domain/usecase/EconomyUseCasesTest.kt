package ru.finpet.kids.core.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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

    // В тестах транзакция вырождается в обычное последовательное выполнение блока
    override suspend fun <T> inTransaction(block: suspend () -> T): T = block()

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

    override suspend fun getAvgClosedCompliance(): Double? =
        periods.values.filter { it.isPeriodClosed }
            .takeIf { it.isNotEmpty() }
            ?.map { it.compliancePercent }
            ?.average()

    override suspend fun getCategorySpending(category: String): Int =
        purchases.filter { it.category == category }.sumOf { it.price }

    override suspend fun getTotalSavedAmount(): Int = goals.values.sumOf { it.savedAmount }

    override suspend fun getCompletedQuestCount(): Int = quests.values.count { it.isCompleted }

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
    fun `ConfirmBudgetUseCase rejects negative values in budget plan`() = runTest {
        val plan = BudgetPlan(plannedMandatory = -10, plannedOptional = 50, plannedSavings = 50)
        val result = confirmBudgetUseCase(periodIndex = 1, plan = plan)

        assertTrue(result is BudgetValidationResult.NegativeValues)
    }

    @Test
    fun `ConfirmBudgetUseCase allows plan exactly matching available balance`() = runTest {
        val plan = BudgetPlan(plannedMandatory = 150, plannedOptional = 50, plannedSavings = 100) // Exactly 300
        val result = confirmBudgetUseCase(periodIndex = 1, plan = plan)

        assertTrue(result is BudgetValidationResult.Success)
        val savedPeriod = repository.getPeriodSync(1)
        assertTrue(savedPeriod?.isBudgetConfirmed == true)
        assertEquals(300, plan.totalPlanned)
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

        assertEquals(2, updatedProfile?.currentPeriodIndex) // Period 1 -> Period 2 (Tuesday)
        assertEquals(270, updatedProfile?.balance) // 300 - 30 + 0 (вторник: 0 монет) = 270
        assertEquals(0, resolution?.periodIncome)
        assertTrue(repository.getPeriodSync(1)?.isPeriodClosed == true)

        // Теперь симулируем завершение воскресенья (период 7 -> 8, понедельник)
        repository.saveProfile(updatedProfile!!.copy(currentPeriodIndex = 7))
        val mondayResolution = completePeriodUseCase()
        val mondayProfile = repository.getProfileSync()

        assertEquals(8, mondayProfile?.currentPeriodIndex) // Period 7 -> Period 8 (Monday)
        assertEquals(200, mondayResolution?.periodIncome) // Понедельник: +200 карманных денег
        assertEquals(470, mondayProfile?.balance) // 270 + 200 = 470
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
    fun `WithdrawFromGoalUseCase returns funds to balance, reduces current period actualSavings and records past savings in fact`() = runTest {
        repository.goals["goal_house"] = GoalEntity("goal_house", "Уютный домик", targetCost = 150, savedAmount = 50)
        // В текущем периоде было накоплено 20 монет
        repository.savePeriod(PeriodEntity(periodIndex = 1, actualSavings = 20))
        val withdrawUseCase = WithdrawFromGoalUseCase(repository)

        // Снимаем 30 монет: 20 с текущего периода, 10 из прошлых накоплений
        val result = withdrawUseCase("goal_house", 30)
        assertTrue(result is GoalWithdrawResult.Success)
        val success = result as GoalWithdrawResult.Success
        assertEquals(330, success.newBalance)
        assertEquals(20, success.newSaved)
        assertEquals(20, repository.getGoalByIdSync("goal_house")?.savedAmount)

        val updatedPeriod = repository.getPeriodSync(1)
        assertEquals(0, updatedPeriod?.actualSavings) // Текущая банка копилки опустела до 0 (не ушла в минус)
        assertEquals(10, updatedPeriod?.withdrawnSavings) // 10 монет зафиксировано в факте как снятие прошлых накоплений
    }

    @Test
    fun `MakePurchaseUseCase borrows from optional jar when mandatory budget shortage occurs`() = runTest {
        repository.savePeriod(
            PeriodEntity(
                periodIndex = 1,
                plannedMandatory = 30,
                plannedOptional = 50,
                actualMandatory = 10,
                actualOptional = 0,
                isBudgetConfirmed = true
            )
        )

        // Покупка еды за 30 монет. В банке "Обязательное" осталось 20 (30 - 10). Не хватает 10 монет.
        // Недостающие 10 монет должны быть взяты из банки "Желаемое".
        val result = makePurchaseUseCase(
            itemId = "food_premium",
            itemName = "Вкусный обед",
            category = "MANDATORY",
            price = 30,
            satietyBonus = 50
        )
        assertTrue(result is PurchaseResult.Success)

        val updatedPeriod = repository.getPeriodSync(1)
        assertEquals(30, updatedPeriod?.actualMandatory) // 10 + 20 (закрыли лимит обязательного)
        assertEquals(10, updatedPeriod?.actualOptional) // 10 монет взято из банки желаемого!
        assertEquals(10, updatedPeriod?.borrowedFromOptional) // зафиксировано взятие из желаемого
    }

    @Test
    fun `CompletePeriodUseCase triggers runaway when mood drops to 0 across consecutive missed feedings`() = runTest {
        repository.saveProfile(
            ProfileEntity(
                satiety = 100,
                health = 100,
                mood = 70,
                balance = 200,
                currentPeriodIndex = 1
            )
        )
        // День 1: еда не куплена
        completePeriodUseCase()
        val profDay1 = repository.getProfileSync()
        assertFalse(profDay1?.isPetRunaway == true)
        assertEquals(30, profDay1?.satiety)
        assertEquals(30, profDay1?.mood)

        // День 2: снова еда не куплена — настроение падает до 0% и кот убегает
        completePeriodUseCase()
        val profDay2 = repository.getProfileSync()
        assertTrue("Питомец должен убежать при падении настроения до 0%", profDay2?.isPetRunaway == true)
        assertEquals(0, profDay2?.mood)
    }

    @Test
    fun `CompletePeriodUseCase freezes pet growth while pet is runaway`() = runTest {
        repository.saveProfile(
            ProfileEntity(
                satiety = 25,
                health = 40,
                mood = 0,
                carePoints = 5,
                growthStage = "BABY",
                isPetRunaway = true,
                runawayDaysLeft = 2,
                balance = 200,
                currentPeriodIndex = 1
            )
        )

        val resolution = completePeriodUseCase()

        val updatedProfile = repository.getProfileSync()
        assertEquals("Очки заботы не должны увеличиваться пока кот сбежал", 5, updatedProfile?.carePoints)
        assertEquals("Стадия взросления не должна меняться пока кот сбежал", "BABY", updatedProfile?.growthStage)
        assertEquals("Сытость не должна восстанавливаться пока кот сбежал", 25, updatedProfile?.satiety)
        assertEquals("Здоровье не должно восстанавливаться пока кот сбежал", 40, updatedProfile?.health)
        assertEquals("Настроение не должно восстанавливаться пока кот сбежал", 0, updatedProfile?.mood)
        assertEquals(25, resolution?.newStats?.satiety)
        assertEquals(40, resolution?.newStats?.health)
        assertEquals(0, resolution?.newStats?.mood)
    }

    @Test
    fun `Pet mood drops to 0 after runaway and stays 0 while runaway`() = runTest {
        repository.saveProfile(
            ProfileEntity(
                satiety = 50,
                health = 60,
                mood = 40,
                isPetRunaway = true,
                runawayDaysLeft = 2,
                balance = 200,
                currentPeriodIndex = 1
            )
        )

        val resolution = completePeriodUseCase()
        val updatedProfile = repository.getProfileSync()

        assertEquals("Настроение кота в побеге должно падать в 0", 0, updatedProfile?.mood)
        assertEquals("Настроение в резолюции периода должно быть 0", 0, resolution?.newStats?.mood)
    }

    @Test
    fun `Pet stats restore only when runaway days end with food bowl placed`() = runTest {
        repository.saveProfile(
            ProfileEntity(
                satiety = 20,
                health = 30,
                mood = 0,
                isPetRunaway = true,
                runawayDaysLeft = 1,
                bowlPlacedToday = true,
                balance = 200,
                currentPeriodIndex = 1
            )
        )

        val resolution = completePeriodUseCase()

        val updatedProfile = repository.getProfileSync()
        assertFalse("Кот должен вернуться", updatedProfile?.isPetRunaway == true)
        assertEquals(80, updatedProfile?.satiety)
        assertEquals(70, updatedProfile?.health)
        assertEquals(60, updatedProfile?.mood)
        assertEquals(80, resolution?.newStats?.satiety)
        assertEquals(70, resolution?.newStats?.health)
        assertEquals(60, resolution?.newStats?.mood)
    }

    @Test
    fun `First period skip protects pet from runaway even if mood drops to 0`() = runTest {
        repository.saveProfile(
            ProfileEntity(
                satiety = 100,
                health = 100,
                mood = 10,
                balance = 200,
                currentPeriodIndex = 1,
                consecutivePeriodSkips = 0
            )
        )

        // Первый скип периода (isPeriodSkip = true)
        val resolution = completePeriodUseCase(isPeriodSkip = true)
        val updatedProfile = repository.getProfileSync()

        assertFalse("При первом скипе периода кот НЕ должен сбежать", updatedProfile?.isPetRunaway == true)
        assertEquals(1, updatedProfile?.consecutivePeriodSkips)
        assertEquals("При скипе без еды настроение падает до 0, но кот не сбегает с первого раза", 0, updatedProfile?.mood)
    }

    @Test
    fun `Consecutive period skip triggers runaway if mood was not raised by at least 1 percent`() = runTest {
        repository.saveProfile(
            ProfileEntity(
                satiety = 100,
                health = 100,
                mood = 20,
                balance = 200,
                currentPeriodIndex = 1,
                consecutivePeriodSkips = 0
            )
        )

        // Скип 1
        completePeriodUseCase(isPeriodSkip = true)
        val profAfterSkip1 = repository.getProfileSync()
        assertFalse(profAfterSkip1?.isPetRunaway == true)
        assertEquals(1, profAfterSkip1?.consecutivePeriodSkips)

        // Игрок НЕ поднимает настроение коту. Скип 2 (повторный):
        completePeriodUseCase(isPeriodSkip = true)
        val profAfterSkip2 = repository.getProfileSync()

        assertTrue("При повторном скипе без заботы кот должен сбежать", profAfterSkip2?.isPetRunaway == true)
        assertEquals(0, profAfterSkip2?.mood)
    }

    @Test
    fun `Consecutive period skip keeps pet safe if mood was raised by at least 1 percent`() = runTest {
        repository.saveProfile(
            ProfileEntity(
                satiety = 100,
                health = 100,
                mood = 20,
                balance = 200,
                currentPeriodIndex = 1,
                consecutivePeriodSkips = 0
            )
        )

        // Скип 1
        completePeriodUseCase(isPeriodSkip = true)
        val profAfterSkip1 = repository.getProfileSync()
        assertFalse(profAfterSkip1?.isPetRunaway == true)

        // Игрок гладит кота или кормит его — настроение поднимается на 5%
        repository.saveProfile(profAfterSkip1!!.copy(mood = profAfterSkip1.mood + 5))

        // Скип 2 (повторный, но забота была проявлена)
        completePeriodUseCase(isPeriodSkip = true)
        val profAfterSkip2 = repository.getProfileSync()

        assertFalse("Кот не должен сбежать, так как о нем позаботились и подняли настроение", profAfterSkip2?.isPetRunaway == true)
    }

    @Test
    fun `CompletePeriodUseCase preserves pet stats on Monday transition without resetting to 100`() = runTest {
        repository.saveProfile(
            ProfileEntity(
                satiety = 45,
                health = 75,
                mood = 55,
                balance = 200,
                currentPeriodIndex = 7
            )
        )
        // В закрываемом периоде были траты на обязательное (30)
        repository.savePeriod(
            PeriodEntity(
                periodIndex = 7,
                plannedMandatory = 30,
                actualMandatory = 30,
                isBudgetConfirmed = true
            )
        )

        val resolution = completePeriodUseCase(nextDayIndex = 8, isPeriodSkip = false)
        val updatedProfile = repository.getProfileSync()

        assertEquals("Сытость не должна сбрасываться до 100 в понедельник", 45, updatedProfile?.satiety)
        assertEquals("Здоровье не должно сбрасываться до 100 в понедельник", 75, updatedProfile?.health)
        assertEquals("Настроение не должно сбрасываться в понедельник", 55, updatedProfile?.mood)
        assertEquals(45, resolution?.newStats?.satiety)
        assertEquals(75, resolution?.newStats?.health)
        assertEquals(55, resolution?.newStats?.mood)
    }
}
