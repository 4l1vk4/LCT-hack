package ru.finpet.kids.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import ru.finpet.kids.MainActivity
import ru.finpet.kids.R

object FinPetNotificationManager {

    const val CHANNEL_ID = "finpet_daily_reminders"
    private const val CHANNEL_NAME = "Уведомления ФинПитомец"
    private const val CHANNEL_DESCRIPTION = "Ежедневные уведомления и забота о питомце"

    /**
     * Создаёт канал уведомлений (необходимо для Android 8.0+).
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                setShowBadge(true)
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    /**
     * Отображает push-уведомление заданного типа.
     */
    fun showNotification(context: Context, type: NotificationType) {
        createNotificationChannel(context)

        // Заголовок: имя питомца, если задано, иначе название приложения
        val petName = NotificationPreferences.getCachedPetName(context)
        val title = petName ?: context.getString(R.string.app_name)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            type.requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_pet)
            .setContentTitle(title)
            .setContentText(type.message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(type.message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        runCatching {
            NotificationManagerCompat.from(context).notify(type.notificationId, notification)
        }
    }
}
