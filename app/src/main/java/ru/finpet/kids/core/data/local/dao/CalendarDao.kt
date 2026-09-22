package ru.finpet.kids.core.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.finpet.kids.core.data.local.entity.CalendarNoteEntity
import ru.finpet.kids.core.data.local.entity.RecurringExpenseEntity

@Dao
interface CalendarDao {

    @Query("SELECT * FROM calendar_notes WHERE dayIndex = :dayIndex ORDER BY id ASC")
    fun getNotesForDay(dayIndex: Int): Flow<List<CalendarNoteEntity>>

    @Query("SELECT * FROM calendar_notes ORDER BY dayIndex ASC, id ASC")
    fun getAllNotes(): Flow<List<CalendarNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: CalendarNoteEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: List<CalendarNoteEntity>)

    @Delete
    suspend fun deleteNote(note: CalendarNoteEntity)

    @Query("UPDATE calendar_notes SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateNoteCompleted(id: Long, isCompleted: Boolean)

    @Query("SELECT * FROM recurring_expenses ORDER BY id ASC")
    fun getAllRecurringExpenses(): Flow<List<RecurringExpenseEntity>>

    @Query("SELECT * FROM recurring_expenses ORDER BY id ASC")
    suspend fun getRecurringExpensesSync(): List<RecurringExpenseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringExpense(expense: RecurringExpenseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringExpenses(expenses: List<RecurringExpenseEntity>)

    @Delete
    suspend fun deleteRecurringExpense(expense: RecurringExpenseEntity)

    @Query("DELETE FROM calendar_notes")
    suspend fun clearAllNotes()

    @Query("DELETE FROM recurring_expenses")
    suspend fun clearAllRecurringExpenses()
}
