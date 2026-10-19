package ru.finpet.kids.feature.map.minigames

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.finpet.kids.R
import ru.finpet.kids.core.designsystem.AppFontFamily
import ru.finpet.kids.core.designsystem.KeepScreenImmersive
import ru.finpet.kids.core.designsystem.PixelGoldBright
import ru.finpet.kids.core.designsystem.PixelGoldDark
import ru.finpet.kids.core.designsystem.PixelPanel
import ru.finpet.kids.core.designsystem.PixelTextDark
import kotlin.math.abs

enum class CatMood {
    NORMAL,
    HAPPY,
    SAD
}

private data class SwapAnimState(
    val r1: Int, val c1: Int,
    val r2: Int, val c2: Int,
    val progress: Float
)

private data class FallingBall(
    val id: Long,
    val color: BallColor,
    val col: Int,
    val fromRow: Float,
    val toRow: Int
)

/**
 * Бесконечная аркадная мини-игра «Три в ряд» для интереса и рекордов (без монет).
 * Плавные анимации обмена, исчезновения и падения шариков.
 * 9-slice всплывающие плашки комбо и бустеров.
 * Увеличенный адаптивный шрифт счета и поднятый рекорд.
 * Крупная анимированная иконка эмоций котика.
 */
@Composable
fun MatchThreeGameDialog(
    currentPeriodIndex: Int = 1,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Проверка и начисление бустеров каждую неделю (+3 каждого)
    LaunchedEffect(currentPeriodIndex) {
        MatchThreePreferences.checkAndGrantWeekly(context, currentPeriodIndex)
    }

    var board by remember { mutableStateOf(MatchThreeGameLogic.generateInitialBoard()) }
    var selectedCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var isResolving by remember { mutableStateOf(false) }

    // Анимационные состояния
    var swapAnim by remember { mutableStateOf<SwapAnimState?>(null) }
    var clearingCells by remember { mutableStateOf<Set<Pair<Int, Int>>>(emptySet()) }
    val clearProgress = remember { Animatable(1f) }

    var fallingBalls by remember { mutableStateOf<List<FallingBall>?>(null) }
    val dropProgress = remember { Animatable(0f) }

    var catMood by remember { mutableStateOf(CatMood.NORMAL) }
    val catBounce = remember { Animatable(1f) }

    var score by remember { mutableIntStateOf(0) }
    var highScore by remember { mutableIntStateOf(MatchThreePreferences.getHighScore(context)) }

    var singleCount by remember { mutableIntStateOf(MatchThreePreferences.getBoosterCount(context, BoosterType.SINGLE)) }
    var columnCount by remember { mutableIntStateOf(MatchThreePreferences.getBoosterCount(context, BoosterType.COLUMN)) }
    var rowCount by remember { mutableIntStateOf(MatchThreePreferences.getBoosterCount(context, BoosterType.ROW)) }
    var areaCount by remember { mutableIntStateOf(MatchThreePreferences.getBoosterCount(context, BoosterType.AREA)) }

    var activeBooster by remember { mutableStateOf<BoosterType?>(null) }
    var statusBannerText by remember { mutableStateOf<String?>(null) }
    var comboBonusInfo by remember { mutableStateOf<Pair<Int, Int>?>(null) } // combo x -> points

    var nextBallId by remember { mutableStateOf(1000L) }

    fun setMood(mood: CatMood, durationMs: Long = 1300L) {
        catMood = mood
        if (mood == CatMood.HAPPY) {
            scope.launch {
                catBounce.snapTo(1f)
                catBounce.animateTo(1.22f, tween(90, easing = FastOutSlowInEasing))
                catBounce.animateTo(1.0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
            }
        }
        scope.launch {
            delay(durationMs)
            if (catMood == mood) {
                catMood = CatMood.NORMAL
            }
        }
    }

    fun animateClearAndDrop(affectedCells: Set<Pair<Int, Int>>, onDone: suspend () -> Unit) {
        scope.launch {
            clearingCells = affectedCells
            clearProgress.snapTo(1f)
            clearProgress.animateTo(0f, tween(180, easing = FastOutSlowInEasing))

            // Очищаем ячейки на доске
            val cleared = Array(MatchThreeGameLogic.ROWS) { r ->
                Array(MatchThreeGameLogic.COLS) { c ->
                    if ((r to c) in affectedCells) null else board[r][c]
                }
            }
            board = cleared
            clearingCells = emptySet()

            // Формируем список падающих шариков для плавной анимации падения
            val falls = mutableListOf<FallingBall>()
            for (c in 0 until MatchThreeGameLogic.COLS) {
                var writeRow = MatchThreeGameLogic.ROWS - 1
                for (r in (MatchThreeGameLogic.ROWS - 1) downTo 0) {
                    val b = board[r][c]
                    if (b != null) {
                        falls.add(FallingBall(nextBallId++, b, c, r.toFloat(), writeRow))
                        writeRow--
                    }
                }
                var spawnIdx = 1
                for (r in writeRow downTo 0) {
                    val b = BallColor.random()
                    falls.add(FallingBall(nextBallId++, b, c, -spawnIdx.toFloat(), r))
                    spawnIdx++
                }
            }

            fallingBalls = falls
            dropProgress.snapTo(0f)
            dropProgress.animateTo(1f, tween(230, easing = FastOutSlowInEasing))

            // Фиксируем упавшие шарики в состояние доски
            val filled = Array(MatchThreeGameLogic.ROWS) { arrayOfNulls<BallColor>(MatchThreeGameLogic.COLS) }
            for (fb in falls) {
                filled[fb.toRow][fb.col] = fb.color
            }
            board = filled
            fallingBalls = null

            onDone()
        }
    }

    fun resolveCascades() {
        scope.launch {
            isResolving = true
            var combo = 1
            while (true) {
                val matches = MatchThreeGameLogic.findMatches(board)
                if (matches.isEmpty()) break

                setMood(CatMood.HAPPY)
                val points = matches.size * 10 * combo
                score += points
                if (score > highScore) {
                    highScore = score
                    MatchThreePreferences.saveHighScore(context, score)
                }

                if (combo > 1) {
                    comboBonusInfo = combo to points
                }

                // Анимируем исчезновение совпавших шариков и плавное падение новых
                var cascadeDone = false
                animateClearAndDrop(matches) {
                    cascadeDone = true
                }
                while (!cascadeDone) {
                    delay(30)
                }

                combo++
                delay(80)
            }

            comboBonusInfo = null

            // Проверка наличия ходов на поле
            if (!MatchThreeGameLogic.hasValidMoves(board)) {
                statusBannerText = "НЕТ ХОДОВ!\nПеремешиваем шарики..."
                setMood(CatMood.SAD)
                delay(750)
                val shuffled = Array(MatchThreeGameLogic.ROWS) { r ->
                    Array(MatchThreeGameLogic.COLS) { c -> board[r][c] }
                }
                MatchThreeGameLogic.shuffleBoard(shuffled)
                board = shuffled
                statusBannerText = null
            }

            isResolving = false
        }
    }

    fun trySwap(r1: Int, c1: Int, r2: Int, c2: Int) {
        if (isResolving || swapAnim != null || fallingBalls != null || clearingCells.isNotEmpty()) return

        val canSwap = MatchThreeGameLogic.isValidSwap(board, r1, c1, r2, c2)
        scope.launch {
            isResolving = true
            val anim = Animatable(0f)
            if (canSwap) {
                // Плавная анимация перемещения шариков навстречу друг другу
                anim.animateTo(1f, tween(160, easing = FastOutSlowInEasing)) {
                    swapAnim = SwapAnimState(r1, c1, r2, c2, value)
                }
                val updated = Array(MatchThreeGameLogic.ROWS) { r ->
                    Array(MatchThreeGameLogic.COLS) { c -> board[r][c] }
                }
                val temp = updated[r1][c1]
                updated[r1][c1] = updated[r2][c2]
                updated[r2][c2] = temp
                board = updated
                swapAnim = null
                selectedCell = null
                resolveCascades()
            } else {
                // Неверный ход: шарики мягко смещаются и возвращаются на место
                setMood(CatMood.SAD)
                anim.animateTo(0.42f, tween(90, easing = FastOutSlowInEasing)) {
                    swapAnim = SwapAnimState(r1, c1, r2, c2, value)
                }
                anim.animateTo(0f, tween(90, easing = FastOutSlowInEasing)) {
                    swapAnim = SwapAnimState(r1, c1, r2, c2, value)
                }
                swapAnim = null
                selectedCell = null
                isResolving = false
            }
        }
    }

    fun handleBoosterClick(type: BoosterType, targetR: Int, targetC: Int) {
        if (isResolving || swapAnim != null || fallingBalls != null || clearingCells.isNotEmpty()) return
        val success = MatchThreePreferences.consumeBooster(context, type)
        if (!success) {
            activeBooster = null
            return
        }

        when (type) {
            BoosterType.SINGLE -> singleCount = MatchThreePreferences.getBoosterCount(context, type)
            BoosterType.COLUMN -> columnCount = MatchThreePreferences.getBoosterCount(context, type)
            BoosterType.ROW -> rowCount = MatchThreePreferences.getBoosterCount(context, type)
            BoosterType.AREA -> areaCount = MatchThreePreferences.getBoosterCount(context, type)
        }
        activeBooster = null

        val affected = MatchThreeGameLogic.getBoosterAffectedCells(type, targetR, targetC)
        setMood(CatMood.HAPPY)
        val points = affected.size * 10
        score += points
        if (score > highScore) {
            highScore = score
            MatchThreePreferences.saveHighScore(context, score)
        }

        scope.launch {
            isResolving = true
            var done = false
            animateClearAndDrop(affected) {
                done = true
            }
            while (!done) {
                delay(30)
            }
            resolveCascades()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        KeepScreenImmersive()

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1B140B)),
            contentAlignment = Alignment.Center
        ) {
            val density = LocalDensity.current
            val canvasNativeWidth = 1073f
            val canvasNativeHeight = 2088f
            val targetAspect = canvasNativeWidth / canvasNativeHeight
            val currentAspect = maxWidth / maxHeight

            val (fieldWidth, fieldHeight) = if (currentAspect > targetAspect) {
                Pair(maxHeight * targetAspect, maxHeight)
            } else {
                Pair(maxWidth, maxWidth / targetAspect)
            }

            val scale = fieldWidth.value / canvasNativeWidth

            fun cPx(px: Float): Dp = with(density) { (px * scale).dp }

            val colOffsets = floatArrayOf(58f, 221f, 383f, 546f, 708f, 870f)
            val rowOffsets = floatArrayOf(209f, 372f, 534f, 696f, 859f, 1021f, 1183f, 1346f, 1508f, 1670f)
            val cellSizePx = 145f

            Box(modifier = Modifier.size(fieldWidth, fieldHeight)) {
                // 1. Полноэкранный фон игрового поля
                Image(
                    painter = painterResource(id = R.drawable.tri_vryad_field),
                    contentDescription = "Поле три в ряд",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.fillMaxSize()
                )

                // 2. Реакция котика (крупная иконка с анимацией подпрыгивания от радости)
                val catRes = when (catMood) {
                    CatMood.NORMAL -> R.drawable.normal_mood_3vryad
                    CatMood.HAPPY -> R.drawable.happy_mood_3vryad
                    CatMood.SAD -> R.drawable.sad_mood_3vryad
                }
                Image(
                    painter = painterResource(id = catRes),
                    contentDescription = "Реакция котика",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .offset(x = cPx(280f), y = cPx(0f))
                        .size(cPx(150f), cPx(140f))
                        .graphicsLayer {
                            scaleX = catBounce.value
                            scaleY = catBounce.value
                        }
                )

                // 3. Табло со счётом
                Box(
                    modifier = Modifier
                        .offset(x = cPx(441f), y = cPx(18f))
                        .size(cPx(347f), cPx(97f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.offset(y = cPx(-3f))
                    ) {
                        Text(
                            text = "$score",
                            fontFamily = AppFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = (32f * scale).coerceAtLeast(19f).sp,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // 4. Кнопка закрытия (справа сверху)
                Box(
                    modifier = Modifier
                        .offset(x = cPx(945f), y = cPx(25f))
                        .size(cPx(90f), cPx(80f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.close_sign),
                        contentDescription = "Закрыть игру",
                        modifier = Modifier.size(cPx(64f))
                    )
                }

                // 5. Игровая зона с шариками (с маскированием верхней кромки при падении)
                val gridLeftPx = colOffsets[0]
                val gridTopPx = rowOffsets[0]
                val gridWidthPx = colOffsets.last() - colOffsets.first() + cellSizePx
                val gridHeightPx = rowOffsets.last() - rowOffsets.first() + cellSizePx

                Box(
                    modifier = Modifier
                        .offset(x = cPx(gridLeftPx), y = cPx(gridTopPx))
                        .size(cPx(gridWidthPx), cPx(gridHeightPx))
                        .clipToBounds()
                ) {
                    val currentFalls = fallingBalls
                    if (currentFalls != null) {
                        // Режим плавной анимации падения шариков
                        for (fb in currentFalls) {
                            val curR = fb.fromRow + (fb.toRow - fb.fromRow) * dropProgress.value
                            val bx = colOffsets[fb.col] - gridLeftPx
                            val by = if (curR >= 0f) {
                                val r0 = curR.toInt().coerceIn(0, 8)
                                val frac = curR - r0
                                (rowOffsets[r0] + (rowOffsets[r0 + 1] - rowOffsets[r0]) * frac) - gridTopPx
                            } else {
                                (rowOffsets[0] + curR * 162.5f) - gridTopPx
                            }

                            Box(
                                modifier = Modifier
                                    .offset(x = cPx(bx), y = cPx(by))
                                    .size(cPx(cellSizePx), cPx(cellSizePx)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = fb.color.drawableRes),
                                    contentDescription = fb.color.name,
                                    modifier = Modifier.size(cPx(122f))
                                )
                            }
                        }
                    } else {
                        // Обычный режим поля и анимации обмена/исчезновения
                        for (r in 0 until MatchThreeGameLogic.ROWS) {
                            for (c in 0 until MatchThreeGameLogic.COLS) {
                                val ball = board[r][c]
                                val baseX = colOffsets[c] - gridLeftPx
                                val baseY = rowOffsets[r] - gridTopPx

                                // Расчёт смещения при плавном обмене шариков
                                val currentSwap = swapAnim
                                var animOffsetX = 0f
                                var animOffsetY = 0f
                                if (currentSwap != null) {
                                    if (r == currentSwap.r1 && c == currentSwap.c1) {
                                        animOffsetX = (colOffsets[currentSwap.c2] - colOffsets[currentSwap.c1]) * currentSwap.progress
                                        animOffsetY = (rowOffsets[currentSwap.r2] - rowOffsets[currentSwap.r1]) * currentSwap.progress
                                    } else if (r == currentSwap.r2 && c == currentSwap.c2) {
                                        animOffsetX = (colOffsets[currentSwap.c1] - colOffsets[currentSwap.c2]) * currentSwap.progress
                                        animOffsetY = (rowOffsets[currentSwap.r1] - rowOffsets[currentSwap.r2]) * currentSwap.progress
                                    }
                                }

                                val isSelected = selectedCell == (r to c)
                                val isClearing = (r to c) in clearingCells

                                Box(
                                    modifier = Modifier
                                        .offset(x = cPx(baseX + animOffsetX), y = cPx(baseY + animOffsetY))
                                        .size(cPx(cellSizePx), cPx(cellSizePx))
                                        .clip(RoundedCornerShape(cPx(10f)))
                                        .then(
                                            if (isSelected) {
                                                Modifier.border(
                                                    width = cPx(4f),
                                                    color = PixelGoldBright,
                                                    shape = RoundedCornerShape(cPx(10f))
                                                )
                                            } else Modifier
                                        )
                                        .pointerInput(activeBooster, isResolving) {
                                            detectDragGestures(
                                                onDragEnd = { /* no-op */ },
                                                onDrag = { change, dragAmount ->
                                                    change.consume()
                                                    if (!isResolving && activeBooster == null && swapAnim == null) {
                                                        val threshold = 18f
                                                        if (abs(dragAmount.x) > abs(dragAmount.y)) {
                                                            if (dragAmount.x > threshold && c + 1 < MatchThreeGameLogic.COLS) {
                                                                trySwap(r, c, r, c + 1)
                                                            } else if (dragAmount.x < -threshold && c - 1 >= 0) {
                                                                trySwap(r, c, r, c - 1)
                                                            }
                                                        } else {
                                                            if (dragAmount.y > threshold && r + 1 < MatchThreeGameLogic.ROWS) {
                                                                trySwap(r, c, r + 1, c)
                                                            } else if (dragAmount.y < -threshold && r - 1 >= 0) {
                                                                trySwap(r, c, r - 1, c)
                                                            }
                                                        }
                                                    }
                                                }
                                            )
                                        }
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            if (isResolving || swapAnim != null || fallingBalls != null) return@clickable

                                            val currentB = activeBooster
                                            if (currentB != null) {
                                                handleBoosterClick(currentB, r, c)
                                            } else {
                                                val sel = selectedCell
                                                if (sel == null) {
                                                    selectedCell = r to c
                                                } else if (sel == (r to c)) {
                                                    selectedCell = null
                                                } else {
                                                    val (sr, sc) = sel
                                                    if (abs(sr - r) + abs(sc - c) == 1) {
                                                        trySwap(sr, sc, r, c)
                                                    } else {
                                                        selectedCell = r to c
                                                    }
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (ball != null) {
                                        Image(
                                            painter = painterResource(id = ball.drawableRes),
                                            contentDescription = ball.name,
                                            modifier = Modifier
                                                .size(cPx(122f))
                                                .graphicsLayer {
                                                    if (isClearing) {
                                                        scaleX = clearProgress.value
                                                        scaleY = clearProgress.value
                                                        alpha = clearProgress.value
                                                    }
                                                }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 6. плашки для комбо, статусов и бустеров
                val activeCombo = comboBonusInfo
                val activeStatus = statusBannerText
                val activeB = activeBooster

                val showPopup = activeCombo != null || activeStatus != null || activeB != null

                AnimatedVisibility(
                    visible = showPopup,
                    enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                    exit = scaleOut() + fadeOut(),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(horizontal = cPx(35f))
                ) {
                    PixelPanel(
                        cornerSizeDp = cPx(20f),
                        sliceHeightDp = cPx(20f),
                        contentPadding = PaddingValues(horizontal = cPx(24f), vertical = cPx(14f))
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Top
                        ) {
                            if (activeCombo != null) {
                                val (comboNum, bonusPts) = activeCombo
                                Text(
                                    text = "КОМБО x$comboNum!",
                                    fontFamily = AppFontFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = (22f * scale).coerceAtLeast(20f).sp,
                                    color = PixelGoldDark,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(cPx(4f)))
                                Text(
                                    text = "+$bonusPts очков",
                                    fontFamily = AppFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = (16f * scale).coerceAtLeast(18f).sp,
                                    color = PixelTextDark,
                                    textAlign = TextAlign.Center
                                )
                            } else if (activeStatus != null) {
                                Text(
                                    text = activeStatus,
                                    fontFamily = AppFontFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = (17f * scale).coerceAtLeast(20f).sp,
                                    color = PixelTextDark,
                                    textAlign = TextAlign.Center
                                )
                            } else if (activeB != null) {
                                val title = when (activeB) {
                                    BoosterType.SINGLE -> "УДАЛЕНИЕ ШАРИКА"
                                    BoosterType.COLUMN -> "ОЧИСТКА СТОЛБЦА"
                                    BoosterType.ROW -> "ОЧИСТКА СТРОКИ"
                                    BoosterType.AREA -> "ВЗРЫВ ЗОНЫ 3x3"
                                }
                                val hint = when (activeB) {
                                    BoosterType.SINGLE -> "Нажми на любой шарик на поле"
                                    BoosterType.COLUMN -> "Нажми на нужный столбец"
                                    BoosterType.ROW -> "Нажми на нужную строку"
                                    BoosterType.AREA -> "Нажми в центр взрыва"
                                }
                                Text(
                                    text = title,
                                    fontFamily = AppFontFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = (18f * scale).coerceAtLeast(20f).sp,
                                    color = PixelGoldDark,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(cPx(3f)))
                                Text(
                                    text = hint,
                                    fontFamily = AppFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = (15f * scale).coerceAtLeast(18f).sp,
                                    color = PixelTextDark,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(cPx(2f)))
                                Text(
                                    text = "(нажми на бустер внизу для отмены)",
                                    fontFamily = AppFontFamily,
                                    fontSize = (12f * scale).coerceAtLeast(16f).sp,
                                    color = Color(0xFF6B5848),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // 7. Кнопки бустеров внизу (4 рамки на фоне)
                val boosters = listOf(
                    Triple(BoosterType.SINGLE, 160f to 1902f, singleCount),
                    Triple(BoosterType.COLUMN, 368f to 1902f, columnCount),
                    Triple(BoosterType.ROW, 577f to 1902f, rowCount),
                    Triple(BoosterType.AREA, 786f to 1902f, areaCount)
                )

                boosters.forEach { (type, pos, count) ->
                    val (bx, by) = pos
                    val isActive = activeBooster == type

                    // Интерактивная область кнопки
                    Box(
                        modifier = Modifier
                            .offset(x = cPx(bx), y = cPx(by))
                            .size(cPx(141f), cPx(162f))
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                if (isResolving) return@clickable
                                if (count > 0) {
                                    activeBooster = if (activeBooster == type) null else type
                                }
                            }
                    )
                }

                // 8. Бейджи с количеством бустеров: сдвинуты правее и ниже (+4 px), крупный адаптивный шрифт
                val badgePositions = listOf(
                    272f to 1874f to singleCount,
                    481f to 1874f to columnCount,
                    690f to 1874f to rowCount,
                    899f to 1874f to areaCount
                )

                val badgeFontSize = (24f * scale).coerceAtLeast(14f).sp

                badgePositions.forEach { (pos, count) ->
                    val (bx, by) = pos
                    Box(
                        modifier = Modifier
                            .offset(x = cPx(bx), y = cPx(by))
                            .size(cPx(50f), cPx(50f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$count",
                            fontFamily = AppFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = badgeFontSize,
                            color = Color(0xFF2E2213),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
