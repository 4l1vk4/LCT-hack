package ru.finpet.kids.feature.map.sheets

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import ru.finpet.kids.core.designsystem.PixelButton
import ru.finpet.kids.core.designsystem.LampochkaIcon
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.aspectRatio
import kotlin.math.roundToInt
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import ru.finpet.kids.core.designsystem.PixelSlider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import ru.finpet.kids.R
import ru.finpet.kids.core.data.local.entity.QuestProgressEntity
import ru.finpet.kids.core.data.repository.QuestItem
import ru.finpet.kids.core.designsystem.AppFontFamily
import ru.finpet.kids.core.designsystem.BooksIcon
import ru.finpet.kids.core.designsystem.CoinIcon
import ru.finpet.kids.core.designsystem.FinCard
import ru.finpet.kids.core.designsystem.GalochkaIcon
import ru.finpet.kids.core.designsystem.FreshGreen
import ru.finpet.kids.core.designsystem.JoyOrange
import ru.finpet.kids.core.designsystem.KubikIcon
import ru.finpet.kids.core.designsystem.LocalUiScale
import ru.finpet.kids.core.designsystem.LockIcon
import ru.finpet.kids.core.designsystem.PixelGoldBright
import ru.finpet.kids.core.designsystem.PixelGoldDark
import ru.finpet.kids.core.designsystem.PixelGreenCrop
import ru.finpet.kids.core.designsystem.PixelParchmentBorder
import ru.finpet.kids.core.designsystem.PixelParchmentLight
import ru.finpet.kids.core.designsystem.PixelTextDark
import ru.finpet.kids.core.designsystem.PixelTextMuted
import ru.finpet.kids.core.designsystem.PixelWoodMedium
import ru.finpet.kids.core.designsystem.TextPrimary
import ru.finpet.kids.core.designsystem.TextSecondary
import ru.finpet.kids.core.designsystem.pixelButtonBackground
import ru.finpet.kids.feature.map.GlossaryDialog
import ru.finpet.kids.feature.map.components.PixelDarkBrown
import ru.finpet.kids.feature.map.components.PixelLocationModalSheet

