package ru.finpet.kids.feature.map

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * одна кликабельная зона на карте.
 * координаты в долях от размера картинки (0f..1f), поэтому не ломаются при любых размерах экрана.
 */
data class MapHotspot(
    val id: String,
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
    val label: String = ""
)

// координаты зданий на карте (Школа, Лавка, Дом)
val DEFAULT_MAP_HOTSPOTS: List<MapHotspot> = listOf(
    MapHotspot(id = "shop",   left = 0.7194f, top = 0.6803f, right = 0.9509f, bottom = 0.7544f, label = "Лавка"),
    MapHotspot(id = "quests", left = 0.0926f, top = 0.5871f, right = 0.4352f, bottom = 0.7024f, label = "Школа"),
    MapHotspot(id = "home",   left = 0.6361f, top = 0.7874f, right = 0.8815f, bottom = 0.8951f, label = "Дом")
)

/**
 * интерактивная карта города с кликабельными зонами.
 * заполняет экран целиком без белых полос, привязана к низу (обрезается только верхушка с небом).
 */
@Composable
fun MapWithHotspots(
    @DrawableRes mapRes: Int,
    imageAspectRatio: Float = 1073f / 3248f,
    hotspots: List<MapHotspot>,
    onClickHotspot: (MapHotspot) -> Unit,
    modifier: Modifier = Modifier,
    anchor: Alignment.Vertical = Alignment.Bottom,
    debug: Boolean = false
) {
    // контейнер карты, обрезает картинку по границам экрана без белых полос
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
    ) {
        val w = maxWidth
        val h = maxHeight

        // масштабируем изображение так, чтобы полностью заполнить ширину экрана,
        // а при необходимости и высоту (cover).
        val targetHeight = w / imageAspectRatio
        val renderedHeight = if (targetHeight >= h) targetHeight else h
        val renderedWidth = if (targetHeight >= h) w else h * imageAspectRatio

        // смещения: привязываем карту к низу экрана, чтобы обрезался только верх (небо)
        val leftOffset = (w - renderedWidth) / 2
        val topOffset = when (anchor) {
            Alignment.Top -> 0.dp
            Alignment.CenterVertically -> (h - renderedHeight) / 2
            else -> h - renderedHeight // Alignment.Bottom
        }

        // 1. сама карта: заполняет экран целиком, прижата строго к низу экрана без отступов
        Image(
            painter = painterResource(id = mapRes),
            contentDescription = "Карта приключений",
            contentScale = ContentScale.Crop,
            alignment = Alignment.BottomCenter,
            modifier = Modifier.fillMaxSize()
        )

        // 2. кликабельные зоны: масштабируются и позиционируются строго относительно картинки карты
        hotspots.forEach { spot ->
            val spotWidth  = renderedWidth * (spot.right - spot.left)
            val spotHeight = renderedHeight * (spot.bottom - spot.top)
            val offsetX    = leftOffset + renderedWidth * spot.left
            val offsetY    = topOffset + renderedHeight * spot.top

            Box(
                modifier = Modifier
                    .offset(x = offsetX, y = offsetY)
                    .size(width = spotWidth, height = spotHeight)
                    .then(
                        if (debug) {
                            Modifier
                                .background(Color.Red.copy(alpha = 0.25f))
                                .border(2.dp, Color.Red)
                        } else {
                            Modifier
                        }
                    )
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onClickHotspot(spot) }
            )
        }

        // 3. отладочный слой для проверки тапов и калибровки координат
        if (debug) {
            var debugTaps by remember { mutableStateOf(listOf<Offset>()) }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(renderedWidth, renderedHeight, leftOffset, topOffset) {
                        val leftPx = leftOffset.toPx()
                        val topPx = topOffset.toPx()
                        val rWidthPx = renderedWidth.toPx()
                        val rHeightPx = renderedHeight.toPx()

                        detectTapGestures { offset ->
                            val nx = (offset.x - leftPx) / rWidthPx
                            val ny = (offset.y - topPx) / rHeightPx
                            Log.d(
                                "MAP",
                                "tap: image(nx=%.4ff, ny=%.4ff) screen(px=%d,%d)".format(
                                    nx, ny, offset.x.roundToInt(), offset.y.roundToInt()
                                )
                            )
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