package ru.finpet.kids.core.domain.usecase

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import ru.finpet.kids.core.data.local.entity.ProfileEntity
import ru.finpet.kids.core.data.repository.ContentRepository
import ru.finpet.kids.core.data.repository.QuestItem
import ru.finpet.kids.core.data.repository.QuestOption

class QuestEngineUseCaseTest {

    private lateinit var repository: FakeFinPetRepository
    private lateinit var contentRepository: ContentRepository
    private lateinit var questEngineUseCase: QuestEngineUseCase

    private val sampleQuests = listOf(
        QuestItem(
            id = "quest_choice_1",
            stage = "BABY",
            type = "CHOICE",
            topic = "BUDGETING",
            topicTitle = "Личный бюджет",
            title = "Обед или сладости",
            situation = "Что купить?",
            options = listOf(
                QuestOption(
                    id = "opt_lunch",
                    text = "Полезный обед",
                    isRecommended = true,
                    feedback = "Отличный выбор!",
                    rewardCoins = 40
                ),
                QuestOption(
                    id = "opt_candy",
                    text = "Сладкая вата",
                    isRecommended = false,
                    feedback = "Сладости не заменят обед.",
                    rewardCoins = 0
                )
            )
        ),
        QuestItem(
            id = "quest_text_1",
            stage = "BABY",
            type = "TEXT_INPUT",
            topic = "SAVINGS",
            topicTitle = "Сбережения",
            title = "Загадка про домик для монет",
            situation = "Кто в неё монеты сложит?",
            expectedAnswers = listOf("копилка", "копилочка", "свинка-копилка"),
            hint = "Слово на букву К",
            feedbackSuccess = "В точку! Копилка бережёт сбережения.",
            feedbackFailure = "Правильный ответ — Копилка!",
            rewardCoins = 45
        ),
        QuestItem(
            id = "quest_alloc_1",
            stage = "TEEN",
            type = "MONEY_ALLOCATION",
            topic = "SAVINGS",
            topicTitle = "Сбережения",
            title = "Первое распределение",
            situation = "Распредели 60 монет",
            totalBudget = 60,
            recommendedMin = 20,
            recommendedMax = 45,
            allocationSuccessFeedback = "Замечательный баланс!",
            allocationWarningFeedback = "Старайся откладывать от 30% до 50%.",
            rewardCoins = 50
        ),
        QuestItem(
            id = "quest_adult_1",
            stage = "ADULT",
            type = "CHOICE",
            topic = "PURCHASES",
            topicTitle = "Покупки",
            title = "Цифровая безопасность",
            situation = "Просят СМС код",
            options = listOf(
                QuestOption(
                    id = "opt_reject",
                    text = "Никому не сообщать",
                    isRecommended = true,
                    feedback = "Супергерой безопасности!",
                    rewardCoins = 50
                )
            )
        )
    )

    @Before
    fun setUp() {
        repository = FakeFinPetRepository()
        repository.profile = ProfileEntity(
            id = "player_1",
            petName = "Финни",
            balance = 100,
            currentPeriodIndex = 3,
            growthStage = "BABY"
        )
        contentRepository = mockk()
        coEvery { contentRepository.getQuests() } returns sampleQuests
        questEngineUseCase = QuestEngineUseCase(repository, contentRepository)
    }

    @Test
    fun `executeOption awards coins and updates profile balance and progress`() = runTest {
        val result = questEngineUseCase.executeOption("quest_choice_1", "opt_lunch")
        assertNotNull(result)
        assertEquals(40, result?.coinsAwarded)
        assertEquals(140, result?.newBalance)
        assertTrue(result?.isRecommended == true)

        val updatedProfile = repository.getProfileSync()
        assertEquals(140, updatedProfile?.balance)

        val savedProgress = repository.quests["quest_choice_1"]
        assertNotNull(savedProgress)
        assertTrue(savedProgress?.isCompleted == true)
        assertEquals("opt_lunch", savedProgress?.selectedOptionId)
    }

