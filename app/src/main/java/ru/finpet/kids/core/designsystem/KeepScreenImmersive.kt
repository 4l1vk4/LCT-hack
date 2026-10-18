package ru.finpet.kids.core.designsystem

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.View
import android.view.Window
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

private fun findWindow(view: View): Window? {
    var current: Any? = view
    while (current != null) {
        if (current is DialogWindowProvider) {
            return current.window
        }
        current = (current as? View)?.parent
    }
    var context: Context? = view.context
    while (context != null) {
        if (context is Activity) {
            return context.window
        }
        context = (context as? ContextWrapper)?.baseContext
    }
    return null
}

/**
 * сохраняет полноэкранный режим без системных панелей телефона (status bar и navigation bar)
 * при открытии диалоговых окон и модальных шторок
 */
@Composable
fun KeepScreenImmersive() {
    val view = LocalView.current
    DisposableEffect(view) {
        val window = findWindow(view)
        if (window != null) {
            // Гарантируем, что окно модалки/диалога получает фокус ввода и тач-события
            window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
            window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)

            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            insetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            insetsController.hide(
                WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars()
            )
        }
        onDispose {}
    }
}
