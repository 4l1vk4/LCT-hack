package ru.finpet.kids.feature.map.minigames

import android.content.Context
import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MatchThreePreferencesTest {

    private val context: Context = mockk(relaxed = true)
    private val prefs: SharedPreferences = mockk(relaxed = true)
    private val editor: SharedPreferences.Editor = mockk(relaxed = true)
    private val storage = mutableMapOf<String, Any>()

    @Before
    fun setUp() {
        storage.clear()
        every { context.getSharedPreferences("match_three_prefs", Context.MODE_PRIVATE) } returns prefs
        every { prefs.edit() } returns editor
        every { editor.putInt(any(), any()) } answers {
            val key = firstArg<String>()
            val value = secondArg<Int>()
            storage[key] = value
            editor
        }
        every { editor.apply() } answers { }
        every { prefs.getInt(any(), any()) } answers {
            val key = firstArg<String>()
            val def = secondArg<Int>()
            (storage[key] as? Int) ?: def
        }
    }

    @Test
    fun `checkAndGrantWeekly grants 3 of each booster when period increases`() {
        // Initial period 1 does not add duplicate boosters (starts with base 3)
        MatchThreePreferences.checkAndGrantWeekly(context, 1)

        assertEquals(3, MatchThreePreferences.getBoosterCount(context, BoosterType.SINGLE))
        assertEquals(3, MatchThreePreferences.getBoosterCount(context, BoosterType.COLUMN))
        assertEquals(3, MatchThreePreferences.getBoosterCount(context, BoosterType.ROW))
        assertEquals(3, MatchThreePreferences.getBoosterCount(context, BoosterType.AREA))

        // Same period does not grant again
        MatchThreePreferences.checkAndGrantWeekly(context, 1)
        assertEquals(3, MatchThreePreferences.getBoosterCount(context, BoosterType.SINGLE))

        // Next period (period 2) grants +3 more
        MatchThreePreferences.checkAndGrantWeekly(context, 2)
        assertEquals(6, MatchThreePreferences.getBoosterCount(context, BoosterType.SINGLE))
    }

    @Test
    fun `awardSchoolBooster grants 1 of each booster`() {
        val initialSingle = MatchThreePreferences.getBoosterCount(context, BoosterType.SINGLE) // 3
        MatchThreePreferences.awardSchoolBooster(context)
        assertEquals(initialSingle + 1, MatchThreePreferences.getBoosterCount(context, BoosterType.SINGLE))
        assertEquals(initialSingle + 1, MatchThreePreferences.getBoosterCount(context, BoosterType.COLUMN))
        assertEquals(initialSingle + 1, MatchThreePreferences.getBoosterCount(context, BoosterType.ROW))
        assertEquals(initialSingle + 1, MatchThreePreferences.getBoosterCount(context, BoosterType.AREA))
    }

    @Test
    fun `consumeBooster decreases count when available`() {
        val count = MatchThreePreferences.getBoosterCount(context, BoosterType.SINGLE)
        assertTrue(count > 0)
        val success = MatchThreePreferences.consumeBooster(context, BoosterType.SINGLE)
        assertTrue(success)
        assertEquals(count - 1, MatchThreePreferences.getBoosterCount(context, BoosterType.SINGLE))
    }

    @Test
    fun `high score updates only when new score is greater`() {
        MatchThreePreferences.saveHighScore(context, 500)
        assertEquals(500, MatchThreePreferences.getHighScore(context))

        assertFalse(MatchThreePreferences.saveHighScore(context, 300))
        assertEquals(500, MatchThreePreferences.getHighScore(context))

        assertTrue(MatchThreePreferences.saveHighScore(context, 1200))
        assertEquals(1200, MatchThreePreferences.getHighScore(context))
    }
}