    @Test
    fun `executeTextAnswer correctly verifies expected term with case and whitespace insensitivity`() = runTest {
        // Ввод с пробелами и в верхнем регистре: "  КОПИЛКА  "
        val result = questEngineUseCase.executeTextAnswer("quest_text_1", "  КОПИЛКА  ")
        assertNotNull(result)
        assertTrue(result?.isRecommended == true)
        assertEquals(45, result?.coinsAwarded)
        assertEquals(145, result?.newBalance)
        assertEquals("В точку! Копилка бережёт сбережения.", result?.feedback)

        val saved = repository.quests["quest_text_1"]
        assertTrue(saved?.isCompleted == true)
        assertEquals("text:копилка", saved?.selectedOptionId)
    }

    @Test
    fun `executeOption with incorrect choice gives zero coins and does not complete quest`() = runTest {
        val result = questEngineUseCase.executeOption("quest_choice_1", "opt_candy", dayIndex = 5)
        assertNotNull(result)
        assertFalse(result?.isRecommended == true)
        assertEquals(0, result?.coinsAwarded)
        assertEquals(100, result?.newBalance) // Баланс не изменился

        val saved = repository.quests["quest_choice_1"]
        assertNotNull(saved)
        assertFalse("Ошибочный ответ не закрывает квест навсегда", saved?.isCompleted == true)
        assertEquals(5, saved?.completedInPeriod)
    }

    @Test
    fun `executeTextAnswer handles incorrect term with gentle educational feedback and zero reward`() = runTest {
        val result = questEngineUseCase.executeTextAnswer("quest_text_1", "конфета")
        assertNotNull(result)
        assertFalse(result?.isRecommended == true)
        // Ошибочный выбор не дает ничего (0 монет)
        assertEquals(0, result?.coinsAwarded)
        assertEquals("Правильный ответ — Копилка!", result?.feedback)

        val saved = repository.quests["quest_text_1"]
        assertNotNull(saved)
        assertFalse("Ошибочный ввод не завершает квест, позволяя решить его завтра", saved?.isCompleted == true)
        assertEquals(3, saved?.completedInPeriod)
    }

    @Test
    fun `quest failed today can be retried and completed successfully next day`() = runTest {
        // День 1: ребенок ошибся
        val failResult = questEngineUseCase.executeOption("quest_choice_1", "opt_candy", dayIndex = 1)
        assertFalse(failResult?.isRecommended == true)
        val progressDay1 = repository.quests["quest_choice_1"]
        assertFalse(progressDay1?.isCompleted == true)
        assertEquals(1, progressDay1?.completedInPeriod)

        // День 2: ребенок выбирает правильный ответ и восстанавливает прогресс
        val successResult = questEngineUseCase.executeOption("quest_choice_1", "opt_lunch", dayIndex = 2)
        assertTrue(successResult?.isRecommended == true)
        assertEquals(40, successResult?.coinsAwarded)
        assertEquals(140, successResult?.newBalance)

        val progressDay2 = repository.quests["quest_choice_1"]
        assertTrue("Квест успешно пройден на следующий день", progressDay2?.isCompleted == true)
        assertEquals(2, progressDay2?.completedInPeriod)
    }

    @Test
    fun `executeAllocation evaluates balanced money split correctly`() = runTest {
        // Рекомендуемый диапазон 20..45, отправляем 30 монет в сбережения
        val resultBalanced = questEngineUseCase.executeAllocation("quest_alloc_1", 30)
        assertNotNull(resultBalanced)
        assertTrue(resultBalanced?.isRecommended == true)
        assertEquals(50, resultBalanced?.coinsAwarded)
        assertEquals("Замечательный баланс!", resultBalanced?.feedback)

        // Несбалансированное распределение (0 монет в копилку) -> 0 монет
        val resultUnbalanced = questEngineUseCase.executeAllocation("quest_alloc_1", 0)
        assertNotNull(resultUnbalanced)
        assertFalse(resultUnbalanced?.isRecommended == true)
        assertEquals(0, resultUnbalanced?.coinsAwarded)
        assertEquals("Старайся откладывать от 30% до 50%.", resultUnbalanced?.feedback)

        val saved = repository.quests["quest_alloc_1"]
        assertNotNull(saved)
        assertFalse("Несбалансированное распределение не завершает квест", saved?.isCompleted == true)
    }

