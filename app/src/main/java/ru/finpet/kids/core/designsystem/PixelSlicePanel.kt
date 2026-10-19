package ru.finpet.kids.core.designsystem

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import ru.finpet.kids.R
import kotlin.math.roundToInt

/**
 * 9-slice рендеринг пиксельной панели на основе top_part.png, middle_part.png, bottom_part.png.
 *
 * Размер исходных ассетов: 951x92 px.
 * - Угловые элементы (кривые скругления): ширина 53 px, высота 92 px.
 * - Прямые горизонтальные кромки: x = 53..898 (ширина 845 px).
 * - Боковые вертикальные кромки: ширина 24 px.
 *
 * Благодаря 9-slice углы ВСЕГДА сохраняют правильное соотношение сторон (не растягиваются по горизонтали)
 * вне зависимости от ширины и высоты карточки!
 */
fun DrawScope.drawPixelSlicedPanel(
    topBmp: ImageBitmap,
    midBmp: ImageBitmap,
    botBmp: ImageBitmap,
    sliceHeightPx: Float,
    colorFilter: ColorFilter? = null
) {
    val dstW = size.width.roundToInt()
    val dstH = size.height.roundToInt()
    if (dstW <= 0 || dstH <= 0) return

    val srcCornerW = 53
    val srcSliceH = 92
    val srcMidBorderW = 24

    // Высота верхней и нижней полосы с ограничением, чтобы оставалось место под середину
    val sliceH = sliceHeightPx.roundToInt()
        .coerceAtMost((dstH / 2).coerceAtLeast(1))
        .coerceAtLeast(1)

    // Масштаб для сохранения пропорций углов 1:1
    val scale = sliceH.toFloat() / srcSliceH.toFloat()
    val cornerW = (srcCornerW * scale).roundToInt().coerceAtMost(dstW / 2)
    val midBorderW = (srcMidBorderW * scale).roundToInt().coerceAtMost(dstW / 2)

    val midH = (dstH - sliceH * 2).coerceAtLeast(0)
    val centerW = (dstW - cornerW * 2).coerceAtLeast(0)
    val midCenterW = (dstW - midBorderW * 2).coerceAtLeast(0)

    // --- 1. ВЕРХНЯЯ ЧАСТЬ (top_part) ---
    // Левый верхний угол (пропорции строго 1:1, не растягивается)
    if (cornerW > 0 && sliceH > 0) {
        drawImage(
            image = topBmp,
            srcOffset = IntOffset(0, 0),
            srcSize = IntSize(srcCornerW, srcSliceH),
            dstOffset = IntOffset(0, 0),
            dstSize = IntSize(cornerW, sliceH),
            filterQuality = FilterQuality.None,
            colorFilter = colorFilter
        )
    }

    // Верхняя прямая планка (растягивается по горизонтали между углами)
    if (centerW > 0 && sliceH > 0) {
        drawImage(
            image = topBmp,
            srcOffset = IntOffset(srcCornerW, 0),
            srcSize = IntSize(topBmp.width - srcCornerW * 2, srcSliceH),
            dstOffset = IntOffset(cornerW, 0),
            dstSize = IntSize(centerW, sliceH),
            filterQuality = FilterQuality.None,
            colorFilter = colorFilter
        )
    }

    // Правый верхний угол (пропорции строго 1:1)
    if (cornerW > 0 && sliceH > 0) {
        drawImage(
            image = topBmp,
            srcOffset = IntOffset(topBmp.width - srcCornerW, 0),
            srcSize = IntSize(srcCornerW, srcSliceH),
            dstOffset = IntOffset(dstW - cornerW, 0),
            dstSize = IntSize(cornerW, sliceH),
            filterQuality = FilterQuality.None,
            colorFilter = colorFilter
        )
    }

    // --- 2. СРЕДНЯЯ ЧАСТЬ (middle_part) ---
    if (midH > 0) {
        // Левая вертикальная рамка
        if (midBorderW > 0) {
            drawImage(
                image = midBmp,
                srcOffset = IntOffset(0, 0),
                srcSize = IntSize(srcMidBorderW, midBmp.height),
                dstOffset = IntOffset(0, sliceH),
                dstSize = IntSize(midBorderW, midH),
                filterQuality = FilterQuality.None,
                colorFilter = colorFilter
            )
        }

        // Центральное пергаментное заполнение
        if (midCenterW > 0) {
            drawImage(
                image = midBmp,
                srcOffset = IntOffset(srcMidBorderW, 0),
                srcSize = IntSize(midBmp.width - srcMidBorderW * 2, midBmp.height),
                dstOffset = IntOffset(midBorderW, sliceH),
                dstSize = IntSize(midCenterW, midH),
                filterQuality = FilterQuality.None,
                colorFilter = colorFilter
            )
        }

        // Правая вертикальная рамка
        if (midBorderW > 0) {
            drawImage(
                image = midBmp,
                srcOffset = IntOffset(midBmp.width - srcMidBorderW, 0),
                srcSize = IntSize(srcMidBorderW, midBmp.height),
                dstOffset = IntOffset(dstW - midBorderW, sliceH),
                dstSize = IntSize(midBorderW, midH),
                filterQuality = FilterQuality.None,
                colorFilter = colorFilter
            )
        }
    }

    // --- 3. НИЖНЯЯ ЧАСТЬ (bottom_part) ---
    val botY = dstH - sliceH
    // Левый нижний угол (пропорции строго 1:1)
    if (cornerW > 0 && sliceH > 0) {
        drawImage(
            image = botBmp,
            srcOffset = IntOffset(0, 0),
            srcSize = IntSize(srcCornerW, botBmp.height),
            dstOffset = IntOffset(0, botY),
            dstSize = IntSize(cornerW, sliceH),
            filterQuality = FilterQuality.None,
            colorFilter = colorFilter
        )
    }

    // Нижняя прямая планка
    if (centerW > 0 && sliceH > 0) {
        drawImage(
            image = botBmp,
            srcOffset = IntOffset(srcCornerW, 0),
            srcSize = IntSize(botBmp.width - srcCornerW * 2, botBmp.height),
            dstOffset = IntOffset(cornerW, botY),
            dstSize = IntSize(centerW, sliceH),
            filterQuality = FilterQuality.None,
            colorFilter = colorFilter
        )
    }

    // Правый нижний угол (пропорции строго 1:1)
    if (cornerW > 0 && sliceH > 0) {
        drawImage(
            image = botBmp,
            srcOffset = IntOffset(botBmp.width - srcCornerW, 0),
            srcSize = IntSize(srcCornerW, botBmp.height),
            dstOffset = IntOffset(dstW - cornerW, botY),
            dstSize = IntSize(cornerW, sliceH),
            filterQuality = FilterQuality.None,
            colorFilter = colorFilter
        )
    }
}

