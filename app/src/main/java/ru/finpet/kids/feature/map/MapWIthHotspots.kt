package ru.finpet.kids.feature.map

import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import ru.finpet.kids.R
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import ru.finpet.kids.core.designsystem.ExclamationIcon
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
import androidx.compose.ui.graphics.graphicsLayer
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

// координаты зданий на карте (школа, лавка, дом)
val DEFAULT_MAP_HOTSPOTS: List<MapHotspot> = listOf(
    MapHotspot(id = "shop",   left = 0.7194f, top = 0.6503f, right = 0.9509f, bottom = 0.7544f, label = "Лавка"),
    MapHotspot(id = "quests", left = 0.0926f, top = 0.5571f, right = 0.4352f, bottom = 0.7024f, label = "Школа"),
    MapHotspot(id = "home",   left = 0.6361f, top = 0.7574f, right = 0.8815f, bottom = 0.8951f, label = "Дом")
)

/**
 * интерактивная карта города с кликабельными зонами.
 */
@Composable
fun MapWithHotspots(
    @DrawableRes mapRes: Int,
    imageAspectRatio: Float = 1073f / 3248f,
    hotspots: List<MapHotspot>,
    onClickHotspot: (MapHotspot) -> Unit,
    modifier: Modifier = Modifier,
    anchor: Alignment.Vertical = Alignment.Bottom,
    hasShopWarning: Boolean = false,
    debug: Boolean = false,
    isNight: Boolean = false
) {
    // контейнер карты с обрезкой по границам экрана
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
    ) {
        val w = maxWidth
        val h = maxHeight

        // масштабирование изображения под ширину и высоту экрана
        val targetHeight = w / imageAspectRatio
        val renderedHeight = if (targetHeight >= h) targetHeight else h
        val renderedWidth = if (targetHeight >= h) w else h * imageAspectRatio

        // смещения: привязка карты к низу экрана
        val leftOffset = (w - renderedWidth) / 2
        val topOffset = when (anchor) {
            Alignment.Top -> 0.dp
            Alignment.CenterVertically -> (h - renderedHeight) / 2
            else -> h - renderedHeight // Alignment.Bottom
        }

        // 1. фоновое изображение карты
        Image(
            painter = painterResource(id = mapRes),
            contentDescription = "Карта приключений",
            contentScale = ContentScale.Crop,
            alignment = Alignment.BottomCenter,
            modifier = Modifier.fillMaxSize()
        )

        // 1.5. анимированный слой плывущих облаков над картой
        ru.finpet.kids.feature.map.components.MovingCloudsOverlay(
            isNight = isNight,
            modifier = Modifier.fillMaxSize()
        )

        val mapPlatesAnim = rememberInfiniteTransition(label = "map_plates_anim")
        val schoolBobY by mapPlatesAnim.animateFloat(
            initialValue = -3.5f,
            targetValue = 3.5f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1900, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "school_bob"
        )
        val shopBobY by mapPlatesAnim.animateFloat(
            initialValue = 3.5f,
            targetValue = -3.5f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2100, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "shop_bob"
        )
        val homeBobY by mapPlatesAnim.animateFloat(
            initialValue = -3f,
            targetValue = 3f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "home_bob"
        )

        // 2. кликабельные зоны: масштабируются и позиционируются строго относительно картинки карты
        hotspots.forEach { spot ->
            val spotWidth  = renderedWidth * (spot.right - spot.left)
            val spotHeight = renderedHeight * (spot.bottom - spot.top)
            val offsetX    = leftOffset + renderedWidth * spot.left
            val offsetY    = topOffset + renderedHeight * spot.top

            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val scale by animateFloatAsState(
                targetValue = if (isPressed) 0.95f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "spot_scale"
            )

            Box(
                modifier = Modifier
                    .offset(x = offsetX, y = offsetY)
                    .size(width = spotWidth, height = spotHeight)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
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
                        interactionSource = interactionSource,
                        indication = null
                    ) { onClickHotspot(spot) }
            ) {
                if (spot.id == "shop" && hasShopWarning) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 2.dp, y = 0.dp)
                    ) {
                        ExclamationIcon(modifier = Modifier.size(22.dp))
                    }
                }
            }
        }

        // 2.5. Анимированные парящие пиксельные вывески над зданиями (Школа, Лавка/Магазин, Домик)
        val schoolW = renderedWidth * (185f / 1073f)
        val schoolH = renderedHeight * (63f / 3248f)
        val schoolLeft = leftOffset + renderedWidth * (198f / 1073f)
        val schoolTop = topOffset + renderedHeight * (1844f / 3248f)
        val questsSpot = hotspots.firstOrNull { it.id == "quests" }

        Image(
            painter = painterResource(id = if (isNight) R.drawable.label_school_night else R.drawable.label_school),
            contentDescription = "Школа",
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = schoolLeft, y = schoolTop)
                .size(width = schoolW, height = schoolH)
                .graphicsLayer {
                    translationY = schoolBobY.dp.toPx()
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { questsSpot?.let(onClickHotspot) }
        )

        val shopW = renderedWidth * (179f / 1073f)
        val shopH = renderedHeight * (63f / 3248f)
        val shopLeft = leftOffset + renderedWidth * (816f / 1073f)
        val shopTop = topOffset + renderedHeight * (2140f / 3248f)
        val shopSpot = hotspots.firstOrNull { it.id == "shop" }

        Image(
            painter = painterResource(id = if (isNight) R.drawable.label_shop_night else R.drawable.label_shop),
            contentDescription = "Лавка",
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = shopLeft, y = shopTop)
                .size(width = shopW, height = shopH)
                .graphicsLayer {
                    translationY = shopBobY.dp.toPx()
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { shopSpot?.let(onClickHotspot) }
        )

        val homeW = renderedWidth * (145f / 1073f)
        val homeH = renderedHeight * (63f / 3248f)
        val homeLeft = leftOffset + renderedWidth * (740f / 1073f)
        val homeTop = topOffset + renderedHeight * (2482f / 3248f)
        val homeSpot = hotspots.firstOrNull { it.id == "home" }

        Image(
            painter = painterResource(id = if (isNight) R.drawable.label_home_night else R.drawable.label_home),
            contentDescription = "Дом",
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = homeLeft, y = homeTop)
                .size(width = homeW, height = homeH)
                .graphicsLayer {
                    translationY = homeBobY.dp.toPx()
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { homeSpot?.let(onClickHotspot) }
        )

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