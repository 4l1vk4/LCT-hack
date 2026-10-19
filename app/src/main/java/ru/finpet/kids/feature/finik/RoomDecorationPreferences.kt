package ru.finpet.kids.feature.finik

import android.content.Context
import android.content.SharedPreferences

/**
 * Хранилище настроек отображения накопленных предметов в комнате питомца (Самокат, Мяч, Наушники).
 * Позволяет убирать/возвращать предметы в комнату через меню гардероба.
 */
object RoomDecorationPreferences {
    private const val PREFS_NAME = "finpet_room_decorations"
    private const val KEY_HIDDEN_ITEMS = "hidden_room_items"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getHiddenItems(context: Context): Set<String> {
        return getPrefs(context).getStringSet(KEY_HIDDEN_ITEMS, emptySet()) ?: emptySet()
    }

    fun toggleItemVisibility(context: Context, itemId: String): Set<String> {
        val current = getHiddenItems(context).toMutableSet()
        if (itemId in current) {
            current.remove(itemId)
        } else {
            current.add(itemId)
        }
        getPrefs(context).edit().putStringSet(KEY_HIDDEN_ITEMS, current).apply()
        return current
    }
}
