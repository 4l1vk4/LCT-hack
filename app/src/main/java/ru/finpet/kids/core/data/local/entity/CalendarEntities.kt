package ru.finpet.kids.core.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "calendar_notes", indices = [Index(value = ["dayIndex"])])
data class CalendarNoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dayIndex: Int, // 1..28
    val title: String,
    val cost: Int = 0,
    val category: String = "MANDATORY", // MANDATORY, OPTIONAL, SAVINGS
    val isCompleted: Boolean = false
)

@Entity(tableName = "recurring_expenses")
data class RecurringExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val cost: Int,
    val frequencyDays: Int = 1, // 1 = каждый день, 3 = раз в 3 дня, 7 = раз в неделю
    val icon: String = "🪙",
    val category: String = "MANDATORY"
)