/**
 * Шторка школы финансовой грамотности:
 * - Прогрессия по стадиям взросления питомца (Малыш -> Подросток -> Взрослый кот).
 * - В ДЕМО-режиме (по ТЗ) все уроки всех уровней открыты сразу.
 * - Интерактивные типы квестов:
 *   1) CHOICE (классический выбор из нескольких вариантов);
 *   2) TEXT_INPUT (ввод финансового термина с подсказкой и проверкой синонимов);
 *   3) MONEY_ALLOCATION (распределение суммы монет интерактивным слайдером);
 *   4) CALCULATION (интерактивный расчет сдачи и скидки со случайной генерацией).
 * - Рандом: кнопка случайного урока, смена параметров ситуаций, вариации чисел.
 * - Интерактивный словарик финансовых терминов питомца.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SchoolBottomSheet(
    quests: List<QuestItem>,
    progressList: List<QuestProgressEntity>,
    petName: String,
    growthStage: String = "BABY",
    demoMode: Boolean = false,
    currentPeriodIndex: Int = 1,
    targetQuestId: String? = null,
    skinId: String = "cat_black",
    petMood: String = "HAPPY",
    onDismiss: () -> Unit,
    onSelectOption: (questId: String, optionId: String) -> Unit,
    onAnswerText: (questId: String, answer: String) -> Unit = { _, _ -> },
    onAnswerAllocation: (questId: String, amount: Int) -> Unit = { _, _ -> }
) {
    val schoolQuestIds = remember(quests) { quests.map { it.id }.toSet() }

    val completedCount = quests.count { q ->
        progressList.any { p -> p.questId == q.id && p.isCompleted }
    }
    // Количество школьных задач, пройденных сегодня (включая задачи с ошибками)
    val todaySolvedCount = progressList.count { p ->
        p.completedInPeriod == currentPeriodIndex && p.questId in schoolQuestIds
    }
    val displayTodaySolved = todaySolvedCount.coerceIn(0, 2)
    val displayCompleted = completedCount.coerceIn(0, quests.size)
    // Ограничение: максимум 2 задачи в день, но в демо-режиме лимита нет
    val isDailyLimitReached = !demoMode && todaySolvedCount >= 2
    var showGlossary by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()

    // Определение разблокировки стадий
    fun isStageUnlocked(stage: String): Boolean {
        if (demoMode) return true
        val currentStageIndex = when (growthStage) {
            "ADULT" -> 2
            "TEEN" -> 1
            else -> 0
        }
        val targetStageIndex = when (stage) {
            "ADULT" -> 2
            "TEEN" -> 1
            else -> 0
        }
        return targetStageIndex <= currentStageIndex
    }

    // Выбранный фильтр по стадии (по умолчанию текущая стадия питомца или "ALL" в демо)
    var selectedStageFilter by remember(growthStage, demoMode) {
        mutableStateOf(if (demoMode) "ALL" else growthStage)
    }

    // Подготовка целевого квеста при переходе
    LaunchedEffect(targetQuestId) {
        if (targetQuestId != null) {
            val targetQuest = quests.find { it.id == targetQuestId }
            if (targetQuest != null) {
                selectedStageFilter = if (demoMode) "ALL" else targetQuest.stage
            }
            delay(200)
            try {
                bringIntoViewRequester.bringIntoView()
            } catch (_: Exception) {
                listState.animateScrollToItem(1)
            }
        }
    }

    // Фильтрация квестов по выбранной вкладке
    val filteredQuests = remember(quests, selectedStageFilter) {
        if (selectedStageFilter == "ALL") {
            quests
        } else {
            quests.filter { it.stage == selectedStageFilter }
        }
    }

    // Состояния для текстовых квестов и подсказок
    val textInputs = remember { mutableStateMapOf<String, String>() }
    val hintsRevealed = remember { mutableStateMapOf<String, Boolean>() }

    // Состояния для интерактивных ползунков распределения денег
    val allocations = remember { mutableStateMapOf<String, Int>() }

    // Состояния динамических вариантов ситуации (для рандома)
    val variantIndices = remember { mutableStateMapOf<String, Int>() }

    // Состояния для математических задач со случайными числами
    val calculationOffsets = remember { mutableStateMapOf<String, Int>() }

    PixelLocationModalSheet(
        title = "Школа финансовой грамотности",
        subtitle = "Интерактивные квесты • Растем и учимся финансовой грамотности",
        headerDrawableRes = R.drawable.school_background_empty,
        badgeContent = {
            val uiScale = LocalUiScale.current
            Text(
                text = if (demoMode) "$displayCompleted / ${quests.size}" else "$displayTodaySolved/2 • $displayCompleted/${quests.size}",
                fontSize = (13f * uiScale).coerceIn(9f, 15f).sp,
                fontWeight = FontWeight.Bold,
                color = PixelDarkBrown,
                fontFamily = AppFontFamily,
                maxLines = 1
            )
        },
        onDismiss = onDismiss,
        listState = listState
    ) {
        val uiScale = LocalUiScale.current
        Column(verticalArrangement = Arrangement.spacedBy((12.dp * uiScale).coerceIn(8.dp, 14.dp))) {

            // Карточка словарика в пиксельном стиле
            FinCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { showGlossary = true }
            ) {
                Row(
                    modifier = Modifier.padding(
                        horizontal = (12.dp * uiScale).coerceIn(8.dp, 14.dp),
                        vertical = (10.dp * uiScale).coerceIn(6.dp, 12.dp)
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size((36.dp * uiScale).coerceIn(26.dp, 38.dp))
                            .clip(RoundedCornerShape(6.dp))
                            .pixelButtonBackground(containerColor = PixelGoldBright.copy(alpha = 0.35f)),
                        contentAlignment = Alignment.Center
                    ) {
                        BooksIcon(modifier = Modifier.size((22.dp * uiScale).coerceIn(16.dp, 24.dp)))
                    }
                    Spacer(modifier = Modifier.width((10.dp * uiScale).coerceIn(6.dp, 12.dp)))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ФИНАНСОВЫЙ СЛОВАРИК",
                            fontFamily = AppFontFamily,
                            fontSize = (16f * uiScale).coerceIn(11f, 18f).sp,
                            fontWeight = FontWeight.Bold,
                            color = PixelTextDark
                        )
                        Text(
                            text = "Что значит «бюджет», «накопления», «сдача» и другие слова",
                            fontFamily = AppFontFamily,
                            fontSize = (13f * uiScale).coerceIn(9f, 15f).sp,
                            color = TextSecondary,
                            lineHeight = (17f * uiScale).coerceIn(12f, 20f).sp
                        )
                    }
                }
            }

            // Оповещение об исчерпании дневного лимита (2 задачи в день)
            if (isDailyLimitReached) {
                FinCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color(0xFFD4ECD5)
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = (12.dp * uiScale).coerceIn(8.dp, 14.dp),
                            vertical = (10.dp * uiScale).coerceIn(6.dp, 12.dp)
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size((36.dp * uiScale).coerceIn(26.dp, 38.dp))
                                .clip(RoundedCornerShape(6.dp))
                                .pixelButtonBackground(containerColor = FreshGreen.copy(alpha = 0.35f)),
                            contentAlignment = Alignment.Center
                        ) {
                            BooksIcon(modifier = Modifier.size((22.dp * uiScale).coerceIn(16.dp, 24.dp)))
                        }
                        Spacer(modifier = Modifier.width((10.dp * uiScale).coerceIn(6.dp, 12.dp)))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ДНЕВНАЯ НОРМА ВЫПОЛНЕНА!",
                                fontFamily = AppFontFamily,
                                fontSize = (16f * uiScale).coerceIn(11f, 18f).sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B5E20)
                            )
                            Text(
                                text = "Ты прошёл 2 урока за сегодня ($displayTodaySolved/2). Новые уроки откроются в следующий день!",
                                fontFamily = AppFontFamily,
                                fontSize = (13f * uiScale).coerceIn(9f, 15f).sp,
                                color = TextSecondary,
                                lineHeight = (17f * uiScale).coerceIn(12f, 20f).sp
                            )
                        }
                    }
                }
            }

            // Переключатель стадий взросления питомца
            Column(verticalArrangement = Arrangement.spacedBy((6.dp * uiScale).coerceIn(4.dp, 8.dp))) {
                Text(
                    text = "Стадии обучения и взросления:",
                    fontFamily = AppFontFamily,
                    fontSize = (17f * uiScale).coerceIn(13f, 20f).sp,
                    fontWeight = FontWeight.Bold,
                    color = PixelTextDark
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy((4.dp * uiScale).coerceIn(2.dp, 6.dp))
                ) {
                    StageTabChip(
                        title = "Малыш",
                        badge = "1",
                        isSelected = selectedStageFilter == "BABY",
                        isUnlocked = isStageUnlocked("BABY"),
                        countDone = quests.count { it.stage == "BABY" && progressList.any { p -> p.questId == it.id && p.isCompleted } },
                        totalCount = quests.count { it.stage == "BABY" },
                        modifier = Modifier.weight(1f),
                        onClick = { selectedStageFilter = "BABY" }
                    )

                    StageTabChip(
                        title = "Подросток",
                        badge = "2",
                        isSelected = selectedStageFilter == "TEEN",
                        isUnlocked = isStageUnlocked("TEEN"),
                        countDone = quests.count { it.stage == "TEEN" && progressList.any { p -> p.questId == it.id && p.isCompleted } },
                        totalCount = quests.count { it.stage == "TEEN" },
                        modifier = Modifier.weight(1f),
                        onClick = { selectedStageFilter = "TEEN" }
                    )

                    StageTabChip(
                        title = "Взрослый",
                        badge = "3",
                        isSelected = selectedStageFilter == "ADULT",
                        isUnlocked = isStageUnlocked("ADULT"),
                        countDone = quests.count { it.stage == "ADULT" && progressList.any { p -> p.questId == it.id && p.isCompleted } },
                        totalCount = quests.count { it.stage == "ADULT" },
                        modifier = Modifier.weight(1f),
                        onClick = { selectedStageFilter = "ADULT" }
                    )

                    if (demoMode) {
                        StageTabChip(
                            title = "Все",
                            badge = "*",
                            isSelected = selectedStageFilter == "ALL",
                            isUnlocked = true,
                            countDone = completedCount,
                            totalCount = quests.size,
                            modifier = Modifier.weight(0.9f),
                            onClick = { selectedStageFilter = "ALL" }
                        )
                    }
                }
            }

            // Если выбранная стадия закрыта (не демо-режим)
            if (selectedStageFilter != "ALL" && !isStageUnlocked(selectedStageFilter)) {
                val stageTitle = when (selectedStageFilter) {
                    "TEEN" -> "Подросток"
                    "ADULT" -> "Взрослый кот"
                    else -> "Малыш"
                }
                val currentStageTitle = when (growthStage) {
                    "TEEN" -> "Подросток"
                    "ADULT" -> "Взрослый кот"
                    else -> "Малыш"
                }

                FinCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding((18.dp * uiScale).coerceIn(12.dp, 20.dp)),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFD7CCC8))
                                .padding(
                                    horizontal = (14.dp * uiScale).coerceIn(8.dp, 16.dp),
                                    vertical = (6.dp * uiScale).coerceIn(4.dp, 8.dp)
                                )
                        ) {
                            Text(
                                text = "Уровень заблокирован",
                                fontFamily = AppFontFamily,
                                fontSize = (14f * uiScale).coerceIn(10f, 16f).sp,
                                fontWeight = FontWeight.Bold,
                                color = PixelWoodMedium
                            )
                        }
                        Spacer(modifier = Modifier.height((8.dp * uiScale).coerceIn(4.dp, 10.dp)))
                        Text(
                            text = "Уроки уровня «$stageTitle» пока закрыты",
                            fontFamily = AppFontFamily,
                            fontSize = (17f * uiScale).coerceIn(13f, 20f).sp,
                            fontWeight = FontWeight.Bold,
                            color = PixelTextDark,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height((6.dp * uiScale).coerceIn(4.dp, 8.dp)))
                        Text(
                            text = "Твой питомец $petName сейчас на стадии «$currentStageTitle».\nЗаботься о котике, планируй бюджет и переходи к следующим игровым дням, чтобы он вырос и открыл этот уровень!\n\n(В Настройках можно включить Демо-режим для доступа ко всем квестам сразу)",
                            fontFamily = AppFontFamily,
                            fontSize = (15f * uiScale).coerceIn(11f, 17f).sp,
                            color = PixelTextMuted,
                            textAlign = TextAlign.Center,
                            lineHeight = (19f * uiScale).coerceIn(14f, 22f).sp
                        )
                    }
                }
            } else {
                // Список квестов для открытой стадии
                filteredQuests.forEach { quest ->
                    val isDone = progressList.any { it.questId == quest.id && it.isCompleted }
                    val isFailedToday = progressList.any {
                        it.questId == quest.id && !it.isCompleted && it.completedInPeriod == currentPeriodIndex
                    }
                    val isTarget = targetQuestId != null && quest.id == targetQuestId

                    // Случайный начальный вариант ситуации для разнообразия
                    val randomVariantSeed = remember(quest.id) {
                        if (quest.dynamicVariants.isNotEmpty()) (0 until quest.dynamicVariants.size).random() else 0
                    }
                    val currentVariantIdx = variantIndices[quest.id] ?: randomVariantSeed
                    val displaySituation = if (quest.dynamicVariants.isNotEmpty()) {
                        quest.dynamicVariants.getOrElse(currentVariantIdx % quest.dynamicVariants.size) { quest.situation }
                    } else {
                        quest.situation
                    }

                    FinCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(if (isTarget) Modifier.bringIntoViewRequester(bringIntoViewRequester) else Modifier)
                    ) {
                        Column(modifier = Modifier.padding((14.dp * uiScale).coerceIn(8.dp, 16.dp))) {

                            // Теги темы и стадии задания
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy((5.dp * uiScale).coerceIn(3.dp, 6.dp)),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TopicChip(topic = quest.topic, topicTitle = quest.topicTitle)
                                StageBadge(stage = quest.stage)
                            }

                            Spacer(modifier = Modifier.height((5.dp * uiScale).coerceIn(3.dp, 7.dp)))

                            // Статус квеста / Награда
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy((4.dp * uiScale).coerceIn(2.dp, 6.dp))
                            ) {
                                if (isDone) {
                                    Box(
                                        modifier = Modifier
                                            .pixelButtonBackground(containerColor = Color(0xFFE8F5E9))
                                            .padding(
                                                horizontal = (8.dp * uiScale).coerceIn(5.dp, 10.dp),
                                                vertical = (3.5.dp * uiScale).coerceIn(2.dp, 5.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Выполнено!",
                                            fontFamily = AppFontFamily,
                                            fontSize = (13f * uiScale).coerceIn(10f, 15f).sp,
                                            color = PixelGreenCrop,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                } else if (isFailedToday) {
                                    Box(
                                        modifier = Modifier
                                            .pixelButtonBackground(containerColor = Color(0xFFFFF3E0))
                                            .padding(
                                                horizontal = (8.dp * uiScale).coerceIn(5.dp, 10.dp),
                                                vertical = (3.5.dp * uiScale).coerceIn(2.dp, 5.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy((4.dp * uiScale).coerceIn(2.dp, 5.dp))
                                        ) {
                                            Image(
                                                painter = painterResource(id = R.drawable.sad_mood_3vryad),
                                                contentDescription = null,
                                                modifier = Modifier.size((14.dp * uiScale).coerceIn(11.dp, 17.dp))
                                            )
                                            Text(
                                                text = "Неверный ответ",
                                                fontFamily = AppFontFamily,
                                                fontSize = (13.5f * uiScale).coerceIn(10f, 16f).sp,
                                                color = JoyOrange,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .pixelButtonBackground(containerColor = Color(0xFFFFF8E1))
                                            .padding(
                                                horizontal = (8.dp * uiScale).coerceIn(5.dp, 10.dp),
                                                vertical = (3.5.dp * uiScale).coerceIn(2.dp, 5.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy((4.dp * uiScale).coerceIn(2.dp, 5.dp))
                                        ) {
                                            CoinIcon(modifier = Modifier.size((14.dp * uiScale).coerceIn(11.dp, 17.dp)))
                                            Text(
                                                text = "+${quest.rewardCoins} монет",
                                                fontFamily = AppFontFamily,
                                                fontSize = (13.5f * uiScale).coerceIn(10f, 16f).sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PixelGoldDark
                                            )
                                        }
                                    }
                                }
                            }

                            // Отступ от надписи +8 монет и статуса
                            Spacer(modifier = Modifier.height((10.dp * uiScale).coerceIn(7.dp, 14.dp)))

                            // Название задания
                            Text(
                                text = quest.title,
                                fontFamily = AppFontFamily,
                                fontSize = (16f * uiScale).coerceIn(12f, 18f).sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            Spacer(modifier = Modifier.height((4.dp * uiScale).coerceIn(2.dp, 6.dp)))

                            // Описание ситуации
                            Text(
                                text = displaySituation,
                                fontFamily = AppFontFamily,
                                fontSize = (14f * uiScale).coerceIn(10f, 16f).sp,
                                color = TextPrimary,
                                lineHeight = (18f * uiScale).coerceIn(13f, 21f).sp
                            )

                            // Кнопка вариативности рандома (если есть альтернативные тексты ситуации)
                            if (quest.dynamicVariants.size > 1 && !isDone && !isFailedToday && !isDailyLimitReached) {
                                Spacer(modifier = Modifier.height((8.dp * uiScale).coerceIn(5.dp, 10.dp)))
                                PixelButton(
                                    text = "Другая ситуация",
                                    onClick = {
                                        variantIndices[quest.id] = currentVariantIdx + 1
                                    },
                                    leadingIcon = { KubikIcon(modifier = Modifier.size((16.dp * uiScale).coerceIn(12.dp, 18.dp))) },
                                    containerColor = Color(0xFF6D4C41),
                                    textColor = PixelParchmentLight,
                                    fontSize = (14f * uiScale).coerceIn(11f, 16f).sp,
                                    contentPadding = PaddingValues(
                                        horizontal = (14.dp * uiScale).coerceIn(10.dp, 16.dp),
                                        vertical = (7.dp * uiScale).coerceIn(4.dp, 9.dp)
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height((12.dp * uiScale).coerceIn(8.dp, 14.dp)))

                            if (isFailedToday) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFFFF3E0))
                                        .border(1.dp, Color(0xFFFFB74D), RoundedCornerShape(8.dp))
                                        .padding((12.dp * uiScale).coerceIn(8.dp, 14.dp))
                                ) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "Учебная пауза",
                                                fontFamily = AppFontFamily,
                                                fontSize = (15f * uiScale).coerceIn(11f, 17f).sp,
                                                color = Color(0xFFE65100),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.height((4.dp * uiScale).coerceIn(2.dp, 6.dp)))
                                        Text(
                                            text = "Возвращайся к этому заданию завтра со свежими силами, чтобы решить правильно и забрать награду.",
                                            fontFamily = AppFontFamily,
                                            fontSize = (13f * uiScale).coerceIn(10f, 15f).sp,
                                            color = PixelTextDark,
                                            lineHeight = (18f * uiScale).coerceIn(13f, 21f).sp
                                        )
                                    }
                                }
                            } else if (!isDone && isDailyLimitReached) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFFFF3E0))
                                        .border(1.dp, Color(0xFFFFB74D), RoundedCornerShape(8.dp))
                                        .padding((12.dp * uiScale).coerceIn(8.dp, 14.dp))
                                ) {
                                    Text(
                                        text = "На сегодня лимит задач исчерпан ($displayTodaySolved/2). Переходи к следующему дню, чтобы решать новые задания!",
                                        fontFamily = AppFontFamily,
                                        fontSize = (14f * uiScale).coerceIn(10f, 16f).sp,
                                        color = Color(0xFFE65100),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            } else {
                                when (quest.type) {
                                "TEXT_INPUT" -> {
                                    TextInputQuestSection(
                                        quest = quest,
                                        isDone = isDone,
                                        currentInput = textInputs[quest.id] ?: "",
                                        isHintRevealed = hintsRevealed[quest.id] ?: false,
                                        onInputChange = { textInputs[quest.id] = it },
                                        onToggleHint = {
                                            hintsRevealed[quest.id] = !(hintsRevealed[quest.id] ?: false)
                                        },
                                        onSubmit = {
                                            val answer = textInputs[quest.id] ?: ""
                                            if (answer.isNotBlank()) {
                                                onAnswerText(quest.id, answer)
                                            }
                                        }
                                    )
                                }

                                "MONEY_ALLOCATION" -> {
                                    MoneyAllocationQuestSection(
                                        quest = quest,
                                        isDone = isDone,
                                        currentAllocation = allocations[quest.id] ?: (quest.totalBudget / 2),
                                        onAllocationChange = { allocations[quest.id] = it },
                                        onSubmit = {
                                            val amt = allocations[quest.id] ?: (quest.totalBudget / 2)
                                            onAnswerAllocation(quest.id, amt)
                                        }
                                    )
                                }

                                "CALCULATION" -> {
                                    CalculationQuestSection(
                                        quest = quest,
                                        isDone = isDone,
                                        randomOffset = calculationOffsets[quest.id] ?: 0,
                                        onRerollNumbers = {
                                            calculationOffsets[quest.id] = (calculationOffsets[quest.id] ?: 0) + 1
                                        },
                                        onSelectOption = { optId -> onSelectOption(quest.id, optId) }
                                    )
                                }

                                else -> {
                                    // CHOICE (классический выбор варианта со случайным перемешиванием)
                                    ChoiceQuestSection(
                                        quest = quest,
                                        isDone = isDone,
                                        onSelectOption = { optId -> onSelectOption(quest.id, optId) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    }

    // Модальное окно со словариком терминов
    if (showGlossary) {
        GlossaryDialog(petName = petName, onDismiss = { showGlossary = false })
    }
}

/**
 * Вкладка выбора стадии взросления питомца
 */