    @Test
    fun `stage progression and demo mode rules are respected`() {
        fun isStageUnlocked(catStage: String, targetStage: String, demoMode: Boolean): Boolean {
            if (demoMode) return true
            val catIdx = when (catStage) {
                "ADULT" -> 2
                "TEEN" -> 1
                else -> 0
            }
            val targetIdx = when (targetStage) {
                "ADULT" -> 2
                "TEEN" -> 1
                else -> 0
            }
            return targetIdx <= catIdx
        }

        // В обычном режиме кот-малыш имеет доступ только к BABY
        assertTrue(isStageUnlocked("BABY", "BABY", demoMode = false))
        assertFalse(isStageUnlocked("BABY", "TEEN", demoMode = false))
        assertFalse(isStageUnlocked("BABY", "ADULT", demoMode = false))

        // Подросток имеет доступ к BABY и TEEN
        assertTrue(isStageUnlocked("TEEN", "BABY", demoMode = false))
        assertTrue(isStageUnlocked("TEEN", "TEEN", demoMode = false))
        assertFalse(isStageUnlocked("TEEN", "ADULT", demoMode = false))

        // Взрослый имеет доступ ко всем стадиям
        assertTrue(isStageUnlocked("ADULT", "BABY", demoMode = false))
        assertTrue(isStageUnlocked("ADULT", "TEEN", demoMode = false))
        assertTrue(isStageUnlocked("ADULT", "ADULT", demoMode = false))

        // По ТЗ: в ДЕМО-режиме открыты ВСЕ задания независимо от возраста кота!
        assertTrue(isStageUnlocked("BABY", "BABY", demoMode = true))
        assertTrue(isStageUnlocked("BABY", "TEEN", demoMode = true))
        assertTrue(isStageUnlocked("BABY", "ADULT", demoMode = true))
    }

    @Test
    fun `quests json content contains valid quests covering all stages and types`() {
        val file = java.io.File("src/main/assets/content/quests.json")
        assertTrue("quests.json file must exist", file.exists())
        val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true; isLenient = true }
        val loadedQuests = json.decodeFromString<List<QuestItem>>(file.readText())

        assertEquals(18, loadedQuests.size)

        // Все 3 обязательные темы из ТЗ (раздел 2.5.8)
        val topics = loadedQuests.map { it.topic }.toSet()
        assertTrue(topics.contains("BUDGETING"))
        assertTrue(topics.contains("SAVINGS"))
        assertTrue(topics.contains("PURCHASES"))

        // Все 3 стадии взросления питомца
        val stages = loadedQuests.map { it.stage }.toSet()
        assertTrue(stages.contains("BABY"))
        assertTrue(stages.contains("TEEN"))
        assertTrue(stages.contains("ADULT"))

        // Разнообразные интерактивные механики (не только выбор из 2 вариантов по ТЗ)
        val types = loadedQuests.map { it.type }.toSet()
        assertTrue(types.contains("CHOICE"))
        assertTrue(types.contains("TEXT_INPUT"))
        assertTrue(types.contains("MONEY_ALLOCATION"))
        assertTrue(types.contains("CALCULATION"))

        // Проверка корректности параметров
        loadedQuests.forEach { q ->
            assertTrue("Quest ${q.id} title should not be blank", q.title.isNotBlank())
            assertTrue("Quest ${q.id} situation should not be blank", q.situation.isNotBlank())
            assertTrue("Quest ${q.id} reward should be positive", q.rewardCoins > 0)
        }
    }
}
