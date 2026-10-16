package ru.finpet.kids.feature.map.sheets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
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
import ru.finpet.kids.core.designsystem.FinCard
import ru.finpet.kids.core.designsystem.FreshGreen
import ru.finpet.kids.core.designsystem.JoyOrange
import ru.finpet.kids.core.designsystem.TextPrimary
import ru.finpet.kids.core.designsystem.TextSecondary
import ru.finpet.kids.feature.map.GlossaryDialog
import ru.finpet.kids.feature.map.components.PixelLocationModalSheet

// шторка школы финграмотности: список интерактивных квестов и словарик терминов
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
    val completedCount = progressList.count { it.isCompleted }
    var showGlossary by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    // скроллим к целевому уроку при переходе из задачи дня
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
            // кнопка словарика первой в списке, чтобы ребенок мог подглядеть незнакомые слова
            Surface(
                onClick = { showGlossary = true },
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFFFF8E1),
                border = BorderStroke(2.dp, Color(0xFFFFE082)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFFFE082)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "📖", fontSize = 22.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Словарик $petName",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Что значит «бюджет», «накопления», «сдача» и другие слова",
                            fontSize = 16.sp,
                            color = TextSecondary,
                            lineHeight = 20.sp
                        )
                    }
                    Text(text = "👉", fontSize = 22.sp)
                }
            }

            // карточки заданий
            quests.forEach { quest ->
                val isDone = progressList.any { it.questId == quest.id && it.isCompleted }
                val isTarget = targetQuestId != null && quest.id == targetQuestId

                FinCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (isTarget) Modifier.bringIntoViewRequester(bringIntoViewRequester) else Modifier),
                    backgroundColor = if (isDone) Color(0xFFE8F5E9) else Color(0xFFFFF9E6),
                    borderColor = if (isTarget) JoyOrange else if (isDone) FreshGreen else Color(0xFFFFD54F)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = quest.title,
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        if (isDone) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.9f),
                                border = BorderStroke(1.dp, FreshGreen)
                            ) {
                                Text(
                                    text = "✅ Пройдено",
                                    fontSize = 16.sp,
                                    color = FreshGreen,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = quest.situation,
                            fontSize = 19.sp,
                            color = TextPrimary,
                            lineHeight = 25.sp
                        )

                        // если квест еще не решен, показываем варианты ответа
                        if (!isDone) {
                            Spacer(modifier = Modifier.height(12.dp))
                            quest.options.forEach { opt ->
                                Button(
                                    onClick = { onSelectOption(quest.id, opt.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .border(1.5.dp, JoyOrange, RoundedCornerShape(14.dp)),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
                                ) {
                                    Text(
                                        text = opt.text,
                                        color = TextPrimary,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Medium,
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

    // диалог со словариком терминов
    if (showGlossary) {
        GlossaryDialog(petName = petName, onDismiss = { showGlossary = false })
    }
}
