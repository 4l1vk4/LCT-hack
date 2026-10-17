package ru.finpet.kids.core.notification

import android.content.Context
import android.content.SharedPreferences
import java.time.LocalDate
import java.time.LocalTime

object NotificationPreferences {

    private const val PREFS_NAME = "finpet_notifications_prefs"
    private const val KEY_LAST_APP_OPEN_MILLIS = "last_app_open_millis"
    private const val KEY_LAST_OPEN_BETWEEN_8_AND_20_DATE = "last_open_between_8_and_20_date"
    private const val KEY_PET_NAME = "cached_pet_name"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Фиксирует вход в приложение.
     * Если вход совершён в интервале с 8:00 до 20:00 (реальное время),
     * запоминает текущую дату.
     */
    fun recordAppOpen(context: Context) {
        val nowTime = LocalTime.now()
        val todayStr = LocalDate.now().toString()
        val prefs = getPrefs(context)
        val editor = prefs.edit()

        editor.putLong(KEY_LAST_APP_OPEN_MILLIS, System.currentTimeMillis())

        // 8:00 до 20:00 (часы от 8 до 19 включительно)
        if (nowTime.hour in 8..19) {
            editor.putString(KEY_LAST_OPEN_BETWEEN_8_AND_20_DATE, todayStr)
        }

        editor.apply()
    }

    /**
     * Проверяет условие для уведомления в 20:00:
     * пользователь ни разу не заходил в приложение с 8:00 до 20:00 сегодня.
     */
    fun shouldShowInactivityNotification(context: Context): Boolean {
        val prefs = getPrefs(context)
        val todayStr = LocalDate.now().toString()
        val lastOpenBetween8And20 = prefs.getString(KEY_LAST_OPEN_BETWEEN_8_AND_20_DATE, null)

        // Если сегодня уже был вход между 8:00 и 20:00 — уведомление не показываем
        if (lastOpenBetween8And20 == todayStr) {
            return false
        }

        // Защитная проверка: если пользователь прямо сейчас в приложении или заходил буквально в последние 10 минут
        val lastOpenMillis = prefs.getLong(KEY_LAST_APP_OPEN_MILLIS, 0L)
        if (System.currentTimeMillis() - lastOpenMillis < 10 * 60 * 1000) {
            return false
        }

        return true
    }

    /**
     * Кэширует имя питомца для быстрого доступа из BroadcastReceiver.
     */
    fun cachePetName(context: Context, name: String) {
        getPrefs(context).edit().putString(KEY_PET_NAME, name).apply()
    }

    fun getCachedPetName(context: Context): String? {
        return getPrefs(context).getString(KEY_PET_NAME, null)?.takeIf { it.isNotBlank() }
    }
}
