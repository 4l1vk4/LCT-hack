package ru.finpet.kids.feature.adult

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finpet.kids.core.designsystem.FinButton
import ru.finpet.kids.core.designsystem.FinCard
import ru.finpet.kids.core.designsystem.FreshGreen
import ru.finpet.kids.core.designsystem.JoyOrange
import ru.finpet.kids.core.designsystem.LocalBottomBarHeight
import ru.finpet.kids.core.designsystem.TextPrimary
import ru.finpet.kids.core.designsystem.TextSecondary
import ru.finpet.kids.core.domain.usecase.AdultSectionUseCase
import ru.finpet.kids.core.domain.usecase.CompetencyReport
import ru.finpet.kids.feature.adult.components.AdultChallengeGate
import ru.finpet.kids.feature.adult.components.CompetencyCard

/**
 * экран родительского контроля.
 * закрыт математическим/юридическим фильтром от детей.
 * содержит аналитику освоения финансовых компетенций, переключатель демо-режима для жюри и сброс прогресса.
 */
@Composable
fun AdultScreen(
    report: CompetencyReport?,
    adultSectionUseCase: AdultSectionUseCase,
    onGrantBonus: (coins: Int, reason: String) -> Unit,
    onNextDemoPeriod: () -> Unit = {},
    onResetProfile: () -> Unit,
    demoMode: Boolean = false,
    onToggleDemoMode: (Boolean) -> Unit = {}
) {
    // держим состояние разблокировки в рамках сессии экрана
    var isUnlocked by remember { mutableStateOf(false) }
    var challenge by remember { mutableStateOf(adultSectionUseCase.generateAdultChallenge()) }
    var selectedAnswer by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showResetDialog by remember { mutableStateOf(false) }

    if (!isUnlocked) {
        // пока не ответили правильно — показываем экран защиты
        AdultChallengeGate(
            challenge = challenge,
            selectedAnswer = selectedAnswer,
            errorMessage = errorMessage,
            onSelectAnswer = { answer ->
                selectedAnswer = answer
                errorMessage = null
            },
            onSubmitAnswer = {
                if (selectedAnswer == challenge.correctAnswer) {
                    isUnlocked = true
                } else {
                    // если ошиблись, меняем вопрос, чтобы не подбирали перебором
                    errorMessage = "Неверно! Задаем другой вопрос."
                    challenge = adultSectionUseCase.generateAdultChallenge()
                    selectedAnswer = null
                }
            },
            onRequestNewChallenge = {
                challenge = adultSectionUseCase.generateAdultChallenge()
                selectedAnswer = null
                errorMessage = null
            }
        )
    } else {
        // отступ под плавающую нижнюю панель навигации
        val bottomBarHeight = LocalBottomBarHeight.current

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = bottomBarHeight + 24.dp)
        ) {
            // шапка с кнопкой закрытия раздела
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "👨‍👩‍👦 Родительский контроль",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Аналитика и настройки периода",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            isUnlocked = false
                            selectedAnswer = null
                            challenge = adultSectionUseCase.generateAdultChallenge()
                            errorMessage = null
                        },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Выйти", fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // блок компетенций ребенка (Минфин РФ / ТЗ)
            item {
                Text(
                    text = "📊 Финансовые компетенции ребенка",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(report?.competencies ?: emptyList(), key = { it.title }) { comp ->
                CompetencyCard(competency = comp)
            }

            // блок управления демо-режимом (для ускоренного тестирования экспертами и жюри)
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Демо-режим",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                FinCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = if (demoMode) Color(0xFFE8F5E9) else Color.White,
                    borderColor = if (demoMode) FreshGreen else Color(0xFFE0E0E0)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Демо-режим",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Быстрое переключение игровых дней без ожидания",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                            Switch(
                                checked = demoMode,
                                onCheckedChange = onToggleDemoMode,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = FreshGreen
                                )
                            )
                        }

                        // кнопка ручного перехода к следующему дню
                        if (demoMode) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = onNextDemoPeriod,
                                colors = ButtonDefaults.buttonColors(containerColor = JoyOrange),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Text(
                                    text = "⏭️ Пропустить один период",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // сброс данных до начального состояния
            item {
                Spacer(modifier = Modifier.height(24.dp))
                FinButton(
                    text = "Очистить все данные",
                    containerColor = Color(0xFFE53935),
                    onClick = { showResetDialog = true },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // диалог подтверждения сброса, чтобы случайно не снести профиль
        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = { Text(text = "Сбросить все данные?", fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        text = "Будет полностью очищен текущий прогресс ребенка, история трат и накопления в копилке. Баланс вернется к стартовым 50 крокетсам.",
                        fontSize = 16.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onResetProfile()
                            showResetDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                    ) {
                        Text(
                            text = "Сбросить",
                            fontSize = 16.sp
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) {
                        Text(
                            text = "Отмена",
                            fontSize = 16.sp
                        )
                    }
                }
            )
        }
    }
}
