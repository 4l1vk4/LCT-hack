package ru.finpet.kids.feature.map.sheets

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import ru.finpet.kids.R
import ru.finpet.kids.core.data.local.entity.QuestProgressEntity
import ru.finpet.kids.core.data.repository.QuestItem
import ru.finpet.kids.core.designsystem.AppFontFamily
import ru.finpet.kids.core.designsystem.FinCard
import ru.finpet.kids.core.designsystem.FreshGreen
import ru.finpet.kids.core.designsystem.PixelGoldBright
import ru.finpet.kids.core.designsystem.PixelGoldDark
import ru.finpet.kids.core.designsystem.PixelParchmentLight
import ru.finpet.kids.core.designsystem.PixelTextDark
import ru.finpet.kids.core.designsystem.TextPrimary
import ru.finpet.kids.core.designsystem.TextSecondary
import ru.finpet.kids.core.designsystem.pixelButtonBackground
import ru.finpet.kids.feature.map.GlossaryDialog
import ru.finpet.kids.feature.map.components.PixelLocationModalSheet

/**
 * Шторка школы финансовой грамотности:
 * - Список интерактивных уроков-квестов с выбором ответов.
 * - Интерактивный словарик понятных финансовых терминов для ребёнка.
 * - Все элементы оформлены в чистом пиксельном стиле без округлых пастельных плашек.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SchoolBottomSheet(
    quests: List<QuestItem>,
    progressList: List<QuestProgressEntity>,
    petName: String,
    targetQuestId: String? = null,
    onDismiss: () -> Unit,
    onSelectOption: (questId: String, optionId: String) -> Unit
) {
    // Считаем прогресс строго по школьным квестам (исключаем домашние дела)
    val completedCount = quests.count { q ->
        progressList.any { p -> p.questId == q.id && p.isCompleted }
    }
    var showGlossary by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    // Автоматическая плавная прокрутка к целевому уроку при переходе из задачи дня
    LaunchedEffect(targetQuestId) {
        if (targetQuestId != null) {
            delay(200)
            try {
                bringIntoViewRequester.bringIntoView()
            } catch (_: Exception) {
                listState.animateScrollToItem(1)
            }
        }
    }

    PixelLocationModalSheet(
        title = "🏫 Школа финансовой грамотности",
        subtitle = "Уроки финансовой грамотности • Полезные советы для жизни",
        headerDrawableRes = R.drawable.location_school_header,
        badgeText = "$completedCount / ${quests.size} решено",
        onDismiss = onDismiss,
        listState = listState
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                            .size(42.dp)
                            .background(Color(0xFFFFE082)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "📖", fontSize = 22.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Словарик $petName",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Что значит «бюджет», «накопления», «сдача» и другие слова",
                            fontSize = 16.sp,
                            color = TextSecondary,
                            lineHeight = 21.sp
                        )
                    }
                    Text(text = "👉", fontSize = 20.sp)
                }
            }

            // Карточки обучающих заданий
            quests.forEach { quest ->
                val isDone = progressList.any { it.questId == quest.id && it.isCompleted }
                val isTarget = targetQuestId != null && quest.id == targetQuestId

                FinCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (isTarget) Modifier.bringIntoViewRequester(bringIntoViewRequester) else Modifier)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = quest.title,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            if (isDone) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "✅ Пройдено",
                                    fontSize = 16.sp,
                                    color = FreshGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = quest.situation,
                            fontSize = 17.sp,
                            color = TextPrimary,
                            lineHeight = 22.sp
                        )

                        // Варианты ответов в аутентичном пиксельном стиле
                        if (!isDone) {
                            Spacer(modifier = Modifier.height(12.dp))
                            quest.options.forEach { opt ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp)
                                        .pixelButtonBackground(containerColor = PixelParchmentLight)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) { onSelectOption(quest.id, opt.id) }
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
            }
        }
    }

    // Модальное окно со словариком терминов
    if (showGlossary) {
        GlossaryDialog(petName = petName, onDismiss = { showGlossary = false })
    }
}
