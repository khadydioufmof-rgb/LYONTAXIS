package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AberHeader
import com.example.ui.components.AberPrimaryButton
import com.example.ui.theme.*

@Composable
fun OtpVerifyScreen(
  phoneNumber: String,
  onVerifySuccess: (String) -> Unit,
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  var otpCode by remember { mutableStateOf("") }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color.White)
  ) {
    // Curved Blue Header
    AberHeader(
      title = "Vérification du numéro",
      subtitle = "Entrez votre code de sécurité reçu par SMS",
      onBackClick = onBackClick
    )

    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 24.dp, vertical = 16.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = "Code envoyé au $phoneNumber",
        fontSize = 14.sp,
        color = AberGrayText
      )

      Spacer(modifier = Modifier.height(28.dp))

      // 4-Digit OTP Code Boxes
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
      ) {
        for (i in 0 until 4) {
          val char = otpCode.getOrNull(i)?.toString()
          val isFocused = otpCode.length == i

          Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (char != null) LyonBlueLight else AberGrayLight,
            border = BorderStroke(
              width = if (isFocused) 2.dp else 1.dp,
              color = if (isFocused || char != null) LyonBluePrimary else AberBorder
            ),
            modifier = Modifier
              .padding(horizontal = 8.dp)
              .size(width = 60.dp, height = 64.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Text(
                text = char ?: "",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = AberDark
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      AberPrimaryButton(
        text = "Vérifier le code",
        onClick = { onVerifySuccess(otpCode) },
        enabled = otpCode.length == 4,
        testTag = "verify_now_button"
      )

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = "Renvoyer le code dans 0:45",
        fontSize = 13.sp,
        color = LyonBluePrimary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
          .clickable { otpCode = "5289" }
          .padding(6.dp)
      )

      Spacer(modifier = Modifier.weight(1f))

      // Custom In-App Numeric Keypad
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        val rows = listOf(
          listOf("1", "2", "3"),
          listOf("4", "5", "6"),
          listOf("7", "8", "9"),
          listOf(".", "0", "BACK")
        )

        rows.forEach { row ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
          ) {
            row.forEach { key ->
              KeypadButton(
                key = key,
                onClick = {
                  if (key == "BACK") {
                    if (otpCode.isNotEmpty()) otpCode = otpCode.dropLast(1)
                  } else if (key != ".") {
                    if (otpCode.length < 4) otpCode += key
                  }
                }
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun KeypadButton(
  key: String,
  onClick: () -> Unit
) {
  Surface(
    shape = CircleShape,
    color = if (key == "BACK") AberGrayLight else Color.White,
    border = if (key == "BACK") null else BorderStroke(1.dp, AberBorder),
    modifier = Modifier
      .size(68.dp)
      .clickable { onClick() }
      .testTag("keypad_$key")
  ) {
    Box(contentAlignment = Alignment.Center) {
      if (key == "BACK") {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.Backspace,
          contentDescription = "Backspace",
          tint = AberDark,
          modifier = Modifier.size(24.dp)
        )
      } else {
        Text(
          text = key,
          fontSize = 22.sp,
          fontWeight = FontWeight.SemiBold,
          color = AberDark
        )
      }
    }
  }
}
