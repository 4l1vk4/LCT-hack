package ru.finpet.kids.feature.finik.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finpet.kids.core.designsystem.FinCard
import ru.finpet.kids.core.designsystem.FreshGreen
import ru.finpet.kids.core.designsystem.JoyOrange
import ru.finpet.kids.core.designsystem.OrbStatBar
import ru.finpet.kids.core.designsystem.SkyBlue
import ru.finpet.kids.core.designsystem.TextPrimary
import ru.finpet.kids.core.designsystem.TextSecondary

/**
 * Карточка состояния питомца:
 * - Отображает взвешенное общее настроение в процентах.
 * - Индикаторы характеристик: Сытость (яблоко), Энергия (молния), Здоровье (сердечко).
 * - Если стат <= 30% и котик дома, выводится знак внимания.
 */
@Composable
fun FinikStatsCard(
    displayName: String,
    effectiveMood: Int,
    satiety: Int,
    mood: Int,
    health: Int,
    isPetRunaway: Boolean,
    modifier: Modifier = Modifier
) {
    FinCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 6.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Состояние $displayName",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "$effectiveMood%",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = remember(effectiveMood) {
                        when {
                            effectiveMood >= 70 -> FreshGreen
                            effectiveMood >= 40 -> JoyOrange
                            else -> Color(0xFFFF5252)
                        }
                    }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            OrbStatBar(
                label = "Сытость",
                icon = "🍎",
                value = satiety,
                tintColor = JoyOrange,
                modifier = Modifier.fillMaxWidth(),
                showWarning = !isPetRunaway
            )
            Spacer(modifier = Modifier.height(10.dp))

            OrbStatBar(
                label = "Энергия",
                icon = "⚡",
                value = mood,
                tintColor = SkyBlue,
                modifier = Modifier.fillMaxWidth(),
                showWarning = !isPetRunaway
            )
            Spacer(modifier = Modifier.height(10.dp))

            OrbStatBar(
                label = "Здоровье",
                icon = "❤️",
                value = health,
                tintColor = FreshGreen,
                modifier = Modifier.fillMaxWidth(),
                showWarning = !isPetRunaway
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Следи за всеми тремя — от них зависит общее настроение!",
                fontSize = 13.sp,
                color = TextSecondary,
                lineHeight = 14.sp
            )
        }
    }
}
