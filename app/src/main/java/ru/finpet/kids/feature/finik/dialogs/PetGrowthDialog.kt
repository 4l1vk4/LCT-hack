package ru.finpet.kids.feature.finik.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ru.finpet.kids.core.designsystem.AppFontFamily
import ru.finpet.kids.core.designsystem.KeepScreenImmersive
import ru.finpet.kids.core.designsystem.PixelButton
import ru.finpet.kids.core.designsystem.PixelTextDark
import ru.finpet.kids.core.designsystem.StardewBoard

import ru.finpet.kids.core.domain.model.GrowthStage

// диалог уведомления о взрослении питомца
@Composable
fun PetGrowthDialog(
    petName: String,
    stage: GrowthStage = GrowthStage.ADULT,
    onDismiss: () -> Unit
) {
    val isAdult = stage == GrowthStage.ADULT
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        KeepScreenImmersive()
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp)
        ) {
            StardewBoard(
                headerTitle = if (isAdult) "УРА, ПИТОМЕЦ ВЫРОС!" else "УРА, ПИТОМЕЦ ПОДРОС!",
                headerIcon = if (isAdult) "🌟" else "🐾"
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (isAdult) "$petName теперь взрослый!" else "$petName стал подростком!",
                        fontFamily = AppFontFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = PixelTextDark,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isAdult) {
                            "Он вырос во взрослого и умного котика! Через неделю он сможет помогать с планом предзакупок."
                        } else {
                            "Котик заметно подрос, окреп и стал веселее! Продолжай кормить его и вести бюджет, чтобы вырастить взрослого питомца."
                        },
                        fontFamily = AppFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = PixelTextDark,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    PixelButton(
                        text = "Отлично!",
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(0.7f),
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
