package ru.finpet.kids.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_TRIGGER_NOTIFICATION = "ru.finpet.kids.ACTION_TRIGGER_NOTIFICATION"
        const val EXTRA_TYPE = "extra_notification_type"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val typeName = intent?.getStringExtra(EXTRA_TYPE) ?: return
        val type = runCatching { NotificationType.valueOf(typeName) }.getOrNull() ?: return

        when (type) {
            NotificationType.MORNING -> {
                FinPetNotificationManager.showNotification(context, type)
            }
            NotificationType.INACTIVITY -> {
                // Показываем только если ребёнок ни разу не заходил в приложение с 8:00 до 20:00
                if (NotificationPreferences.shouldShowInactivityNotification(context)) {
                    FinPetNotificationManager.showNotification(context, type)
                }
            }
            NotificationType.EVENING -> {
                FinPetNotificationManager.showNotification(context, type)
            }
        }

        // Автоматически перепланируем на следующий день
        NotificationScheduler.schedule(context, type)
    }
}