@Composable
private fun StageTabChip(
    title: String,
    badge: String,
    isSelected: Boolean,
    isUnlocked: Boolean,
    countDone: Int,
    totalCount: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val uiScale = LocalUiScale.current
    val bg = if (isSelected) PixelGoldBright else if (isUnlocked) Color(0xFFF3E5AB) else Color(0xFFDED0B6)
    val textColor = if (isSelected) PixelTextDark else if (isUnlocked) PixelWoodMedium else Color(0xFF8D7B68)

    Box(
        modifier = modifier
            .pixelButtonBackground(containerColor = bg)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(
                vertical = (7.dp * uiScale).coerceIn(4.dp, 8.dp),
                horizontal = (4.dp * uiScale).coerceIn(2.dp, 6.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!isUnlocked) {
                    LockIcon(modifier = Modifier.size((13.dp * uiScale).coerceIn(9.dp, 15.dp)))
                    Spacer(modifier = Modifier.width((2.dp * uiScale).coerceIn(1.dp, 3.dp)))
                }
                Text(
                    text = title,
                    fontFamily = AppFontFamily,
                    fontSize = (14f * uiScale).coerceIn(10f, 16f).sp,
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                    color = textColor,
                    maxLines = 1
                )
            }
            if (totalCount > 0) {
                Text(
                    text = "$countDone/$totalCount",
                    fontFamily = AppFontFamily,
                    fontSize = (12f * uiScale).coerceIn(8f, 13f).sp,
                    color = if (isSelected) PixelGreenCrop else PixelTextMuted,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Значок темы задания ("Личный бюджет" и др.)
 */
@Composable
private fun TopicChip(topic: String, topicTitle: String) {
    val uiScale = LocalUiScale.current
    val bg = when (topic) {
        "SAVINGS" -> Color(0xFFFFD54F)
        "PURCHASES" -> Color(0xFFC8E6C9)
        else -> Color(0xFFBBDEFB)
    }

    Box(
        modifier = Modifier
            .pixelButtonBackground(containerColor = bg)
            .padding(
                horizontal = (8.dp * uiScale).coerceIn(5.dp, 10.dp),
                vertical = (4.dp * uiScale).coerceIn(2.dp, 6.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = topicTitle,
            fontFamily = AppFontFamily,
            fontSize = (13.5f * uiScale).coerceIn(10f, 15f).sp,
            fontWeight = FontWeight.Bold,
            color = PixelTextDark,
            maxLines = 1
        )
    }
}

/**
 * Значок стадии задания ("1 стадия", "2 стадия", "3 стадия")
 */
@Composable
private fun StageBadge(stage: String) {
    val uiScale = LocalUiScale.current
    val label = when (stage) {
        "ADULT" -> "3 стадия"
        "TEEN" -> "2 стадия"
        else -> "1 стадия"
    }
    val bg = when (stage) {
        "ADULT" -> Color(0xFFFFCC80)
        "TEEN" -> Color(0xFFFFF9C4)
        else -> Color(0xFFEFEBE9)
    }

    Box(
        modifier = Modifier
            .pixelButtonBackground(containerColor = bg)
            .padding(
                horizontal = (8.dp * uiScale).coerceIn(5.dp, 10.dp),
                vertical = (4.dp * uiScale).coerceIn(2.dp, 6.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = AppFontFamily,
            fontSize = (12.5f * uiScale).coerceIn(9f, 14f).sp,
            fontWeight = FontWeight.Bold,
            color = PixelTextDark,
            maxLines = 1
        )
    }
}

/**
 * Секция квеста с вводом текста (финансовый термин)
 */
@Composable
private fun TextInputQuestSection(
    quest: QuestItem,
    isDone: Boolean,
    currentInput: String,
    isHintRevealed: Boolean,
    onInputChange: (String) -> Unit,
    onToggleHint: () -> Unit,
    onSubmit: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val uiScale = LocalUiScale.current

    if (isDone) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFE8F5E9))
                .padding((12.dp * uiScale).coerceIn(8.dp, 14.dp))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.happy_mood_3vryad),
                    contentDescription = null,
                    modifier = Modifier.size((28.dp * uiScale).coerceIn(22.dp, 34.dp))
                )
                Spacer(modifier = Modifier.width((8.dp * uiScale).coerceIn(5.dp, 10.dp)))
                Text(
                    text = "Термин разгадан! Ответ принят.",
                    fontFamily = AppFontFamily,
                    fontSize = (15f * uiScale).coerceIn(11f, 17f).sp,
                    color = PixelGreenCrop,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy((8.dp * uiScale).coerceIn(5.dp, 10.dp))) {
            // Подсказка, если ребенок хочет проверить себя (в пиксельном стиле кнопок)
            if (quest.hint.isNotBlank()) {
                PixelButton(
                    text = if (isHintRevealed) "Скрыть подсказку" else "Нужна подсказка",
                    leadingIcon = { LampochkaIcon(modifier = Modifier.size((16.dp * uiScale).coerceIn(12.dp, 18.dp))) },
                    onClick = onToggleHint,
                    containerColor = if (isHintRevealed) PixelGoldBright else Color(0xFFFFD54F),
                    textColor = PixelTextDark,
                    fontSize = (14f * uiScale).coerceIn(11f, 16f).sp,
                    contentPadding = PaddingValues(
                        horizontal = (14.dp * uiScale).coerceIn(9.dp, 16.dp),
                        vertical = (7.dp * uiScale).coerceIn(4.dp, 8.dp)
                    )
                )

                if (isHintRevealed) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFFF9C4))
                            .border(1.dp, Color(0xFFFFD54F), RoundedCornerShape(8.dp))
                            .padding((10.dp * uiScale).coerceIn(6.dp, 12.dp))
                    ) {
                        Text(
                            text = quest.hint,
                            fontFamily = AppFontFamily,
                            fontSize = (14f * uiScale).coerceIn(11f, 16f).sp,
                            color = PixelTextDark,
                            lineHeight = (18f * uiScale).coerceIn(13f, 21f).sp
                        )
                    }
                }
            }

            // Поле ввода текста ответа
            OutlinedTextField(
                value = currentInput,
                onValueChange = onInputChange,
                placeholder = {
                    Text(
                        text = quest.inputPlaceholder.ifEmpty { "Введи ответ сюда..." },
                        fontFamily = AppFontFamily,
                        fontSize = (15f * uiScale).coerceIn(11f, 16f).sp,
                        color = Color(0xFF9E9E9E)
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    onSubmit()
                }),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PixelWoodMedium,
                    unfocusedBorderColor = PixelParchmentBorder,
                    cursorColor = PixelWoodMedium,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Кнопка подтверждения ответа
            PixelButton(
                text = "Проверить ответ",
                onClick = {
                    focusManager.clearFocus()
                    onSubmit()
                },
                enabled = currentInput.isNotBlank(),
                containerColor = PixelGoldBright,
                textColor = PixelTextDark,
                modifier = Modifier.fillMaxWidth(),
                fontSize = (16f * uiScale).coerceIn(12f, 18f).sp,
                contentPadding = PaddingValues(vertical = (11.dp * uiScale).coerceIn(7.dp, 13.dp))
            )
        }
    }
}

