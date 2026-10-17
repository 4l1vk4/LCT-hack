package ru.finpet.kids.core.notification

enum class NotificationType(
    val hour: Int,
    val minute: Int,
    val message: String,
    val requestCode: Int,
    val notificationId: Int
) {
    MORNING(
        hour = 8,
        minute = 0,
        message = "Привет, я уже проснулся и готов к новому дню!",
        requestCode = 101,
        notificationId = 1001
    ),
    INACTIVITY(
        hour = 20,
        minute = 0,
        message = "Эй, а кто будет меня кормить и помогать распоряжаться деньгами? Скорее заходи, я жду тебя!",
        requestCode = 102,
        notificationId = 1002
    ),
    EVENING(
        hour = 21,
        minute = 0,
        message = "Я скоро лягу спать, доделай свои задачи на сегодня, если что-то осталось!",
        requestCode = 103,
        notificationId = 1003
    )
}
