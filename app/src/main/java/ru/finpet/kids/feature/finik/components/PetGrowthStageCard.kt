package ru.finpet.kids.feature.finik.components

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finpet.kids.core.designsystem.AppFontFamily
import ru.finpet.kids.core.designsystem.LocalUiScale
import ru.finpet.kids.core.designsystem.OrbRow
import ru.finpet.kids.core.designsystem.PixelGoldBright
import ru.finpet.kids.core.designsystem.PixelGreenCrop
import ru.finpet.kids.core.designsystem.PixelTextDark
import ru.finpet.kids.core.designsystem.StarIcon
import ru.finpet.kids.core.designsystem.pixelButtonBackground
import ru.finpet.kids.core.domain.model.GrowthStage

/**
 * Плашка стадии развития питомца с очками заботы и орбами прогресса.
 * Отображается при клике на котика перед списком вопросов.
 */
@Composable
fun PetGrowthStageCard(
    growthStage: String = "BABY",
    carePoints: Int = 0,
    modifier: Modifier = Modifier
) {
    val uiScale = LocalUiScale.current
    val stageEnum = try { GrowthStage.valueOf(growthStage) } catch (_: Exception) { GrowthStage.BABY }
    val (stageName, targetPts) = when (stageEnum) {
        GrowthStage.BABY -> Pair(stageEnum.title, GrowthStage.TEEN.minPoints)
        GrowthStage.TEEN -> Pair(stageEnum.title, GrowthStage.ADULT.minPoints)
        GrowthStage.ADULT -> Pair("Взрослый кот", GrowthStage.ADULT.minPoints)
    }
    val progressFraction = when (stageEnum) {
        GrowthStage.BABY -> (carePoints.toFloat() / GrowthStage.TEEN.minPoints.toFloat()).coerceIn(0f, 1f)
        GrowthStage.TEEN -> (carePoints.toFloat() / GrowthStage.ADULT.minPoints.toFloat()).coerceIn(0f, 1f)
        GrowthStage.ADULT -> 1f
    }
    val stageColor = if (stageEnum == GrowthStage.ADULT) PixelGreenCrop else Color(0xAFC1FF15)

    Column(
        modifier = modifier
            .pixelButtonBackground(containerColor = Color(0xFF9A5F83))
            .padding(
                horizontal = (14.dp * uiScale).coerceIn(8.dp, 16.dp),
                vertical = (10.dp * uiScale).coerceIn(6.dp, 12.dp)
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Стадия: $stageName",
                    fontFamily = AppFontFamily,
                    fontSize = (16f * uiScale).coerceIn(12f, 17f).sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PixelTextDark,
                    letterSpacing = 0.5.sp
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (stageEnum == GrowthStage.ADULT) "$carePoints" else "$carePoints / $targetPts",
                    fontFamily = AppFontFamily,
                    fontSize = (15f * uiScale).coerceIn(11f, 16f).sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xAFC1FF15)
                )
                Spacer(modifier = Modifier.width(3.dp))
                StarIcon(modifier = Modifier.size((20.dp * uiScale).coerceIn(10.dp, 24.dp)))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        OrbRow(
            filledRatio = progressFraction,
            tintColor = stageColor,
            orbCount = 20,
            emptyColor = Color(0xFFFF703E)
        )
    }
}
