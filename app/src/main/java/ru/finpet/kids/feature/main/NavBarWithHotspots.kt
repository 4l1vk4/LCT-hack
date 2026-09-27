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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

data class NavHotspot(
    val id: String,
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
)

// Координаты вкладок на нарисованной панели.
val DEFAULT_NAV_HOTSPOTS: List<NavHotspot> = listOf(
    NavHotspot("map",      0.111f, 0.250f, 0.280f, 0.761f),
    NavHotspot("plans",    0.338f, 0.222f, 0.468f, 0.733f),
    NavHotspot("finik",    0.525f, 0.178f, 0.664f, 0.733f),
    NavHotspot("family",   0.722f, 0.122f, 0.872f, 0.761f)
)

/**
 * Нарисованная панель меню + невидимые кликабельные зоны.
 *
 * @param navRes          R.drawable.nav_bar
 * @param imageAspectRatio  ширина / высота картинки
 * @param hotspots        список зон
 * @param onSelectTab     что вызвать (0..4)
 * @param debug           true — рисует красные рамки, помогает отлаживать
 */
@Suppress("UnusedBoxWithConstraintsScope")
@Composable
fun NavBarWithHotspots(
    @DrawableRes navRes: Int,
    imageAspectRatio: Float,
    hotspots: List<NavHotspot>,
    onSelectTab: (Int) -> Unit,
    modifier: Modifier = Modifier,
    debug: Boolean = false
) {
    BoxWithConstraints(
        modifier = modifier.aspectRatio(imageAspectRatio)
    ) {
        val w = maxWidth
        val h = maxHeight

        // Картинка панели
        Image(
            painter = painterResource(id = navRes),
            contentDescription = "Навигация",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

        // Невидимые кликабельные зоны
        hotspots.forEachIndexed { index, spot ->
            Box(
                modifier = Modifier
                    .offset(x = w * spot.left, y = h * spot.top)
                    .size(w * (spot.right - spot.left), h * (spot.bottom - spot.top))
                    .then(
                        if (debug) Modifier
                            .background(Color.Red.copy(alpha = 0.25f))
                            .border(2.dp, Color.Red)
                        else Modifier
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onSelectTab(index) }
            )
        }

        // Debug-оверлей для замера координат
        if (debug) {
            var debugTaps by remember { mutableStateOf(listOf<Offset>()) }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val nx = offset.x / size.width
                            val ny = offset.y / size.height
                            Log.d("NAV", "tap: nx=$nx ny=$ny (px=${offset.x.roundToInt()},${offset.y.roundToInt()})")
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