/**
 * Секция квеста с распределением денег (интерактивный слайдер / ползунок)
 */
@Composable
private fun MoneyAllocationQuestSection(
    quest: QuestItem,
    isDone: Boolean,
    currentAllocation: Int,
    onAllocationChange: (Int) -> Unit,
    onSubmit: () -> Unit
) {
    val uiScale = LocalUiScale.current
    val total = quest.totalBudget
    val allocation = currentAllocation.coerceIn(0, total)
    val remaining = total - allocation
    val percent = ((allocation.toFloat() / total.toFloat()) * 100).toInt()

    if (isDone) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFE8F5E9))
                .padding((12.dp * uiScale).coerceIn(8.dp, 14.dp))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.happy_mood_3vryad),
                    contentDescription = null,
                    modifier = Modifier.size((28.dp * uiScale).coerceIn(22.dp, 34.dp))
                )
                Spacer(modifier = Modifier.width((8.dp * uiScale).coerceIn(5.dp, 10.dp)))
                Text(
                    text = "Бюджет успешно распределён! Отличное решение.",
                    fontFamily = AppFontFamily,
                    fontSize = (15f * uiScale).coerceIn(11f, 17f).sp,
                    color = PixelGreenCrop,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy((10.dp * uiScale).coerceIn(6.dp, 12.dp))) {
            Text(
                text = quest.sliderLabel.ifEmpty { "Выбери сумму монет для решения:" },
                fontFamily = AppFontFamily,
                fontSize = (14f * uiScale).coerceIn(11f, 16f).sp,
                fontWeight = FontWeight.Bold,
                color = PixelTextDark
            )

            // Текущее распределение сумм
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(PixelParchmentLight)
                    .border(1.5.dp, PixelParchmentBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = (12.dp * uiScale).coerceIn(8.dp, 16.dp), vertical = (8.dp * uiScale).coerceIn(6.dp, 10.dp)),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val labelTextSize = (13f * uiScale).coerceIn(10f, 15f).sp
                Text(
                    text = "${quest.allocationTarget.ifEmpty { "В копилку" }}: $allocation ($percent%)",
                    fontFamily = AppFontFamily,
                    fontSize = labelTextSize,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE65100)
                )

                Text(
                    text = "На радости: $remaining",
                    fontFamily = AppFontFamily,
                    fontSize = labelTextSize,
                    fontWeight = FontWeight.Bold,
                    color = PixelGreenCrop
                )
            }

            // Интерактивный ползунок распределения
            val btnSize = (40.dp * uiScale).coerceIn(32.dp, 44.dp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Кнопка уменьшения на 10
                PixelButton(
                    text = "-",
                    onClick = { onAllocationChange((allocation - 10).coerceAtLeast(0)) },
                    enabled = allocation > 0,
                    containerColor = PixelGoldBright,
                    textColor = PixelTextDark,
                    modifier = Modifier.size(btnSize),
                    fontSize = (24f * uiScale).coerceIn(18f, 26f).sp,
                    contentPadding = PaddingValues(0.dp)
                )

                PixelSlider(
                    value = allocation.toFloat(),
                    onValueChange = { onAllocationChange(((it / 10).toInt() * 10).coerceIn(0, total)) },
                    valueRange = 0f..total.toFloat(),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = (8.dp * uiScale).coerceIn(4.dp, 10.dp))
                )

                // Кнопка увеличения на 10
                PixelButton(
                    text = "+",
                    onClick = { onAllocationChange((allocation + 10).coerceAtMost(total)) },
                    enabled = allocation < total,
                    containerColor = PixelGoldBright,
                    textColor = PixelTextDark,
                    modifier = Modifier.size(btnSize),
                    fontSize = (22f * uiScale).coerceIn(16f, 24f).sp,
                    contentPadding = PaddingValues(0.dp)
                )
            }

            // Кнопка подтверждения распределения
            PixelButton(
                text = "Подтвердить распределение",
                onClick = { onSubmit() },
                containerColor = PixelGoldBright,
                textColor = PixelTextDark,
                modifier = Modifier.fillMaxWidth(),
                fontSize = (16f * uiScale).coerceIn(12f, 18f).sp,
                contentPadding = PaddingValues(vertical = (11.dp * uiScale).coerceIn(7.dp, 13.dp))
            )
        }
    }
}

