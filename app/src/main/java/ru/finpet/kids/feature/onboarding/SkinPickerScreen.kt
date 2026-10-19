package ru.finpet.kids.feature.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.request.ImageRequest
import ru.finpet.kids.R

data class SkinOption(val id: String, val title: String)

val AVAILABLE_SKINS = listOf(
    SkinOption("cat_black",  "Чёрный котик"),
    SkinOption("cat_brown", "Коричневый котик"),
    SkinOption("cat_nlo",  "Инопланетный котик")
)

/**
 * область нажатия стрелок на фоне экрана выбора.
 */
private data class Hotspot(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
)

private val LEFT_ARROW  = Hotspot(0.0065f, 0.5737f, 0.1296f, 0.6784f)
private val RIGHT_ARROW = Hotspot(0.8630f, 0.5737f, 0.9648f, 0.6722f)
private val PLAY_BUTTON = Hotspot(0.185f, 0.774f, 0.835f, 0.873f)

// координаты кота
private const val CAT_CENTER_X = 0.5f
private const val CAT_CENTER_Y = 0.6f
private const val CAT_WIDTH_RATIO = 0.55f

@Composable
@Suppress("UnusedBoxWithConstraintsScope")
fun SkinPickerScreen(
    onSkinSelected: (String) -> Unit
) {
    var currentIndex by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    fun goNext() {
        currentIndex = (currentIndex + 1) % AVAILABLE_SKINS.size
    }
    fun goPrev() {
        currentIndex = (currentIndex - 1 + AVAILABLE_SKINS.size) % AVAILABLE_SKINS.size
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFfff9e6))
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1073f / 2088f)
                .align(Alignment.Center)
                .pointerInput(Unit) {
                    var totalDrag = 0f
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            val threshold = size.width * 0.12f
                            when {
                                totalDrag < -threshold -> goNext()
                                totalDrag > threshold  -> goPrev()
                            }
                            totalDrag = 0f
                        },
                        onHorizontalDrag = { _, delta -> totalDrag += delta }
                    )
                }
        ) {
            val w = maxWidth
            val h = maxHeight

            // фоновое изображение
            Image(
                painter = painterResource(R.drawable.choose_character),
                contentDescription = "Выбор питомца",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize()
            )

            // отображение кота в центре
            val catW = w * CAT_WIDTH_RATIO
            val catH = catW

            AnimatedContent(
                targetState = currentIndex,
                transitionSpec = {
                    val dir = if (targetState > initialState) 1 else -1
                    (slideInHorizontally { fullW -> dir * fullW } + fadeIn(tween(220))) togetherWith
                            (slideOutHorizontally { fullW -> -dir * fullW } + fadeOut(tween(220)))
                },
                label = "cat_switch",
                modifier = Modifier
                    .offset(
                        x = w * CAT_CENTER_X - catW / 2,
                        y = h * CAT_CENTER_Y - catH / 2
                    )
                    .size(catW, catH)
            ) { index ->
                val skin = AVAILABLE_SKINS[index]
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(previewDrawableFor(skin.id))
                        .decoderFactory(GifDecoder.Factory())
                        .crossfade(false)
                        .build(),
                    contentDescription = skin.title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 3. Хитбоксы стрелок
            HotspotBox(
                hotspot = LEFT_ARROW,
                w = w, h = h,
                enabled = true,
                onClick = ::goPrev
            )
            HotspotBox(
                hotspot = RIGHT_ARROW,
                w = w, h = h,
                enabled = true,
                onClick = ::goNext
            )
            HotspotBox(
                hotspot = PLAY_BUTTON,
                w = w, h = h,
                enabled = true,
                onClick = { onSkinSelected(AVAILABLE_SKINS[currentIndex].id) }
            )
        }
    }
}

@Composable
private fun HotspotBox(
    hotspot: Hotspot,
    w: androidx.compose.ui.unit.Dp,
    h: androidx.compose.ui.unit.Dp,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .offset(x = w * hotspot.left, y = h * hotspot.top)
            .size(w * (hotspot.right - hotspot.left), h * (hotspot.bottom - hotspot.top))
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
    )
}

/**
 * Превью скина для пикера (статика, neutral).
 */
fun previewDrawableFor(skinId: String): Int = when (skinId) {
    "cat_black"  -> R.drawable.cat_black_neutral
    "cat_brown" -> R.drawable.cat_brown_neutral
    "cat_nlo"  -> R.drawable.cat_nlo_neutral
    else         -> R.drawable.cat_black_neutral
}