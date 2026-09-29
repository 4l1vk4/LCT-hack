package ru.finpet.kids.feature.main

import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

/**
 * Элемент панели. Всё задаётся явно в долях от размера картинки (0..1).
 * Меняешь числа — двигаешь/растягиваешь.
 */
data class HelpHotspot(
    val id: String,

    // ─── Кликабельная зона (иконка + текст) ───
    val hitLeft: Float,
    val hitTop: Float,
    val hitRight: Float,
    val hitBottom: Float,

    // ─── Текст внутри зоны ───
    val label: String,
    val labelCX: Float,        // X-позиция ЛЕВОГО края текста (доля ширины)
    val labelCY: Float,        // Y-позиция ВЕРХА текста (доля высоты)
    val labelWidth: Float,     // ширина блока текста (доля ширины)
    val labelFontSize: Float   // размер шрифта
)

// ⚠️ Просто подгоняй числа, ничего не считай вручную.
val DEFAULT_HELP_HOTSPOTS: List<HelpHotspot> = listOf(
    HelpHotspot(
        id = "instructions",
        // Кликабельная зона (иконка + текст справа)
        hitLeft = 0.02f, hitTop = 0.15f, hitRight = 0.42f, hitBottom = 0.85f,
        // Текст «Инструкция»
        label = "Инструкция",
        labelCX = 0.09f,        // ← X левого края текста
        labelCY = 0.15f,        // ← Y верха текста
        labelWidth = 0.40f,     // ← ширина
        labelFontSize = 15f     // ← размер шрифта
    ),
    HelpHotspot(
        id = "settings",
        // Кликабельная зона (иконка + текст слева)
        hitLeft = 0.45f, hitTop = 0.15f, hitRight = 0.82f, hitBottom = 0.85f,
        // Текст «Настройки»
        label = "Настройки",
        labelCX = 0.53f,
        labelCY = 0.15f,
        labelWidth = 0.40f,
        labelFontSize = 15f
    ),
    HelpHotspot(
        id = "close",
        hitLeft = 0.9f, hitTop = 0.0f, hitRight = 1.0f, hitBottom = 1.0f,
        label = "",
        labelCX = 0f, labelCY = 0f,
        labelWidth = 0f, labelFontSize = 0f
)
)

@Suppress("UnusedBoxWithConstraintsScope")
@Composable
fun HelpPanelWithHotspots(
    @DrawableRes panelRes: Int,
    imageAspectRatio: Float,
    hotspots: List<HelpHotspot> = DEFAULT_HELP_HOTSPOTS,
    onInstructions: () -> Unit,
    onSettings: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    debug: Boolean = false
) {
    BoxWithConstraints(
        modifier = modifier.aspectRatio(imageAspectRatio)
    ) {
        val w = maxWidth
        val h = maxHeight

        // 1. Фон
        Image(
            painter = painterResource(panelRes),
            contentDescription = "Панель справки",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Хитбоксы + подписи
        hotspots.forEach { spot ->
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = w * spot.hitLeft, y = h * spot.hitTop)
                    .size(
                        w * (spot.hitRight - spot.hitLeft),
                        h * (spot.hitBottom - spot.hitTop)
                    )
                    .then(
                        if (debug) Modifier
                            .background(Color.Red.copy(alpha = 0.25f))
                            .border(2.dp, Color.Red)
                        else Modifier
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        when (spot.id) {
                            "instructions" -> onInstructions()
                            "settings" -> onSettings()
                            "close" -> onClose()
                        }
                    }
            )

            // Текст — рисуется АБСОЛЮТНО, не привязан к хитбоксу
            Text(
                text = spot.label,
                fontSize = spot.labelFontSize.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF381F0E),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = w * spot.labelCX, y = h * spot.labelCY)
                    .width(w * spot.labelWidth)
            )
        }

        // 4. Debug — лог тапов
        if (debug) {
            var debugTaps by remember { mutableStateOf(listOf<Offset>()) }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val nx = offset.x / size.width
                            val ny = offset.y / size.height
                            Log.d("HELP", "tap: nx=$nx ny=$ny")
                            debugTaps = debugTaps + offset
                        }
                    }
            ) {
                debugTaps.forEach { pos ->
                    Box(
                        modifier = Modifier
                            .offset { IntOffset(pos.x.roundToInt() - 8, pos.y.roundToInt() - 8) }
                            .size(16.dp)
                            .background(Color.Magenta, CircleShape)
                    )
                }
            }
        }
    }
}