package ru.finpet.kids.feature.finik

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.request.ImageRequest
import ru.finpet.kids.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.remember
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max

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
    accessoryId: String = "none",           // ← НОВОЕ
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    val res = petDrawableFor(skinId, stage, mood)
    val accessory = remember(accessoryId) { findAccessory(accessoryId) }

    BoxWithConstraints(
        modifier = modifier
            .size(240.dp)                    // фиксированный размер кота
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        val w = maxWidth

        // 1. GIF-кот на фоне
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(res)
                .decoderFactory(GifDecoder.Factory())
                .crossfade(false)
                .build(),
            contentDescription = "Питомец $mood",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Аксессуар поверх — если выбран
        if (accessory.id != "none" && accessory.drawableRes != 0) {
            val accWidth = w * accessory.widthRatio
            // Позиционируем по центру аксессуара
            // Смещение = (центр кота) - (половина аксессуара)
            val offsetX = w * accessory.cx - accWidth / 2
            val offsetY = w * accessory.cy - accWidth / 2   // считаем аксессуар квадратным (Fit сохранит пропорции)

            Image(
                painter = painterResource(accessory.drawableRes),
                contentDescription = accessory.title,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .offset(x = offsetX, y = offsetY)
                    .size(accWidth)
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