/**
 * Секция квеста со сдачей / расчетом (со случайным перемешиванием вариантов)
 */
@Composable
private fun CalculationQuestSection(
    quest: QuestItem,
    isDone: Boolean,
    randomOffset: Int,
    onRerollNumbers: () -> Unit,
    onSelectOption: (String) -> Unit
) {
    val uiScale = LocalUiScale.current
    if (isDone) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFE8F5E9))
                .padding((12.dp * uiScale).coerceIn(8.dp, 14.dp))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.happy_mood_3vryad),
                    contentDescription = null,
                    modifier = Modifier.size((28.dp * uiScale).coerceIn(22.dp, 34.dp))
                )
                Spacer(modifier = Modifier.width((8.dp * uiScale).coerceIn(5.dp, 10.dp)))
                Text(
                    text = "Расчёт выполнен верно! Награда начислена.",
                    fontFamily = AppFontFamily,
                    fontSize = (15f * uiScale).coerceIn(11f, 17f).sp,
                    color = PixelGreenCrop,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    } else {
        val shuffledOptions = remember(quest.id, randomOffset) { quest.options.shuffled() }

        Column(verticalArrangement = Arrangement.spacedBy((8.dp * uiScale).coerceIn(5.dp, 10.dp))) {
            // Кнопка рандомизации вариантов
            PixelButton(
                text = "Перемешать варианты",
                onClick = onRerollNumbers,
                leadingIcon = { KubikIcon(modifier = Modifier.size((16.dp * uiScale).coerceIn(12.dp, 18.dp))) },
                containerColor = Color(0xFF6D4C41),
                textColor = PixelParchmentLight,
                fontSize = (14f * uiScale).coerceIn(11f, 16f).sp,
                contentPadding = PaddingValues(
                    horizontal = (14.dp * uiScale).coerceIn(9.dp, 16.dp),
                    vertical = (7.dp * uiScale).coerceIn(4.dp, 8.dp)
                )
            )

            // Варианты ответов в аутентичном пиксельном стиле
            shuffledOptions.forEach { opt ->
                PixelButton(
                    text = opt.text,
                    onClick = { onSelectOption(opt.id) },
                    containerColor = PixelParchmentLight,
                    textColor = PixelTextDark,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = (2.dp * uiScale).coerceIn(1.dp, 3.dp)),
                    fontSize = (15f * uiScale).coerceIn(12f, 17f).sp,
                    contentPadding = PaddingValues(
                        horizontal = (14.dp * uiScale).coerceIn(9.dp, 16.dp),
                        vertical = (9.dp * uiScale).coerceIn(6.dp, 11.dp)
                    )
                )
            }
        }
    }
}

