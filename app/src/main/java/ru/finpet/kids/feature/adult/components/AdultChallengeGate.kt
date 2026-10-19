package ru.finpet.kids.feature.adult.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finpet.kids.core.designsystem.FinCard
import ru.finpet.kids.core.designsystem.JoyOrange
import ru.finpet.kids.core.designsystem.LocalUiScale
import ru.finpet.kids.core.designsystem.PixelButton
import ru.finpet.kids.core.designsystem.PixelGreenCrop
import ru.finpet.kids.core.designsystem.PixelPanel
import ru.finpet.kids.core.designsystem.PixelParchmentLight
import ru.finpet.kids.core.designsystem.PixelParchmentMedium
import ru.finpet.kids.core.designsystem.PixelTextDark
import ru.finpet.kids.core.designsystem.ShieldIcon
import ru.finpet.kids.core.designsystem.TextPrimary
import ru.finpet.kids.core.designsystem.TextSecondary
import ru.finpet.kids.core.domain.usecase.AdultChallenge
import androidx.compose.foundation.layout.size

/**
 * экран-барьер перед родительской аналитикой.
 * защищает раздел от ребенка вопросами про налоги, документы и школьную алгебру.
 */
@Composable
fun AdultChallengeGate(
    challenge: AdultChallenge,
    selectedAnswer: String?,
    errorMessage: String?,
    onSelectAnswer: (String) -> Unit,
    onSubmitAnswer: () -> Unit,
    onRequestNewChallenge: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiScale = LocalUiScale.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                start = (16.dp * uiScale).coerceIn(8.dp, 16.dp),
                end = (16.dp * uiScale).coerceIn(8.dp, 16.dp),
                top = 8.dp,
                bottom = 24.dp
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        FinCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding((16.dp * uiScale).coerceIn(10.dp, 20.dp)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ShieldIcon(modifier = Modifier.size((44.dp * uiScale).coerceIn(28.dp, 56.dp)))
                Spacer(modifier = Modifier.height((6.dp * uiScale).coerceIn(3.dp, 8.dp)))
                Text(
                    text = "Вход только для родителей",
                    fontSize = (18f * uiScale).coerceIn(13f, 20f).sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Подтвердите, что вы взрослый.\nВопрос рассчитан на жизненный опыт родителей:",
                    fontSize = (14f * uiScale).coerceIn(10f, 16f).sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = (18f * uiScale).coerceIn(13f, 21f).sp
                )

                Spacer(modifier = Modifier.height((8.dp * uiScale).coerceIn(4.dp, 8.dp)))

                // карточка с самим вопросом в 9-slice стиле
                PixelPanel(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(
                        horizontal = (12.dp * uiScale).coerceIn(8.dp, 14.dp),
                        vertical = (10.dp * uiScale).coerceIn(6.dp, 14.dp)
                    )
                ) {
                    Text(
                        text = challenge.question,
                        fontSize = (15f * uiScale).coerceIn(11f, 17f).sp,
                        fontWeight = FontWeight.Bold,
                        color = PixelTextDark,
                        textAlign = TextAlign.Center,
                        lineHeight = (20f * uiScale).coerceIn(14f, 22f).sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height((10.dp * uiScale).coerceIn(6.dp, 14.dp)))

                // варианты ответов
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy((6.dp * uiScale).coerceIn(4.dp, 8.dp))
                ) {
                    challenge.options.forEach { opt ->
                        val isSelected = selectedAnswer == opt
                        val isLongText = opt.length > 28
                        val baseFontSize = if (isLongText) 13f else 15f
                        PixelButton(
                            text = opt,
                            onClick = { onSelectAnswer(opt) },
                            containerColor = if (isSelected) JoyOrange else PixelParchmentLight,
                            textColor = if (isSelected) Color.White else PixelTextDark,
                            fontSize = (baseFontSize * uiScale).coerceIn(9f, 16f).sp,
                            contentPadding = PaddingValues(
                                horizontal = (10.dp * uiScale).coerceIn(6.dp, 12.dp),
                                vertical = (6.dp * uiScale).coerceIn(4.dp, 8.dp)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                errorMessage?.let { msg ->
                    Spacer(modifier = Modifier.height((8.dp * uiScale).coerceIn(4.dp, 10.dp)))
                    Text(
                        text = msg,
                        color = Color(0xFFD32F2F),
                        fontSize = (13f * uiScale).coerceIn(10f, 15f).sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height((10.dp * uiScale).coerceIn(6.dp, 14.dp)))

                PixelButton(
                    text = "Подтвердить ответ",
                    enabled = selectedAnswer != null,
                    onClick = onSubmitAnswer,
                    containerColor = PixelGreenCrop,
                    textColor = Color.White,
                    fontSize = (15f * uiScale).coerceIn(11f, 16f).sp,
                    contentPadding = PaddingValues(
                        horizontal = (12.dp * uiScale).coerceIn(8.dp, 14.dp),
                        vertical = (8.dp * uiScale).coerceIn(5.dp, 10.dp)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height((6.dp * uiScale).coerceIn(4.dp, 8.dp)))
                // если попался слишком душный вопрос, можно скипнуть на другой
                PixelButton(
                    text = "Другой вопрос",
                    onClick = onRequestNewChallenge,
                    containerColor = PixelParchmentMedium,
                    textColor = PixelTextDark,
                    fontSize = (14f * uiScale).coerceIn(10f, 15f).sp,
                    contentPadding = PaddingValues(
                        horizontal = (12.dp * uiScale).coerceIn(8.dp, 14.dp),
                        vertical = (6.dp * uiScale).coerceIn(4.dp, 8.dp)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