/**
 * Отрисовка горизонтальной планки (кнопки, хедера, однострочной плашки).
 * Левая и правая заглушки сохраняют пропорции 1:1, а при увеличении высоты (многострочный текст)
 * верхние и нижние границы НЕ распухают и сохраняют постоянную пиксельную толщину.
 */
fun DrawScope.drawPixelHorizontalBar(
    leftBmp: ImageBitmap,
    midBmp: ImageBitmap,
    rightBmp: ImageBitmap,
    colorFilter: ColorFilter? = null,
    standardHeightDp: Dp = 40.dp
) {
    val dstW = size.width.roundToInt()
    val dstH = size.height.roundToInt()
    if (dstW <= 0 || dstH <= 0) return

    val srcCornerSize = 28
    val srcH = 92
    val srcMidH = 36 // 92 - 28 * 2 = 36 (строки 28..64)
    val srcMidW = midBmp.width

    val standardHPx = standardHeightDp.toPx()
    // Базовый масштаб для углов и толщины рамок не раздувается на высоких кнопках
    val scale = if (dstH.toFloat() > standardHPx) {
        standardHPx / srcH.toFloat()
    } else {
        dstH.toFloat() / srcH.toFloat()
    }

    val capW = (srcCornerSize * scale).roundToInt().coerceAtMost(dstW / 2)
    val capH = (srcCornerSize * scale).roundToInt().coerceAtMost(dstH / 2)
    val midW = (dstW - capW * 2).coerceAtLeast(0)
    val midH = (dstH - capH * 2).coerceAtLeast(0)

    // --- 1. ВЕРХНИЙ РЯД (высота capH) ---
    if (capH > 0) {
        if (capW > 0) {
            // Верхний левый угол
            drawImage(
                image = leftBmp,
                srcOffset = IntOffset(0, 0),
                srcSize = IntSize(srcCornerSize, srcCornerSize),
                dstOffset = IntOffset(0, 0),
                dstSize = IntSize(capW, capH),
                filterQuality = FilterQuality.None,
                colorFilter = colorFilter
            )
            // Верхний правый угол
            drawImage(
                image = rightBmp,
                srcOffset = IntOffset(rightBmp.width - srcCornerSize, 0),
                srcSize = IntSize(srcCornerSize, srcCornerSize),
                dstOffset = IntOffset(dstW - capW, 0),
                dstSize = IntSize(capW, capH),
                filterQuality = FilterQuality.None,
                colorFilter = colorFilter
            )
        }
        if (midW > 0) {
            // Верхняя горизонтальная планка
            drawImage(
                image = midBmp,
                srcOffset = IntOffset(0, 0),
                srcSize = IntSize(srcMidW, srcCornerSize),
                dstOffset = IntOffset(capW, 0),
                dstSize = IntSize(midW, capH),
                filterQuality = FilterQuality.None,
                colorFilter = colorFilter
            )
        }
    }

    // --- 2. СРЕДНИЙ РЯД (высота midH — растягивается вертикально при росте высоты) ---
    if (midH > 0) {
        if (capW > 0) {
            // Левая вертикальная кромка
            drawImage(
                image = leftBmp,
                srcOffset = IntOffset(0, srcCornerSize),
                srcSize = IntSize(srcCornerSize, srcMidH),
                dstOffset = IntOffset(0, capH),
                dstSize = IntSize(capW, midH),
                filterQuality = FilterQuality.None,
                colorFilter = colorFilter
            )
            // Правая вертикальная кромка
            drawImage(
                image = rightBmp,
                srcOffset = IntOffset(rightBmp.width - srcCornerSize, srcCornerSize),
                srcSize = IntSize(srcCornerSize, srcMidH),
                dstOffset = IntOffset(dstW - capW, capH),
                dstSize = IntSize(capW, midH),
                filterQuality = FilterQuality.None,
                colorFilter = colorFilter
            )
        }
        if (midW > 0) {
            // Центральное заполнение кнопки
            drawImage(
                image = midBmp,
                srcOffset = IntOffset(0, srcCornerSize),
                srcSize = IntSize(srcMidW, srcMidH),
                dstOffset = IntOffset(capW, capH),
                dstSize = IntSize(midW, midH),
                filterQuality = FilterQuality.None,
                colorFilter = colorFilter
            )
        }
    }

    // --- 3. НИЖНИЙ РЯД (высота capH) ---
    val botY = dstH - capH
    if (capH > 0) {
        if (capW > 0) {
            // Нижний левый угол
            drawImage(
                image = leftBmp,
                srcOffset = IntOffset(0, srcH - srcCornerSize),
                srcSize = IntSize(srcCornerSize, srcCornerSize),
                dstOffset = IntOffset(0, botY),
                dstSize = IntSize(capW, capH),
                filterQuality = FilterQuality.None,
                colorFilter = colorFilter
            )
            // Нижний правый угол
            drawImage(
                image = rightBmp,
                srcOffset = IntOffset(rightBmp.width - srcCornerSize, rightBmp.height - srcCornerSize),
                srcSize = IntSize(srcCornerSize, srcCornerSize),
                dstOffset = IntOffset(dstW - capW, botY),
                dstSize = IntSize(capW, capH),
                filterQuality = FilterQuality.None,
                colorFilter = colorFilter
            )
        }
        if (midW > 0) {
            // Нижняя горизонтальная планка
            drawImage(
                image = midBmp,
                srcOffset = IntOffset(0, midBmp.height - srcCornerSize),
                srcSize = IntSize(srcMidW, srcCornerSize),
                dstOffset = IntOffset(capW, botY),
                dstSize = IntSize(midW, capH),
                filterQuality = FilterQuality.None,
                colorFilter = colorFilter
            )
        }
    }
}

