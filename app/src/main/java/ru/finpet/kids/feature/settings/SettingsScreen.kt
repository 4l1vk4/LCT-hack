package ru.finpet.kids.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finpet.kids.core.designsystem.FinCard
import ru.finpet.kids.core.designsystem.JoyOrange
import ru.finpet.kids.core.designsystem.TextPrimary
import ru.finpet.kids.core.designsystem.TextSecondary

/**
 * экран настроек приложения (звуки, музыка, громкость).
 */
@Composable
fun SettingsScreen(
    soundEnabled: Boolean,
    musicEnabled: Boolean,
    musicVolume: Float,
    onToggleSound: (Boolean) -> Unit,
    onToggleMusic: (Boolean) -> Unit,
    onMusicVolumeChange: (Float) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 84.dp)
    ) {
        item {
            Text(
                text = "⚙️ Настройки",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
            Text(
                text = "Управление звуком и музыкой",
                fontSize = 14.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        item {
            // блок аудио-настроек
            FinCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color.White
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingSwitchRow(
                        title = "Звуковые эффекты (монетки, тапы)",
                        checked = soundEnabled,
                        onCheckedChange = onToggleSound
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    SettingSwitchRow(
                        title = "Фоновая музыка",
                        checked = musicEnabled,
                        onCheckedChange = onToggleMusic
                    )
                    // ползунок громкости доступен только если музыка включена
                    if (musicEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Громкость музыки",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "${(musicVolume * 100).toInt()}%",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = JoyOrange
                                )
                            }
                            Slider(
                                value = musicVolume,
                                onValueChange = onMusicVolumeChange,
                                valueRange = 0f..1f,
                                colors = SliderDefaults.colors(
                                    thumbColor = JoyOrange,
                                    activeTrackColor = JoyOrange,
                                    inactiveTrackColor = Color(0xFFFFE0B2)
                                )
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Версия 1.0.0",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
            }
        }
    }
}

/**
 * строка с переключателем в стиле дизайн-системы приложения
 */
@Composable
fun SettingSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = JoyOrange
            )
        )
    }
}
