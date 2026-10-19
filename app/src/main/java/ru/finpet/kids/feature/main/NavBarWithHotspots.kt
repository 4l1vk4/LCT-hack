package ru.finpet.kids.feature.main

import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import ru.finpet.kids.core.designsystem.ExclamationIcon
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

import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntSize
import ru.finpet.kids.R

data class NavHotspot(
    val id: String,
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
)

// координаты вкладок на панели навигации
val DEFAULT_NAV_HOTSPOTS: List<NavHotspot> = listOf(
    NavHotspot("map",      0.000f, 0.000f, 0.250f, 1.000f),
    NavHotspot("calendar", 0.250f, 0.000f, 0.500f, 1.000f),
    NavHotspot("finik",    0.500f, 0.000f, 0.750f, 1.000f),
    NavHotspot("family",   0.750f, 0.000f, 1.000f, 1.000f)
)

/**
 * панель навигации с невидимыми кликабельными зонами и рамкой активной локации.
 */
@Suppress("UnusedBoxWithConstraintsScope")
@Composable
fun NavBarWithHotspots(
    @DrawableRes navRes: Int,
    imageAspectRatio: Float,
    hotspots: List<NavHotspot>,
    onSelectTab: (Int) -> Unit,
    modifier: Modifier = Modifier,
    selectedTab: Int = 0,
    showImage: Boolean = true,
    calendarHasWarning: Boolean = false,
    mapHasWarning: Boolean = false,
    debug: Boolean = false
) {
    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(imageAspectRatio)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { /* блокировка кликов мимо кнопок на нижней панели */ }
    ) {
        val w = maxWidth
        val h = maxHeight

        // фоновая картинка панели
        if (showImage) {
            Image(
                painter = painterResource(id = navRes),
                contentDescription = "Навигация",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize()
            )
        }

        // рамка поверх выбранной локации
        NavSelectionFrame(selectedTab = selectedTab)

        // кликабельные зоны вкладок
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
            ) {
                // красный восклицательный знак справа сверху от важного
                if (spot.id == "calendar" && calendarHasWarning) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 4.dp, end = 10.dp)
                    ) {
                        ExclamationIcon(modifier = Modifier.size(16.dp))
                    }
                }
                if (spot.id == "map" && mapHasWarning) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 4.dp, end = 10.dp)
                    ) {
                        ExclamationIcon(modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // оверлей для отладки координат
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

/**
 * рамка поверх выбранной локации в меню.
 * одинаковый аккуратный квадратик на всех вкладках без растягивания.
 */
@Composable
private fun NavSelectionFrame(
    selectedTab: Int,
    modifier: Modifier = Modifier
) {
    if (selectedTab !in 0..3) return
    val ramkaBitmap = ImageBitmap.imageResource(id = R.drawable.ramka_for_menu)

    val targetBaseX = when (selectedTab) {
        0 -> 87f    // карта
        1 -> 325f   // календарь
        2 -> 568f   // финни
        3 -> 810f   // семья (сдвинуто чуть правее для ровного обрамления)
        else -> 87f
    }

    val animatedBaseX by animateFloatAsState(
        targetValue = targetBaseX,
        animationSpec = spring(
            dampingRatio = 0.8f,
            stiffness = 380f
        ),
        label = "nav_selection_x"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val scaleX = size.width / 1073f
        val h = size.height.roundToInt()
        val dstWidth = (174f * scaleX).roundToInt()
        val dstLeft = (animatedBaseX * scaleX).roundToInt()

        drawImage(
            image = ramkaBitmap,
            dstOffset = IntOffset(dstLeft, 0),
            dstSize = IntSize(dstWidth, h),
            filterQuality = FilterQuality.None
        )
    }
}