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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import kotlinx.coroutines.launch
import ru.finpet.kids.R
import ru.finpet.kids.core.data.local.entity.QuestProgressEntity
import ru.finpet.kids.core.data.repository.QuestItem
import ru.finpet.kids.core.designsystem.AppFontFamily
import ru.finpet.kids.core.designsystem.FinCard
import ru.finpet.kids.core.designsystem.FreshGreen
import ru.finpet.kids.core.designsystem.JoyOrange
import ru.finpet.kids.core.designsystem.PixelGoldBright
import ru.finpet.kids.core.designsystem.PixelGoldDark
import ru.finpet.kids.core.designsystem.PixelGreenCrop
import ru.finpet.kids.core.designsystem.PixelParchmentBorder
import ru.finpet.kids.core.designsystem.PixelParchmentLight
import ru.finpet.kids.core.designsystem.PixelParchmentMedium
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
    val completedCount = quests.count { q ->
        progressList.any { p -> p.questId == q.id && p.isCompleted }
    }
    // Количество задач, решенных именно сегодня
    val todaySolvedCount = progressList.count { p ->
        p.isCompleted && p.completedInPeriod == currentPeriodIndex
    }
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
        subtitle = "Интерактивные квесты • Растем и учимся вместе с $petName",
        headerDrawableRes = R.drawable.school_background_empty,
        badgeContent = {
            Text(
                text = if (demoMode) "$completedCount / ${quests.size}" else "$todaySolvedCount/2 • $completedCount/${quests.size}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = PixelDarkBrown,
                fontFamily = AppFontFamily,
                maxLines = 1
            )
        },
        onDismiss = onDismiss,
        listState = listState
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {

            // Оповещение об исчерпании дневного лимита (2 задачи в день)
            if (isDailyLimitReached) {
                FinCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE8F5E9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🎓", fontSize = 22.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Дневная норма выполнена!",
                                fontFamily = AppFontFamily,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = PixelGreenCrop
                            )
                            Text(
                                text = "Ты решил 2 задачи за сегодня ($todaySolvedCount/2). Новые уроки откроются в следующий день!",
                                fontFamily = AppFontFamily,
                                fontSize = 14.sp,
                                color = PixelTextMuted,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

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
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .pixelButtonBackground(containerColor = PixelGoldBright.copy(alpha = 0.35f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "i",
                            fontFamily = AppFontFamily,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = PixelTextDark
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Словарик $petName",
                            fontFamily = AppFontFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PixelTextDark
                        )
                        Text(
                            text = "Что значит «бюджет», «накопления», «сдача» и другие слова",
                            fontFamily = AppFontFamily,
                            fontSize = 15.sp,
                            color = TextSecondary,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            // Переключатель стадий взросления питомца
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Стадии обучения $petName:",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = PixelTextDark
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
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
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFD7CCC8))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Уровень заблокирован",
                                fontFamily = AppFontFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = PixelWoodMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Уроки уровня «$stageTitle» пока закрыты",
                            fontFamily = AppFontFamily,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = PixelTextDark,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Твой питомец $petName сейчас на стадии «$currentStageTitle».\nЗаботься о котике, планируй бюджет и переходи к следующим игровым дням, чтобы он вырос и открыл этот уровень!\n\n(В Настройках можно включить Демо-режим для доступа ко всем квестам сразу)",
                            fontFamily = AppFontFamily,
                            fontSize = 16.sp,
                            color = PixelTextMuted,
                            textAlign = TextAlign.Center,
                            lineHeight = 19.sp
                        )
                    }
                }
            } else {
                // Список квестов для открытой стадии
                filteredQuests.forEach { quest ->
                    val isDone = progressList.any { it.questId == quest.id && it.isCompleted }
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
                        Column(modifier = Modifier.padding(16.dp)) {

                            // Заголовок карточки и значки темы/стадии
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TopicChip(topic = quest.topic, topicTitle = quest.topicTitle)
                                    StageBadge(stage = quest.stage)
                                }

                                if (isDone) {
                                    Text(
                                        text = "[Пройдено]",
                                        fontFamily = AppFontFamily,
                                        fontSize = 14.sp,
                                        color = FreshGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    Text(
                                        text = "+${quest.rewardCoins} монет",
                                        fontFamily = AppFontFamily,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PixelGoldDark
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Название задания
                            Text(
                                text = quest.title,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Описание ситуации
                            Text(
                                text = displaySituation,
                                fontSize = 16.sp,
                                color = TextPrimary,
                                lineHeight = 21.sp
                            )

                            // Кнопка вариативности рандома (если есть альтернативные тексты ситуации)
                            if (quest.dynamicVariants.size > 1 && !isDone && !isDailyLimitReached) {
                                Spacer(modifier = Modifier.height(8.dp))
                                PixelButton(
                                    text = "🎲 Другая ситуация",
                                    onClick = {
                                        variantIndices[quest.id] = currentVariantIdx + 1
                                    },
                                    containerColor = Color(0xFF6D4C41),
                                    textColor = PixelParchmentLight,
                                    fontSize = 14.sp,
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 7.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            if (!isDone && isDailyLimitReached) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFFFF3E0))
                                        .border(1.dp, Color(0xFFFFB74D), RoundedCornerShape(8.dp))
                                        .padding(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "⏳", fontSize = 18.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "На сегодня лимит задач исчерпан ($todaySolvedCount/2). Переходи к следующему дню, чтобы решать новые задания!",
                                            fontFamily = AppFontFamily,
                                            fontSize = 14.sp,
                                            color = Color(0xFFE65100),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            } else {
                                // --- РЕНДЕРИНГ МЕХАНИКИ ПО ТИПУ ЗАДАНИЯ ---
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
    val bg = if (isSelected) Color(0xFFFFD54F) else if (isUnlocked) Color(0xFFF3E5AB) else Color(0xFFE0D7C6)
    val textColor = if (isSelected) PixelTextDark else if (isUnlocked) PixelWoodMedium else Color(0xFF8D7B68)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) PixelWoodMedium else PixelParchmentBorder,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!isUnlocked) {
                    Text(
                        text = "[X] ",
                        fontFamily = AppFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8D7B68)
                    )
                }
                Text(
                    text = title,
                    fontFamily = AppFontFamily,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = textColor
                )
            }
            if (totalCount > 0) {
                Text(
                    text = "$countDone/$totalCount",
                    fontSize = 11.sp,
                    color = if (isSelected) PixelGreenCrop else PixelTextMuted,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Значок темы задания
 */
@Composable
private fun TopicChip(topic: String, topicTitle: String) {
    val bg = when (topic) {
        "SAVINGS" -> Color(0xFFFFE082)
        "PURCHASES" -> Color(0xFFC8E6C9)
        else -> Color(0xFFBBDEFB)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 7.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = topicTitle,
            fontFamily = AppFontFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = PixelTextDark
        )
    }
}

/**
 * Значок стадии задания
 */
@Composable
private fun StageBadge(stage: String) {
    val label = when (stage) {
        "ADULT" -> "3 стадия"
        "TEEN" -> "2 стадия"
        else -> "1 стадия"
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFEDE0D4))
            .padding(horizontal = 6.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = AppFontFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = PixelWoodMedium
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

    if (isDone) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFE8F5E9))
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(PixelGreenCrop),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "V",
                        fontFamily = AppFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Термин разгадан! Ответ принят.",
                    fontFamily = AppFontFamily,
                    fontSize = 15.sp,
                    color = PixelGreenCrop,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Подсказка, если ребенок хочет проверить себя (в пиксельном стиле кнопок)
            if (quest.hint.isNotBlank()) {
                PixelButton(
                    text = if (isHintRevealed) "💡 Скрыть подсказку" else "💡 Нужна подсказка",
                    onClick = onToggleHint,
                    containerColor = if (isHintRevealed) PixelGoldBright else Color(0xFFFFD54F),
                    textColor = PixelTextDark,
                    fontSize = 14.sp,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 7.dp)
                )

                if (isHintRevealed) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFFF9C4))
                            .border(1.dp, Color(0xFFFFD54F), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = quest.hint,
                            fontFamily = AppFontFamily,
                            fontSize = 14.sp,
                            color = PixelTextDark,
                            lineHeight = 18.sp
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
                        fontSize = 15.sp,
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .pixelButtonBackground(containerColor = if (currentInput.isNotBlank()) PixelGoldBright else Color(0xFFE0E0E0))
                    .clickable(
                        enabled = currentInput.isNotBlank(),
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        focusManager.clearFocus()
                        onSubmit()
                    }
                    .padding(vertical = 11.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Проверить ответ",
                    fontFamily = AppFontFamily,
                    color = PixelTextDark,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
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
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(PixelGreenCrop),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "V",
                        fontFamily = AppFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Бюджет успешно распределён! Отличное решение.",
                    fontFamily = AppFontFamily,
                    fontSize = 15.sp,
                    color = PixelGreenCrop,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = quest.sliderLabel.ifEmpty { "Выбери сумму монет для решения:" },
                fontFamily = AppFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = PixelTextDark
            )

            // Интерактивный ползунок распределения
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Кнопка уменьшения на 10
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFDED0B6))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onAllocationChange((allocation - 10).coerceAtLeast(0))
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "-", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = PixelTextDark)
                }

                Slider(
                    value = allocation.toFloat(),
                    onValueChange = { onAllocationChange(((it / 10).toInt() * 10).coerceIn(0, total)) },
                    valueRange = 0f..total.toFloat(),
                    steps = (total / 10) - 1,
                    colors = SliderDefaults.colors(
                        thumbColor = JoyOrange,
                        activeTrackColor = JoyOrange,
                        inactiveTrackColor = Color(0xFFC8E6C9)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 10.dp)
                )

                // Кнопка увеличения на 10
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFDED0B6))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onAllocationChange((allocation + 10).coerceAtMost(total))
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "+", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PixelTextDark)
                }
            }

            // Наглядная шкала распределения двух направлений
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF5EFEB))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${quest.allocationTarget.ifEmpty { "В копилку" }}: $allocation монет ($percent%)",
                        fontFamily = AppFontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE65100)
                    )
                    Text(
                        text = "На радости: $remaining монет",
                        fontFamily = AppFontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PixelGreenCrop
                    )
                }

                // Двухцветная визуальная полоска
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFC8E6C9))
                ) {
                    if (percent > 0) {
                        Box(
                            modifier = Modifier
                                .weight(percent.toFloat().coerceAtLeast(1f))
                                .height(12.dp)
                                .background(JoyOrange)
                        )
                    }
                    if (100 - percent > 0) {
                        Box(
                            modifier = Modifier
                                .weight((100 - percent).toFloat().coerceAtLeast(1f))
                                .height(12.dp)
                                .background(Color(0xFF81C784))
                        )
                    }
                }
            }

            // Кнопка подтверждения распределения
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .pixelButtonBackground(containerColor = PixelGoldBright)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onSubmit() }
                    .padding(vertical = 11.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Подтвердить распределение",
                    fontFamily = AppFontFamily,
                    color = PixelTextDark,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
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
    if (isDone) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFE8F5E9))
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(PixelGreenCrop),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "V",
                        fontFamily = AppFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Расчёт выполнен верно! Награда начислена.",
                    fontFamily = AppFontFamily,
                    fontSize = 15.sp,
                    color = PixelGreenCrop,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    } else {
        val shuffledOptions = remember(quest.id, randomOffset) { quest.options.shuffled() }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Кнопка рандомизации вариантов
            PixelButton(
                text = "🎲 Перемешать варианты",
                onClick = onRerollNumbers,
                containerColor = Color(0xFF6D4C41),
                textColor = PixelParchmentLight,
                fontSize = 14.sp,
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 7.dp)
            )

            // Варианты ответов в аутентичном пиксельном стиле
            shuffledOptions.forEach { opt ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                        .pixelButtonBackground(containerColor = PixelParchmentLight)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onSelectOption(opt.id) }
                        .padding(horizontal = 14.dp, vertical = 9.dp)
                ) {
                    Text(
                        text = opt.text,
                        fontFamily = AppFontFamily,
                        color = PixelTextDark,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
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
    if (!isDone) {
        val shuffledOptions = remember(quest.id) { quest.options.shuffled() }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            shuffledOptions.forEach { opt ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pixelButtonBackground(containerColor = PixelParchmentLight)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onSelectOption(opt.id) }
                        .padding(horizontal = 14.dp, vertical = 9.dp)
                ) {
                    Text(
                        text = opt.text,
                        fontFamily = AppFontFamily,
                        color = PixelTextDark,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
