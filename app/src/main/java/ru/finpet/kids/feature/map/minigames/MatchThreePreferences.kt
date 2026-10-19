package ru.finpet.kids.feature.map.minigames

import android.content.Context
import android.content.SharedPreferences

enum class BoosterType {
    SINGLE,
    COLUMN,
    ROW,
    AREA
}

object MatchThreePreferences {
    private const val PREFS_NAME = "match_three_prefs"
    private const val KEY_BOOSTER_SINGLE = "booster_single"
    private const val KEY_BOOSTER_COLUMN = "booster_column"
    private const val KEY_BOOSTER_ROW = "booster_row"
    private const val KEY_BOOSTER_AREA = "booster_area"
    private const val KEY_LAST_PERIOD_AWARDED = "last_period_awarded"
    private const val KEY_HIGH_SCORE = "high_score"

    private fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun checkAndGrantWeekly(context: Context, currentPeriod: Int) {
        val prefs = getPrefs(context)
        val lastPeriod = prefs.getInt(KEY_LAST_PERIOD_AWARDED, 1)
        if (currentPeriod > lastPeriod) {
            val single = prefs.getInt(KEY_BOOSTER_SINGLE, 3) + 3
            val column = prefs.getInt(KEY_BOOSTER_COLUMN, 3) + 3
            val row = prefs.getInt(KEY_BOOSTER_ROW, 3) + 3
            val area = prefs.getInt(KEY_BOOSTER_AREA, 3) + 3
            prefs.edit()
                .putInt(KEY_BOOSTER_SINGLE, single)
                .putInt(KEY_BOOSTER_COLUMN, column)
                .putInt(KEY_BOOSTER_ROW, row)
                .putInt(KEY_BOOSTER_AREA, area)
                .putInt(KEY_LAST_PERIOD_AWARDED, currentPeriod)
                .apply()
        } else if (lastPeriod > currentPeriod) {
            // Исправление рассинхронизации, если ранее ошибочно был записан dayIndex
            prefs.edit().putInt(KEY_LAST_PERIOD_AWARDED, currentPeriod).apply()
        }
    }

    fun awardSchoolBooster(context: Context) {
        val prefs = getPrefs(context)
        val single = prefs.getInt(KEY_BOOSTER_SINGLE, 3) + 1
        val column = prefs.getInt(KEY_BOOSTER_COLUMN, 3) + 1
        val row = prefs.getInt(KEY_BOOSTER_ROW, 3) + 1
        val area = prefs.getInt(KEY_BOOSTER_AREA, 3) + 1
        prefs.edit()
            .putInt(KEY_BOOSTER_SINGLE, single)
            .putInt(KEY_BOOSTER_COLUMN, column)
            .putInt(KEY_BOOSTER_ROW, row)
            .putInt(KEY_BOOSTER_AREA, area)
            .apply()
    }

    fun getBoosterCount(context: Context, type: BoosterType): Int {
        val prefs = getPrefs(context)
        return when (type) {
            BoosterType.SINGLE -> prefs.getInt(KEY_BOOSTER_SINGLE, 3)
            BoosterType.COLUMN -> prefs.getInt(KEY_BOOSTER_COLUMN, 3)
            BoosterType.ROW -> prefs.getInt(KEY_BOOSTER_ROW, 3)
            BoosterType.AREA -> prefs.getInt(KEY_BOOSTER_AREA, 3)
        }
    }

    fun consumeBooster(context: Context, type: BoosterType): Boolean {
        val prefs = getPrefs(context)
        val key = when (type) {
            BoosterType.SINGLE -> KEY_BOOSTER_SINGLE
            BoosterType.COLUMN -> KEY_BOOSTER_COLUMN
            BoosterType.ROW -> KEY_BOOSTER_ROW
            BoosterType.AREA -> KEY_BOOSTER_AREA
        }
        val current = prefs.getInt(key, 3)
        if (current > 0) {
            prefs.edit().putInt(key, current - 1).apply()
            return true
        }
        return false
    }

    fun getHighScore(context: Context): Int =
        getPrefs(context).getInt(KEY_HIGH_SCORE, 0)

    fun saveHighScore(context: Context, score: Int): Boolean {
        val prefs = getPrefs(context)
        val current = prefs.getInt(KEY_HIGH_SCORE, 0)
        if (score > current) {
            prefs.edit().putInt(KEY_HIGH_SCORE, score).apply()
            return true
        }
        return false
    }

    fun resetAll(context: Context) {
        val prefs = getPrefs(context)
        prefs.edit()
            .putInt(KEY_BOOSTER_SINGLE, 3)
            .putInt(KEY_BOOSTER_COLUMN, 3)
            .putInt(KEY_BOOSTER_ROW, 3)
            .putInt(KEY_BOOSTER_AREA, 3)
            .putInt(KEY_LAST_PERIOD_AWARDED, 1)
            .putInt(KEY_HIGH_SCORE, 0)
            .apply()
    }
}
