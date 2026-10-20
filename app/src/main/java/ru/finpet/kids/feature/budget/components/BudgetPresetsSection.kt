package ru.finpet.kids.feature.budget.components

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finpet.kids.core.designsystem.AppFontFamily
import ru.finpet.kids.core.designsystem.LocalUiScale
import ru.finpet.kids.core.designsystem.PixelGoldBright
import ru.finpet.kids.core.designsystem.PixelParchmentLight
import ru.finpet.kids.core.designsystem.PixelTextDark
import ru.finpet.kids.core.designsystem.PixelTextMuted
import ru.finpet.kids.core.designsystem.RenderBoardIcon
import ru.finpet.kids.core.designsystem.pixelButtonBackground

/**
 * Компактная плашка быстрого пресета распределения монет.
 */
@Composable
fun PresetCard(
    icon: String,
    title: String,
    proportions: String,
    isSelected: Boolean = false,
    containerColor: Color = PixelParchmentLight,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val uiScale = LocalUiScale.current
    val bg = if (isSelected) PixelGoldBright else containerColor
    Box(
        modifier = modifier
            .pixelButtonBackground(containerColor = bg)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(
                horizontal = (6.dp * uiScale).coerceIn(3.dp, 8.dp),
                vertical = (5.dp * uiScale).coerceIn(3.dp, 7.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon == "↺") {
                    RenderBoardIcon(icon = icon, modifier = Modifier.size((14.dp * uiScale).coerceIn(10.dp, 14.dp)))
                } else {
                    RenderBoardIcon(icon = icon, modifier = Modifier.size((18.dp * uiScale).coerceIn(12.dp, 20.dp)))
                }
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = title,
                    fontSize = (13f * uiScale).coerceIn(10f, 15f).sp,
                    fontFamily = AppFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    color = PixelTextDark,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = proportions,
                fontSize = (12f * uiScale).coerceIn(9f, 14f).sp,
                fontFamily = AppFontFamily,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) PixelTextDark else PixelTextMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Блок быстрых детских пресетов в виде сетки 2x2.
 */
@Composable
fun BudgetPresetsSection(
    balance: Int,
    selectedPreset: String?,
    onSelectPreset: (presetKey: String, mandatory: Int, optional: Int, savings: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiScale = LocalUiScale.current

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "БЫСТРЫЕ ПРЕСЕТЫ:",
            fontFamily = AppFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (15f * uiScale).coerceIn(12f, 16f).sp,
            color = PixelTextMuted
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PresetCard(
                icon = "🌟",
                title = "Умный хозяин",
                proportions = "50% • 30% • 20%",
                isSelected = selectedPreset == "50/30/20",
                containerColor = PixelParchmentLight,
                modifier = Modifier.weight(1f),
                onClick = {
                    val m = ((balance * 0.50f) / 10).toInt() * 10
                    val s = ((balance * 0.30f) / 10).toInt() * 10
                    val o = ((balance * 0.20f) / 10).toInt() * 10
                    onSelectPreset("50/30/20", m, o, s)
                }
            )
            PresetCard(
                icon = "🎯",
                title = "Турбо-копилка",
                proportions = "35% • 55% • 10%",
                isSelected = selectedPreset == "turbo",
                containerColor = PixelParchmentLight,
                modifier = Modifier.weight(1f),
                onClick = {
                    val m = ((balance * 0.35f) / 10).toInt() * 10
                    val s = ((balance * 0.55f) / 10).toInt() * 10
                    val o = ((balance * 0.10f) / 10).toInt() * 10
                    onSelectPreset("turbo", m, o, s)
                }
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PresetCard(
                icon = "⚖️",
                title = "Поровну",
                proportions = "по 33% в баночки",
                isSelected = selectedPreset == "equal",
                containerColor = PixelParchmentLight,
                modifier = Modifier.weight(1f),
                onClick = {
                    val share = (balance / 30) * 10
                    onSelectPreset("equal", share, share, share)
                }
            )
            PresetCard(
                icon = "↺",
                title = "Сброс",
                proportions = "по 0 монет",
                isSelected = selectedPreset == "reset",
                containerColor = PixelParchmentLight,
                modifier = Modifier.weight(1f),
                onClick = {
                    onSelectPreset("reset", 0, 0, 0)
                }
            )
        }
    }
}
