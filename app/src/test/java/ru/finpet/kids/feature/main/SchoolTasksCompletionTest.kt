package ru.finpet.kids.feature.main

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.finpet.kids.core.data.local.entity.CalendarNoteEntity
import ru.finpet.kids.core.data.local.entity.QuestProgressEntity
import ru.finpet.kids.core.data.repository.QuestItem

class SchoolTasksCompletionTest {

    private fun createQuest(id: String, stage: String): QuestItem =
        QuestItem(
            id = id,
            topic = "FIN_LITERACY",
            topicTitle = "Грамотность",
            title = "Урок $id",
            situation = "Ситуация $id",
            stage = stage
        )

    private fun checkAllAvailableQuestsDone(
        quests: List<QuestItem>,
        questProgress: List<QuestProgressEntity>,
        growthStage: String,
        demoMode: Boolean
    ): Boolean {
        if (quests.isEmpty()) return false
        val currentStageIndex = when (growthStage) {
            "ADULT" -> 2
            "TEEN" -> 1
            else -> 0
        }
        val available = quests.filter { q ->
            if (demoMode) true else {
                val targetIndex = when (q.stage) {
                    "ADULT" -> 2
                    "TEEN" -> 1
                    else -> 0
                }
                targetIndex <= currentStageIndex
            }
        }
        return available.isNotEmpty() && available.all { q ->
            questProgress.any { p -> p.questId == q.id && p.isCompleted }
        }
    }

    @Test
    fun `when some quests are pending, allSchoolQuestsDone is false and school note remains in todayNotes`() {
        val quests = listOf(
            createQuest("q1", "BABY"),
            createQuest("q2", "BABY")
        )
        val progress = listOf(
            QuestProgressEntity(questId = "q1", isCompleted = true, completedInPeriod = 1)
        )

        val allDone = checkAllAvailableQuestsDone(quests, progress, growthStage = "BABY", demoMode = false)
        assertFalse(allDone)

        val notes = listOf(
            CalendarNoteEntity(id = 1, dayIndex = 5, title = "Сделать 2 урока в Школе", cost = 0, category = "SCHOOL", isCompleted = false),
            CalendarNoteEntity(id = 2, dayIndex = 5, title = "Купить корм для питомца", cost = 30, category = "PET_FOOD", isCompleted = false)
        )

        val todayNotes = notes.filter { note ->
            note.dayIndex == 5 && !(note.category == "SCHOOL" && allDone)
        }

        assertEquals(2, todayNotes.size)
        assertTrue(todayNotes.any { it.category == "SCHOOL" })
    }

    @Test
    fun `when all available quests are completed, allSchoolQuestsDone is true and school note is excluded from todayNotes`() {
        val quests = listOf(
            createQuest("q1", "BABY"),
            createQuest("q2", "BABY"),
            createQuest("q3", "TEEN")
        )
        val progress = listOf(
            QuestProgressEntity(questId = "q1", isCompleted = true, completedInPeriod = 1),
            QuestProgressEntity(questId = "q2", isCompleted = true, completedInPeriod = 1)
        )

        // For BABY stage, only q1 and q2 are available and both are completed
        val allDoneBaby = checkAllAvailableQuestsDone(quests, progress, growthStage = "BABY", demoMode = false)
        assertTrue(allDoneBaby)

        val notes = listOf(
            CalendarNoteEntity(id = 1, dayIndex = 5, title = "Сделать 2 урока в Школе", cost = 0, category = "SCHOOL", isCompleted = false),
            CalendarNoteEntity(id = 2, dayIndex = 5, title = "Купить корм для питомца", cost = 30, category = "PET_FOOD", isCompleted = false)
        )

        val todayNotes = notes.filter { note ->
            note.dayIndex == 5 && !(note.category == "SCHOOL" && allDoneBaby)
        }

        assertEquals(1, todayNotes.size)
        assertFalse(todayNotes.any { it.category == "SCHOOL" })
        assertEquals("PET_FOOD", todayNotes.first().category)
    }

    @Test
    fun `when pet grows to TEEN, new quests unlock and school note reappears until TEEN quests are also completed`() {
        val quests = listOf(
            createQuest("q1", "BABY"),
            createQuest("q2", "BABY"),
            createQuest("q3", "TEEN")
        )
        val progress = listOf(
            QuestProgressEntity(questId = "q1", isCompleted = true, completedInPeriod = 1),
            QuestProgressEntity(questId = "q2", isCompleted = true, completedInPeriod = 1)
        )

        // For TEEN stage, q3 is now available and not completed
        val allDoneTeen = checkAllAvailableQuestsDone(quests, progress, growthStage = "TEEN", demoMode = false)
        assertFalse(allDoneTeen)

        val notes = listOf(
            CalendarNoteEntity(id = 1, dayIndex = 5, title = "Сделать 2 урока в Школе", cost = 0, category = "SCHOOL", isCompleted = false)
        )

        val todayNotes = notes.filter { note ->
            note.dayIndex == 5 && !(note.category == "SCHOOL" && allDoneTeen)
        }

        assertEquals(1, todayNotes.size)
        assertTrue(todayNotes.any { it.category == "SCHOOL" })
    }
}
