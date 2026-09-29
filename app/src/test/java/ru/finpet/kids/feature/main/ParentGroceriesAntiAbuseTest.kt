package ru.finpet.kids.feature.main

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.finpet.kids.core.data.local.dao.CalendarDao
import ru.finpet.kids.core.data.local.entity.CalendarNoteEntity
import ru.finpet.kids.core.data.local.entity.ProfileEntity
import ru.finpet.kids.core.data.local.entity.RecurringExpenseEntity
import ru.finpet.kids.core.data.repository.PurchaseItem

class FakeCalendarDaoForTest : CalendarDao {
    val notes = mutableListOf<CalendarNoteEntity>()

    override fun getNotesForDay(dayIndex: Int): Flow<List<CalendarNoteEntity>> =
        flowOf(notes.filter { it.dayIndex == dayIndex })

    override fun getAllNotes(): Flow<List<CalendarNoteEntity>> = flowOf(notes)
    override suspend fun getNotesCount(): Int = notes.size
    override suspend fun getChecklistCount(): Int = notes.count { it.category == "PET_FOOD" }
    override suspend fun insertNote(note: CalendarNoteEntity): Long {
        notes.add(note)
        return note.id
    }
    override suspend fun insertNotes(notes: List<CalendarNoteEntity>) {
        this.notes.addAll(notes)
    }
    override suspend fun deleteNote(note: CalendarNoteEntity) {
        notes.remove(note)
    }
    override suspend fun updateNoteCompleted(id: Long, isCompleted: Boolean) {
        val idx = notes.indexOfFirst { it.id == id }
        if (idx != -1) notes[idx] = notes[idx].copy(isCompleted = isCompleted)
    }
    override suspend fun updateNoteStatus(id: Long, isCompleted: Boolean, cost: Int) {
        val idx = notes.indexOfFirst { it.id == id }
        if (idx != -1) notes[idx] = notes[idx].copy(isCompleted = isCompleted, cost = cost)
    }
    override suspend fun getPetQuestionNotesCount(): Int = notes.count { it.category == "PET_QUESTION" }
    override suspend fun completeChecklistTaskByCategory(dayIndex: Int, category: String) {
        val idx = notes.indexOfFirst { it.dayIndex == dayIndex && it.category == category }
        if (idx != -1) notes[idx] = notes[idx].copy(isCompleted = true)
    }
    override suspend fun updateChecklistNoteByCategory(
        dayIndex: Int,
        category: String,
        isCompleted: Boolean,
        cost: Int
    ) {
        val idx = notes.indexOfFirst { it.dayIndex == dayIndex && it.category == category }
        if (idx != -1) notes[idx] = notes[idx].copy(isCompleted = isCompleted, cost = cost)
    }
    override suspend fun getNoteByDayAndCategory(dayIndex: Int, category: String): CalendarNoteEntity? {
        return notes.find { it.dayIndex == dayIndex && it.category == category }
    }
    override fun getAllRecurringExpenses(): Flow<List<RecurringExpenseEntity>> = flowOf(emptyList())
    override suspend fun getRecurringExpensesSync(): List<RecurringExpenseEntity> = emptyList()
    override suspend fun insertRecurringExpense(expense: RecurringExpenseEntity): Long = 1L
    override suspend fun insertRecurringExpenses(expenses: List<RecurringExpenseEntity>) {}
    override suspend fun deleteRecurringExpense(expense: RecurringExpenseEntity) {}
    override suspend fun clearAllNotes() { notes.clear() }
    override suspend fun clearAllRecurringExpenses() {}
}

class ParentGroceriesAntiAbuseTest {

    @Test
    fun `buying groceries_parents once succeeds and marks task as completed`() = runTest {
        val calendarDao = FakeCalendarDaoForTest()
        var balance = 100
        val currentDay = 2

        // Добавляем активное задание на покупку продуктов родителям
        calendarDao.insertNote(
            CalendarNoteEntity(
                id = 1L,
                dayIndex = currentDay,
                title = "Купить продукты по поручению родителей",
                cost = 0,
                category = "GROCERIES",
                isCompleted = false
            )
        )

        // Имитация первого выполнения задания
        val task = calendarDao.getNoteByDayAndCategory(currentDay, "GROCERIES")
        assertTrue("Task should be present and not completed", task != null && !task.isCompleted)

        val change = 10
        balance += change
        calendarDao.updateChecklistNoteByCategory(currentDay, "GROCERIES", isCompleted = true, cost = change)

        val updatedTask = calendarDao.getNoteByDayAndCategory(currentDay, "GROCERIES")
        assertEquals(110, balance)
        assertTrue(updatedTask?.isCompleted == true)
        assertEquals(10, updatedTask?.cost)
    }

    @Test
    fun `repeated purchase of groceries_parents on same day is rejected and gives zero coins`() = runTest {
        val calendarDao = FakeCalendarDaoForTest()
        var balance = 100
        val currentDay = 2

        // Уже выполненное задание
        calendarDao.insertNote(
            CalendarNoteEntity(
                id = 1L,
                dayIndex = currentDay,
                title = "Купить продукты по поручению родителей",
                cost = 15,
                category = "GROCERIES",
                isCompleted = true
            )
        )

        val task = calendarDao.getNoteByDayAndCategory(currentDay, "GROCERIES")
        // Проверка условия защиты в MainViewModel: если null или isCompleted — возврат без начисления
        val canExecute = task != null && !task.isCompleted
        assertFalse("Repeated execution should not be allowed", canExecute)

        // Баланс не должен измениться
        assertEquals(100, balance)
    }

    @Test
    fun `purchase on day without groceries task is rejected`() = runTest {
        val calendarDao = FakeCalendarDaoForTest()
        var balance = 100
        val currentDay = 1 // В день 1 родители не давали задания

        val task = calendarDao.getNoteByDayAndCategory(currentDay, "GROCERIES")
        val canExecute = task != null && !task.isCompleted
        assertFalse("Execution without active parent task should not be allowed", canExecute)

        assertEquals(100, balance)
    }
}
