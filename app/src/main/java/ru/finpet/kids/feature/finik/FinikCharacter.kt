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
    mood: String = "HAPPY",
    stage: String = "BABY",
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    val res = petDrawableFor(stage, mood)

    Box(
        modifier = modifier
            .size(240.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(res)
                .decoderFactory(GifDecoder.Factory())
                .crossfade(false)          // без плавных переходов между кадрами
                .build(),
            contentDescription = "Питомец $mood",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Выбор GIF по стадии и настроению.
 */
private fun petDrawableFor(stage: String, mood: String): Int = when (stage) {
    "BABY" -> when (mood) {
        "HAPPY" -> R.drawable.cat_black
        "SAD"   -> R.drawable.cat_black
        else    -> R.drawable.cat_black
    }
    "TEEN" -> when (mood) {
        "HAPPY" -> R.drawable.cat_black
        "SAD"   -> R.drawable.cat_black
        else    -> R.drawable.cat_black
    }
    "ADULT" -> when (mood) {
        "HAPPY" -> R.drawable.cat_black
        "SAD"   -> R.drawable.cat_black
        else    -> R.drawable.cat_black
    }
    else -> R.drawable.cat_nlo
}