/**
 * Модификатор для добавления пиксельного фона на основе 9-slice рендеринга
 * с идеальным сохранением пропорций углов без сплющивания.
 */
fun Modifier.pixelPanelBackground(
    cornerSizeDp: Dp = 18.dp,
    sliceHeightDp: Dp = 18.dp,
    containerColor: Color? = null
): Modifier = composed {
    val topBmp = ImageBitmap.imageResource(id = R.drawable.top_part)
    val midBmp = ImageBitmap.imageResource(id = R.drawable.middle_part)
    val botBmp = ImageBitmap.imageResource(id = R.drawable.bottom_part)

    val colorFilter = if (containerColor != null && containerColor != Color.Unspecified) {
        ColorFilter.tint(containerColor, BlendMode.Modulate)
    } else {
        null
    }

    this.drawBehind {
        drawPixelSlicedPanel(
            topBmp = topBmp,
            midBmp = midBmp,
            botBmp = botBmp,
            sliceHeightPx = sliceHeightDp.toPx(),
            colorFilter = colorFilter
        )
    }
}

/**
 * Модификатор фона для хедеров и однострочных плашек на основе left_part, middle_little_part, right_part.
 */
fun Modifier.pixelBarBackground(): Modifier = composed {
    val leftBmp = ImageBitmap.imageResource(id = R.drawable.left_part)
    val midBmp = ImageBitmap.imageResource(id = R.drawable.middle_little_part)
    val rightBmp = ImageBitmap.imageResource(id = R.drawable.right_part)

    this.drawBehind {
        drawPixelHorizontalBar(
            leftBmp = leftBmp,
            midBmp = midBmp,
            rightBmp = rightBmp
        )
    }
}