/**
 * Секция квеста с выбором варианта (CHOICE) со случайным перемешиванием порядка вариантов
 */
@Composable
private fun ChoiceQuestSection(
    quest: QuestItem,
    isDone: Boolean,
    onSelectOption: (String) -> Unit
) {
    val uiScale = LocalUiScale.current
    if (isDone) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFE8F5E9))
                .padding((12.dp * uiScale).coerceIn(8.dp, 14.dp))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.happy_mood_3vryad),
                    contentDescription = null,
                    modifier = Modifier.size((28.dp * uiScale).coerceIn(22.dp, 34.dp))
                )
                Spacer(modifier = Modifier.width((8.dp * uiScale).coerceIn(5.dp, 10.dp)))
                Text(
                    text = "Задание успешно пройдено!",
                    fontFamily = AppFontFamily,
                    fontSize = (15f * uiScale).coerceIn(11f, 17f).sp,
                    color = PixelGreenCrop,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    } else {
        val shuffledOptions = remember(quest.id) { quest.options.shuffled() }

        Column(verticalArrangement = Arrangement.spacedBy((6.dp * uiScale).coerceIn(4.dp, 8.dp))) {
            shuffledOptions.forEach { opt ->
                PixelButton(
                    text = opt.text,
                    onClick = { onSelectOption(opt.id) },
                    containerColor = PixelParchmentLight,
                    textColor = PixelTextDark,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = (2.dp * uiScale).coerceIn(1.dp, 3.dp)),
                    fontSize = (15f * uiScale).coerceIn(12f, 17f).sp,
                    contentPadding = PaddingValues(
                        horizontal = (14.dp * uiScale).coerceIn(9.dp, 16.dp),
                        vertical = (9.dp * uiScale).coerceIn(6.dp, 11.dp)
                    )
                )
            }
        }
    }
}
