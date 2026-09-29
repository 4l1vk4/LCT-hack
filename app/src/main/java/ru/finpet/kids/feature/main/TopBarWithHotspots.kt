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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import ru.finpet.kids.R
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.BlendMode

data class TopBarHotspot(
    val id: String,
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
)

// ⚠️ Все координаты — в долях от размера картинки. Подгони через debug.
private val MENU_HOTSPOT = TopBarHotspot(
    id = "menu",
    left = 0.91f, top = 0.10f, right = 0.99f, bottom = 0.90f
)

// Текст баланса (слева, поверх монетки на картинке)
private const val MONEY_CX = 0.12f
private const val MONEY_CY = 0.2f
private const val MONEY_FONT_SIZE = 13f

// Полоска орбов (в центре)
private const val ORB_BAR_LEFT = 0.27f     // где начинается ряд
private const val ORB_BAR_CENTER_Y = 0.49f // по вертикали — центр
private const val ORB_SIZE_RATIO = 0.40f   // размер орба (доля от высоты панели)
private const val ORB_STEP_RATIO = 0.5f   // сдвиг вправо между орбами (доля от размера)
private const val ORB_PERCENT_STEP = 5     // 1 орб = 5% настроения

private const val MOOD_FACE_CX = 0.876f
private const val MOOD_FACE_CY = 0.50f
private const val MOOD_FACE_WIDTH_RATIO = 0.07f   // ширина (доля ШИРИНЫ панели)

// Проценты настроения (справа от последнего орба)
private const val PERCENT_FONT_SIZE = 13f
private const val PERCENT_CX = 0.68f
private const val PERCENT_CY = 0.15f

@Suppress("UnusedBoxWithConstraintsScope")
@Composable
fun TopBarWithHotspots(
    @DrawableRes topBarRes: Int,
    imageAspectRatio: Float,
    balance: Int,
    moodPercent: Int,
    onOpenInstructions: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    debug: Boolean = false
) {
    var menuExpanded by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = modifier.aspectRatio(imageAspectRatio)
    ) {
        val w = maxWidth
        val h = maxHeight

        // 1. Фон панели
        Image(
            painter = painterResource(topBarRes),
            contentDescription = "Верхняя панель",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

        val moneyFontSize = when {
            balance > 9999 -> 8f
            balance > 999  -> 10f
            else           -> MONEY_FONT_SIZE
        }

        // 2. Баланс — текст поверх монетки слева
        Text(
            text = "$balance",
            fontSize = moneyFontSize.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF381F0E),
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = w * MONEY_CX, y = h * MONEY_CY)
        )

        // 3. Орбы настроения — накладываем N штук в ряд
        val orbCount = (moodPercent / ORB_PERCENT_STEP).coerceIn(0, 100 / ORB_PERCENT_STEP)

        val orbSizeDp = h * ORB_SIZE_RATIO
        val stepDp = orbSizeDp * ORB_STEP_RATIO
        val startXDp = w * ORB_BAR_LEFT
        val yDp = h * ORB_BAR_CENTER_Y - orbSizeDp / 2
        val tint = moodColor(moodPercent)

        if (orbCount > 0) {
            for (i in 0 until orbCount) {
                val xDp = startXDp + stepDp * i
                Image(
                    painter = painterResource(R.drawable.orb),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    // Тонируем однотонный orb.png в цвет настроения
                    colorFilter = ColorFilter.tint(tint, BlendMode.SrcIn),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = xDp, y = yDp)
                        .size(orbSizeDp)
                )
            }
        }

        // 3.1. Проценты справа от последнего орба
        Text(
            text = "$moodPercent%",
            fontSize = PERCENT_FONT_SIZE.sp,
            fontWeight = FontWeight.Black,
            color = tint,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(
                    x = w * PERCENT_CX,
                    y = h * PERCENT_CY
                )
        )

        // 3.2. Смайлик настроения (слева от кнопки меню)
        val faceWidth = w * MOOD_FACE_WIDTH_RATIO
        Image(
            painter = painterResource(moodFaceFor(moodPercent)),
            contentDescription = "Настроение",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(
                    x = w * MOOD_FACE_CX - faceWidth / 2,
                    y = h * MOOD_FACE_CY - faceWidth / 2
                )
                .size(faceWidth)
        )

        // 4. Хитбокс кнопки меню
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = w * MENU_HOTSPOT.left, y = h * MENU_HOTSPOT.top)
                .size(
                    w * (MENU_HOTSPOT.right - MENU_HOTSPOT.left),
                    h * (MENU_HOTSPOT.bottom - MENU_HOTSPOT.top)
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { menuExpanded = true }
        )

        // 5. Выпадающее меню под кнопкой
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(
                    x = w * MENU_HOTSPOT.left - 140.dp,
                    y = h * MENU_HOTSPOT.bottom + 4.dp
                )
        ) {
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("📖 Инструкция", fontSize = 15.sp, fontWeight = FontWeight.SemiBold) },
                    onClick = {
                        menuExpanded = false
                        onOpenInstructions()
                    }
                )
                DropdownMenuItem(
                    text = { Text("⚙️ Настройки", fontSize = 15.sp, fontWeight = FontWeight.SemiBold) },
                    onClick = {
                        menuExpanded = false
                        onOpenSettings()
                    }
                )
            }
        }

        // 6. Debug
        if (debug) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = w * MENU_HOTSPOT.left, y = h * MENU_HOTSPOT.top)
                    .size(
                        w * (MENU_HOTSPOT.right - MENU_HOTSPOT.left),
                        h * (MENU_HOTSPOT.bottom - MENU_HOTSPOT.top)
                    )
                    .background(Color.Red.copy(alpha = 0.3f))
                    .border(2.dp, Color.Red)
            )

            var debugTaps by remember { mutableStateOf(listOf<Offset>()) }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val nx = offset.x / size.width
                            val ny = offset.y / size.height
                            Log.d("TOPBAR", "tap: nx=$nx ny=$ny")
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

@DrawableRes
private fun moodFaceFor(percent: Int): Int = when {
    percent >= 70 -> R.drawable.happy_top
    percent >= 40 -> R.drawable.neutral_top
    else          -> R.drawable.sad_top
}

private fun moodColor(percent: Int): Color = when {
    percent >= 70 -> Color(0xFF4CAF50)
    percent >= 40 -> Color(0xFFFFC107)
    else          -> Color(0xFFE53935)
}