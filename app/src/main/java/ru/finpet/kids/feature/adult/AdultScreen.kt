package ru.finpet.kids.feature.adult

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finpet.kids.core.designsystem.CoinIcon
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.finpet.kids.core.designsystem.FinButton
import ru.finpet.kids.core.designsystem.FinCard
import ru.finpet.kids.core.designsystem.FreshGreen
import ru.finpet.kids.core.designsystem.JoyOrange
import ru.finpet.kids.core.designsystem.SkyBlue
import ru.finpet.kids.core.designsystem.TextPrimary
import ru.finpet.kids.core.designsystem.TextSecondary
import ru.finpet.kids.core.domain.usecase.AdultSectionUseCase
import ru.finpet.kids.core.domain.usecase.CompetencyReport
import ru.finpet.kids.core.domain.usecase.MathProblem
import ru.finpet.kids.core.designsystem.LocalBottomBarHeight
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults

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
    var isUnlocked by remember { mutableStateOf(false) }
    var challenge by remember { mutableStateOf(adultSectionUseCase.generateAdultChallenge()) }
    var selectedAnswer by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var bonusSuccessMessage by remember { mutableStateOf<String?>(null) }
    var showResetDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    if (!isUnlocked) {
        // --- ЗАЩИТНЫЙ ЭКРАН-БАРЬЕР ДЛЯ ВЗРОСЛОГО ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            FinCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color.White
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "🛡️", fontSize = 42.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Вход только для родителей",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Подтвердите, что вы взрослый.\nВопрос рассчитан на жизненный опыт родителей:",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 17.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Категория
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFFF3E0))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = challenge.title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Сам вопрос
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF5F5F5))
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = challenge.question,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            textAlign = TextAlign.Center,
                            lineHeight = 21.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Варианты ответа (вертикальный список для удобного чтения)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        challenge.options.forEach { opt ->
                            val isSelected = selectedAnswer == opt
                            Button(
                                onClick = {
                                    selectedAnswer = opt
                                    errorMessage = null
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) JoyOrange else Color(0xFFEEEEEE)
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Text(
                                    text = opt,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else TextPrimary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    errorMessage?.let { msg ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = msg, color = Color(0xFFD32F2F), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    FinButton(
                        text = "Подтвердить ответ",
                        enabled = selectedAnswer != null,
                        onClick = {
                            if (selectedAnswer == challenge.correctAnswer) {
                                isUnlocked = true
                            } else {
                                errorMessage = "Неверно! Задаем другой вопрос."
                                challenge = adultSectionUseCase.generateAdultChallenge()
                                selectedAnswer = null
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    TextButton(
                        onClick = {
                            challenge = adultSectionUseCase.generateAdultChallenge()
                            selectedAnswer = null
                            errorMessage = null
                        }
                    ) {
                        Text("🔄 Другой вопрос", fontSize = 13.sp, color = TextSecondary)
                    }
                }
            }
        }
    } else {
        val bottomBarHeight = LocalBottomBarHeight.current
        // --- ОСНОВНОЙ РАЗДЕЛ ДЛЯ РОДИТЕЛЕЙ ---
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = bottomBarHeight + 24.dp)
        ) {
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
                            text = "Аналитика",
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

            // Блок 1: Аналитика компетенций ребенка
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
                FinCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    backgroundColor = Color.White
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = comp.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${comp.scorePercent}%",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (comp.scorePercent >= 70) FreshGreen else JoyOrange
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { (comp.scorePercent / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (comp.scorePercent >= 70) FreshGreen else JoyOrange,
                            trackColor = Color(0xFFEEEEEE)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Статус: ${comp.statusDescription}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = comp.advice,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

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

                        // Кнопка пропуска — появляется только когда демо-режим включён
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

        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = { Text(text = "Сбросить все данные?", fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        text = "Будет полностью очищен текущий прогресс ребенка, история трат и накопления в копилке. Баланс вернется к стартовым 50 крокетсам.",
                        fontSize = 14.sp
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
                        Text("Сбросить")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) {
                        Text("Отмена")
                    }
                }
            )
        }
    }
}

@Composable
fun BonusRewardButton(
    icon: String,
    label: String,
    coins: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFFFF8E1),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFE082)),
        modifier = modifier.height(68.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = icon, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "+$coins ", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFE65100))
                CoinIcon(modifier = Modifier.size(15.dp))
            }
        }
    }
}
