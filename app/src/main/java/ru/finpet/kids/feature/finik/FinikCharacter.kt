package ru.finpet.kids.feature.finik

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.request.ImageRequest
import ru.finpet.kids.R
import kotlin.math.roundToInt

/**
 * Питомец в кресле.
 * Картинка — анимированный GIF, анимация автоматическая через Coil.
 *
 * @param mood   "HAPPY" | "NEUTRAL" | "SAD"
 * @param stage  "BABY" | "TEEN" | "ADULT"
 * @param enabled  false — рисуется статичный первый кадр без анимации
 *                 (для скрытой вкладки, чтобы не тратить CPU)
 */
@Composable
fun FinikInArmchair(
    modifier: Modifier = Modifier,
    skinId: String = "cat_black",
    mood: String = "HAPPY",
    stage: String = "BABY",
    accessoryId: String = "none",
    enabled: Boolean = true,
    debugTaps: Boolean = false,
    onClick: () -> Unit = {}
) {
    val res = petDrawableFor(skinId, stage, mood)
    val accessory = remember(accessoryId) { findAccessory(accessoryId) }
    val taps = remember { mutableStateListOf<Offset>() }

    BoxWithConstraints(
        modifier = modifier
            .size(240.dp),                // ← ФИКСИРУЕМ РАЗМЕР, важно!
        contentAlignment = Alignment.Center
    ) {
        val w = maxWidth
        val h = maxHeight

        // 1. GIF-кот
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(res)
                .decoderFactory(GifDecoder.Factory())
                .crossfade(false)
                .build(),
            contentDescription = "Питомец",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Аксессуары поверх — все сразу, в порядке zOrder
        val accessories = remember(accessoryId) { parseAccessories(accessoryId) }

        accessories.forEach { accessory ->
            val placement = accessory.placementFor(stage)
            val accWidth = w * placement.widthRatio
            val offsetX = w * placement.cx - accWidth / 2
            val offsetY = h * placement.cy - accWidth / 2

            Image(
                painter = painterResource(accessory.drawableRes),
                contentDescription = accessory.title,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = offsetX, y = offsetY)
                    .size(accWidth)
            )
        }

        // 3. Debug: ловим тапы, логируем, рисуем точки
        if (debugTaps) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val nx = offset.x / size.width
                            val ny = offset.y / size.height
                            Log.d("PET", "tap: nx=${"%.3f".format(nx)} ny=${"%.3f".format(ny)} (px=${offset.x.roundToInt()},${offset.y.roundToInt()})")
                            taps.add(offset)
                        }
                    }
            )

            // Рисуем точки в местах тапов
            Canvas(modifier = Modifier.fillMaxSize()) {
                taps.forEachIndexed { index, pos ->
                    drawCircle(
                        color = Color.Magenta,
                        radius = 8f,
                        center = pos
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 4f,
                        center = pos
                    )
                }
            }
        } else {
            // Обычный клик по коту
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onClick() }
            )
        }
    }
}

/**
 * Выбор GIF по стадии и настроению.
 */
private fun petDrawableFor(skinId: String, stage: String, mood: String): Int = when (skinId) {
    "cat_black" -> when(stage) {
        "BABY" -> when (mood) {
            "HAPPY" -> R.drawable.cat_black_happy
            "SAD"   -> R.drawable.cat_black_sad
            else    -> R.drawable.cat_black_neutral
        }
        "TEEN" -> when (mood) {
            "HAPPY" -> R.drawable.cat_black_happy_teen
            "SAD"   -> R.drawable.cat_black_sad_teen
            else    -> R.drawable.cat_black_neutral_teen
        }
        "ADULT" -> when (mood) {
            "HAPPY" -> R.drawable.cat_black_happy_adult
            "SAD"   -> R.drawable.cat_black_sad_adult
            else    -> R.drawable.cat_black_neutral_adult
        }
        else -> R.drawable.cat_black_neutral
    }
    "cat_orange" -> when(stage) {
        "BABY" -> when (mood) {
            "HAPPY" -> R.drawable.cat_brown_happy
            "SAD"   -> R.drawable.cat_brown_sad
            else    -> R.drawable.cat_brown_neutral
        }
        "TEEN" -> when (mood) {
            "HAPPY" -> R.drawable.cat_brown_happy
            "SAD"   -> R.drawable.cat_brown_sad
            else    -> R.drawable.cat_brown_neutral
        }
        "ADULT" -> when (mood) {
            "HAPPY" -> R.drawable.cat_brown_happy
            "SAD"   -> R.drawable.cat_brown_sad
            else    -> R.drawable.cat_brown_neutral
        }
        else -> R.drawable.cat_brown_neutral
    }
    "cat_nlo" -> when(stage) {
        "BABY" -> when (mood) {
            "HAPPY" -> R.drawable.cat_nlo_happy
            "SAD"   -> R.drawable.cat_nlo_sad
            else    -> R.drawable.cat_nlo_neutral
        }
        "TEEN" -> when (mood) {
            "HAPPY" -> R.drawable.cat_nlo_happy_teen
            "SAD"   -> R.drawable.cat_nlo_sad_teen
            else    -> R.drawable.cat_nlo_neutral_teen
        }
        "ADULT" -> when (mood) {
            "HAPPY" -> R.drawable.cat_nlo_happy_adult
            "SAD"   -> R.drawable.cat_nlo_sad_adult
            else    -> R.drawable.cat_nlo_neutral_adult
        }
        else -> R.drawable.cat_nlo_neutral
    }
    else -> R.drawable.cat_nlo_neutral
}