/**
 * Модификатор фона для кнопок на основе button_left_part, button_middle_little_part, button_right_part.
 * Поддерживает аутентичную покраску кнопки через containerColor (BlendMode.Modulate).
 */
fun Modifier.pixelButtonBackground(containerColor: Color? = null): Modifier = composed {
    val leftBmp = ImageBitmap.imageResource(id = R.drawable.button_left_part)
    val midBmp = ImageBitmap.imageResource(id = R.drawable.button_middle_little_part)
    val rightBmp = ImageBitmap.imageResource(id = R.drawable.button_right_part)

    val colorFilter = if (containerColor != null && containerColor != Color.Unspecified) {
        ColorFilter.tint(containerColor, BlendMode.Modulate)
    } else {
        null
    }

    val uiScale = LocalUiScale.current
    val standardHeightDp = (40.dp * uiScale).coerceIn(26.dp, 40.dp)

    this.drawBehind {
        drawPixelHorizontalBar(
            leftBmp = leftBmp,
            midBmp = midBmp,
            rightBmp = rightBmp,
            colorFilter = colorFilter,
            standardHeightDp = standardHeightDp
        )
    }
}

/**
 * Пиксельная панель (контейнер) для карточек и окон в новом стиле (9-slice)
 */
@Composable
fun PixelPanel(
    modifier: Modifier = Modifier,
    cornerSizeDp: Dp = 18.dp,
    sliceHeightDp: Dp = 18.dp,
    containerColor: Color? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .pixelPanelBackground(
                cornerSizeDp = cornerSizeDp,
                sliceHeightDp = sliceHeightDp,
                containerColor = containerColor
            )
            .padding(contentPadding),
        contentAlignment = Alignment.TopStart
    ) {
        content()
    }
}

/**
 * Горизонтальная плашка-хедер или информационная панель в стиле Stardew Valley.
 * Идеально подходит для кнопок, однострочных баннеров и заголовков секций.
 */
