package ru.finpet.kids.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finpet.kids.core.designsystem.AppFontFamily
import ru.finpet.kids.core.designsystem.PixelButton
import ru.finpet.kids.core.designsystem.PixelGoldDark
import ru.finpet.kids.core.designsystem.PixelGreenCrop
import ru.finpet.kids.core.designsystem.PixelParchmentLight
import ru.finpet.kids.core.designsystem.PixelTextDark
import ru.finpet.kids.core.designsystem.PixelTextMuted
import ru.finpet.kids.core.designsystem.PixelWoodDark
import ru.finpet.kids.core.designsystem.StardewBoard

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager

@Composable
fun PetNameScreen(
    onNameConfirmed: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    val trimmed = name.trim()
    val isValid = trimmed.length in 2..16
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE8DAC2))
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        StardewBoard(
            headerTitle = "ИМЯ ПИТОМЦА",
            headerIcon = "🐾",
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Как назовём питомца?",
                    fontFamily = AppFontFamily,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = PixelTextDark,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Придумай имя от 2 до 16 букв.\nОно появится на табличке в комнате!",
                    fontFamily = AppFontFamily,
                    fontSize = 15.sp,
                    color = PixelTextMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { if (it.length <= 16) name = it },
                    label = { Text("Имя питомца", fontFamily = AppFontFamily) },
                    placeholder = { Text("Например: Финни", fontFamily = AppFontFamily) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done
                    ),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PixelTextDark,
                        unfocusedTextColor = PixelTextDark,
                        focusedBorderColor = PixelWoodDark,
                        unfocusedBorderColor = PixelGoldDark,
                        focusedLabelColor = PixelWoodDark,
                        unfocusedLabelColor = PixelTextMuted,
                        cursorColor = PixelWoodDark,
                        focusedContainerColor = PixelParchmentLight,
                        unfocusedContainerColor = PixelParchmentLight
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                PixelButton(
                    text = "Сохранить имя",
                    enabled = isValid,
                    onClick = { onNameConfirmed(trimmed) },
                    containerColor = if (isValid) PixelGreenCrop else Color(0xFFC0B098),
                    textColor = if (isValid) Color.White else Color(0xFF7A6855),
                    fontSize = 17.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}