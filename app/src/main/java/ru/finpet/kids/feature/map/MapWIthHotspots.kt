package ru.finpet.kids.feature.map

import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.gestures.detectTapGestures
import kotlin.math.roundToInt

/**
 * Одна кликабельная зона на карте.
 * Координаты — в долях от размера картинки: 0f = край, 1f = противоположный край.
 * Такие координаты не ломаются при смене разрешения и размера экрана.
 */
data class MapHotspot(
    val id: String,
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
    val label: String = ""
)

// Координаты зданий на карте
val DEFAULT_MAP_HOTSPOTS: List<MapHotspot> = listOf(
    MapHotspot(id = "shop",   left = 0.7222f, top = 0.5852f, right = 0.9463f, bottom = 0.6955f, label = "Лавка"),
    MapHotspot(id = "quests", left = 0.0787f, top = 0.4377f, right = 0.4324f, bottom = 0.6232f, label = "Шатёр"),
    MapHotspot(id = "home",   left = 0.6343f, top = 0.7540f, right = 0.8611f, bottom = 0.9206f, label = "Домик")
)

/**
 * Карта-картинка с невидимыми кликабельными зонами поверх.
 *
 * @param mapRes            R.drawable.day
 * @param imageAspectRatio  ширина / высота картинки
 * @param hotspots          список зон
 * @param onClickHotspot    что вызвать при тапе по зоне
 * @param debug             true — рисует полупрозрачные рамки, помогает отлаживать
 */
@Suppress("UnusedBoxWithConstraintsScope")
@Composable
fun MapWithHotspots(
    @DrawableRes mapRes: Int,
    imageAspectRatio: Float,
    hotspots: List<MapHotspot>,
    onClickHotspot: (MapHotspot) -> Unit,
    modifier: Modifier = Modifier,
    debug: Boolean = false
) {
    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(imageAspectRatio)
    ) {
        val w = maxWidth
        val h = maxHeight

        // 1. Сама карта
        Image(
            painter = painterResource(id = mapRes),
            contentDescription = "Карта приключений",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Кликабельные зоны
        hotspots.forEach { spot ->
            val spotWidth  = w * (spot.right - spot.left)
            val spotHeight = h * (spot.bottom - spot.top)
            val offsetX    = w * spot.left
            val offsetY    = h * spot.top

            Box(
                modifier = Modifier
                    .offset(x = offsetX, y = offsetY)
                    .size(width = spotWidth, height = spotHeight)
                    .then(
                        if (debug) {
                            Modifier
                                .background(Color.Red.copy(alpha = 0.25f))
                                .border(2.dp, Color.Red)
                        } else Modifier
                    )
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onClickHotspot(spot) }
            )
        }

        // 3. Debug-оверлей: тапы логируются и рисуются точками
        if (debug) {
            var debugTaps by remember { mutableStateOf(listOf<Offset>()) }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val nx = offset.x / size.width
                            val ny = offset.y / size.height
                            Log.d("MAP", "tap: nx=$nx ny=$ny  (px=${offset.x.roundToInt()},${offset.y.roundToInt()})")
                            debugTaps = debugTaps + offset
                        }
                    }
            ) {
                debugTaps.forEach { pos ->
                    Box(
                        modifier = Modifier
                            .offset {
                                IntOffset(pos.x.roundToInt() - 8, pos.y.roundToInt() - 8)
                            }
                            .size(16.dp)
                            .background(Color.Magenta, CircleShape)
                    )
                }
            }
        }
    }
}