@Composable
fun PixelBar(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier
            .pixelBarBackground()
            .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

/**
 * 9-slice рендеринг аутентичной белой пиксельной рамки (white_pixel_box.png).
 * Точно повторяет белую плашку под монетки из хедера локаций.
 */
fun DrawScope.drawWhitePixelBox(
    boxBmp: ImageBitmap
) {
    val dstW = size.width.roundToInt()
    val dstH = size.height.roundToInt()
    if (dstW <= 0 || dstH <= 0) return

    val srcCornerW = 18
    val srcCornerH = 18
    val scale = dstH.toFloat() / boxBmp.height.toFloat()
    val cornerW = (srcCornerW * scale).roundToInt().coerceAtMost(dstW / 2)
    val cornerH = (srcCornerH * scale).roundToInt().coerceAtMost(dstH / 2)
    val midW = (dstW - cornerW * 2).coerceAtLeast(0)
    val midH = (dstH - cornerH * 2).coerceAtLeast(0)

    val srcMidW = boxBmp.width - srcCornerW * 2
    val srcMidH = boxBmp.height - srcCornerH * 2

    // 1. Верхний ряд
    drawImage(
        image = boxBmp,
        srcOffset = IntOffset(0, 0),
        srcSize = IntSize(srcCornerW, srcCornerH),
        dstOffset = IntOffset(0, 0),
        dstSize = IntSize(cornerW, cornerH),
        filterQuality = FilterQuality.None
    )
    if (midW > 0) {
        drawImage(
            image = boxBmp,
            srcOffset = IntOffset(srcCornerW, 0),
            srcSize = IntSize(srcMidW, srcCornerH),
            dstOffset = IntOffset(cornerW, 0),
            dstSize = IntSize(midW, cornerH),
            filterQuality = FilterQuality.None
        )
    }
    drawImage(
        image = boxBmp,
        srcOffset = IntOffset(boxBmp.width - srcCornerW, 0),
        srcSize = IntSize(srcCornerW, srcCornerH),
        dstOffset = IntOffset(dstW - cornerW, 0),
        dstSize = IntSize(cornerW, cornerH),
        filterQuality = FilterQuality.None
    )

    // 2. Средний ряд
    if (midH > 0) {
        drawImage(
            image = boxBmp,
            srcOffset = IntOffset(0, srcCornerH),
            srcSize = IntSize(srcCornerW, srcMidH),
            dstOffset = IntOffset(0, cornerH),
            dstSize = IntSize(cornerW, midH),
            filterQuality = FilterQuality.None
        )
        if (midW > 0) {
            drawImage(
                image = boxBmp,
                srcOffset = IntOffset(srcCornerW, srcCornerH),
                srcSize = IntSize(srcMidW, srcMidH),
                dstOffset = IntOffset(cornerW, cornerH),
                dstSize = IntSize(midW, midH),
                filterQuality = FilterQuality.None
            )
        }
        drawImage(
            image = boxBmp,
            srcOffset = IntOffset(boxBmp.width - srcCornerW, srcCornerH),
            srcSize = IntSize(srcCornerW, srcMidH),
            dstOffset = IntOffset(dstW - cornerW, cornerH),
            dstSize = IntSize(cornerW, midH),
            filterQuality = FilterQuality.None
        )
    }

    // 3. Нижний ряд
    drawImage(
        image = boxBmp,
        srcOffset = IntOffset(0, boxBmp.height - srcCornerH),
        srcSize = IntSize(srcCornerW, srcCornerH),
        dstOffset = IntOffset(0, dstH - cornerH),
        dstSize = IntSize(cornerW, cornerH),
        filterQuality = FilterQuality.None
    )
    if (midW > 0) {
        drawImage(
            image = boxBmp,
            srcOffset = IntOffset(srcCornerW, boxBmp.height - srcCornerH),
            srcSize = IntSize(srcMidW, srcCornerH),
            dstOffset = IntOffset(cornerW, dstH - cornerH),
            dstSize = IntSize(midW, cornerH),
            filterQuality = FilterQuality.None
        )
    }
    drawImage(
        image = boxBmp,
        srcOffset = IntOffset(boxBmp.width - srcCornerW, boxBmp.height - srcCornerH),
        srcSize = IntSize(srcCornerW, srcCornerH),
        dstOffset = IntOffset(dstW - cornerW, dstH - cornerH),
        dstSize = IntSize(cornerW, cornerH),
        filterQuality = FilterQuality.None
    )
}

/**
 * Модификатор для пиксельной белой рамки (на основе белого квадрата из хедера).
 */
fun Modifier.whitePixelBoxBackground(): Modifier = composed {
    val boxBmp = ImageBitmap.imageResource(id = R.drawable.white_pixel_box)
    drawBehind {
        drawWhitePixelBox(boxBmp)
    }
}
