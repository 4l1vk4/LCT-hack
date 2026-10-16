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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import ru.finpet.kids.core.designsystem.FinCard
import ru.finpet.kids.core.designsystem.JoyOrange
import ru.finpet.kids.core.designsystem.TextPrimary
import ru.finpet.kids.core.designsystem.TextSecondary

/**
 * экран настроек приложения (звуки, музыка, громкость, демо-режим).
 */
@Composable
fun SettingsScreen(
    soundEnabled: Boolean,
    musicEnabled: Boolean,
    musicVolume: Float,
    demoMode: Boolean,
    onToggleSound: (Boolean) -> Unit,
    onToggleMusic: (Boolean) -> Unit,
    onMusicVolumeChange: (Float) -> Unit,
    onToggleDemoMode: (Boolean) -> Unit,
    isNightMode: Boolean = false,
    onToggleNightMode: (Boolean) -> Unit = {},
    onClose: (() -> Unit)? = null
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 84.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
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
                }
                if (onClose != null) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onClose),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✕",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }
            }
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
            // блок демо-режима для жюри и разработчиков
            FinCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (demoMode) Modifier.border(2.dp, Color(0xFF4CAF50), RoundedCornerShape(16.dp))
                        else Modifier.border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(16.dp))
                    ),
                backgroundColor = if (demoMode) Color(0xFFF1F8E9) else Color.White
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🎮 Демо-режим",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // Подсветка статуса ВКЛ / ВЫКЛ
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (demoMode) Color(0xFFC8E6C9) else Color(0xFFEEEEEE))
                                    .border(
                                        1.dp,
                                        if (demoMode) Color(0xFF4CAF50) else Color(0xFFBDBDBD),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (demoMode) "🟢 ВКЛ" else "⚪ ВЫКЛ",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (demoMode) Color(0xFF1B5E20) else Color(0xFF757575)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (demoMode) "Включен: в календаре открыт переход между днями"
                        else "Для включения режима тестирования введите код demo",
                        fontSize = 15.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (!demoMode) {
                        var demoCodeInput by remember { mutableStateOf("") }
                        var hasCodeError by remember { mutableStateOf(false) }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = demoCodeInput,
                                onValueChange = {
                                    demoCodeInput = it
                                    hasCodeError = false
                                },
                                placeholder = { Text("Код (demo)", fontSize = 16.sp, color = Color.Gray) },
                                singleLine = true,
                                isError = hasCodeError,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (demoCodeInput.trim().equals("demo", ignoreCase = true)) {
                                        onToggleDemoMode(true)
                                        demoCodeInput = ""
                                        hasCodeError = false
                                    } else {
                                        hasCodeError = true
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = JoyOrange),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Войти", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        if (hasCodeError) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Неверный код доступа",
                                fontSize = 14.sp,
                                color = Color(0xFFD32F2F)
                            )
                        }
                    } else {
                        Button(
                            onClick = { onToggleDemoMode(false) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF757575)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Отключить демо-режим",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // ночной режим для тестирования карты
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "🌙 Ночной режим (22:00 – 06:00)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = if (isNightMode) "Включен (ночная карта, все здания закрыты)" else "Выключен (день, все здания открыты)",
                                    fontSize = 14.sp,
                                    color = TextSecondary
                                )
                            }
                            Switch(
                                checked = isNightMode,
                                onCheckedChange = { onToggleNightMode(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = JoyOrange
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
