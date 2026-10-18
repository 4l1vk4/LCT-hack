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
import androidx.compose.ui.platform.LocalContext
import ru.finpet.kids.core.designsystem.AppFontFamily
import ru.finpet.kids.core.designsystem.FinCard
import ru.finpet.kids.core.designsystem.JoyOrange
import ru.finpet.kids.core.designsystem.PixelButton
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.BlendMode
import ru.finpet.kids.R
import ru.finpet.kids.core.designsystem.TextPrimary
import ru.finpet.kids.core.designsystem.TextSecondary
import ru.finpet.kids.core.designsystem.pixelButtonBackground

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
            .background(Color(0xFFE8DAC2))
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
                        fontSize = 16.sp,
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
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "${(musicVolume * 100).toInt()}%",
                                    fontSize = 15.sp,
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
                modifier = Modifier.fillMaxWidth()
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
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // статус демо-режима в пиксельном стиле с аутентичным орбом
                            Box(
                                modifier = Modifier
                                    .pixelButtonBackground(containerColor = if (demoMode) Color(0xFFC8E6C9) else Color(0xFFDED0B6))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Image(
                                        painter = painterResource(R.drawable.orb),
                                        contentDescription = null,
                                        modifier = Modifier.size(11.dp),
                                        colorFilter = ColorFilter.tint(
                                            color = if (demoMode) Color(0xFF4CAF50) else Color(0xFF9E9E9E),
                                            blendMode = BlendMode.SrcIn
                                        )
                                    )
                                    Text(
                                        text = if (demoMode) "вкл" else "выкл",
                                        fontFamily = AppFontFamily,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (demoMode) Color(0xFF1B5E20) else Color(0xFF757575)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (demoMode) "Включен: в календаре открыт переход между днями"
                        else "Для включения режима тестирования введите код demo",
                        fontSize = 16.sp,
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
                            PixelButton(
                                text = "Войти",
                                onClick = {
                                    if (demoCodeInput.trim().equals("demo", ignoreCase = true)) {
                                        onToggleDemoMode(true)
                                        demoCodeInput = ""
                                        hasCodeError = false
                                    } else {
                                        hasCodeError = true
                                    }
                                },
                                containerColor = JoyOrange,
                                textColor = Color.White,
                                fontSize = 14.sp,
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                            )
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
                        PixelButton(
                            text = "Отключить демо-режим",
                            onClick = { onToggleDemoMode(false) },
                            containerColor = Color(0xFF8D6E63),
                            textColor = Color.White,
                            fontSize = 14.sp,
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        )

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
                                    text = if (isNightMode) "Включен (ночная тема карты)" else "Выключен (дневная тема карты)",
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

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "🔔 Проверка push-уведомлений:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val context = LocalContext.current
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            PixelButton(
                                text = "8:00 Утро",
                                onClick = {
                                    ru.finpet.kids.core.notification.FinPetNotificationManager.showNotification(
                                        context,
                                        ru.finpet.kids.core.notification.NotificationType.MORNING
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                containerColor = JoyOrange,
                                textColor = Color.White,
                                fontSize = 13.sp,
                                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 6.dp)
                            )
                            PixelButton(
                                text = "20:00 Пропуск",
                                onClick = {
                                    ru.finpet.kids.core.notification.FinPetNotificationManager.showNotification(
                                        context,
                                        ru.finpet.kids.core.notification.NotificationType.INACTIVITY
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                containerColor = JoyOrange,
                                textColor = Color.White,
                                fontSize = 13.sp,
                                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 6.dp)
                            )
                            PixelButton(
                                text = "21:00 Вечер",
                                onClick = {
                                    ru.finpet.kids.core.notification.FinPetNotificationManager.showNotification(
                                        context,
                                        ru.finpet.kids.core.notification.NotificationType.EVENING
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                containerColor = JoyOrange,
                                textColor = Color.White,
                                fontSize = 13.sp,
                                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 6.dp)
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
                    fontSize = 14.sp,
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
            fontSize = 16.sp,
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
