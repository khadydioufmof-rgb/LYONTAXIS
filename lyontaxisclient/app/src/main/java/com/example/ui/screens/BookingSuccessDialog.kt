package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.components.AberPrimaryButton
import com.example.ui.theme.*

@Composable
fun BookingSuccessDialog(
  onDismiss: () -> Unit,
  onDone: () -> Unit,
  onCancel: () -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(28.dp),
      color = Color.White,
      shadowElevation = 24.dp,
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .testTag("booking_success_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Big Green Checkmark Icon with Glow
        Surface(
          shape = CircleShape,
          color = LyonBluePrimary,
          shadowElevation = 8.dp,
          modifier = Modifier.size(84.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Default.Check,
              contentDescription = "Succès",
              tint = Color.White,
              modifier = Modifier.size(48.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
          text = "Réservation confirmée !",
          fontSize = 22.sp,
          fontWeight = FontWeight.Bold,
          color = AberDark,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = "Votre course LyonTaxis est confirmée. Le chauffeur arrive à votre point de départ dans environ 2 minutes.",
          fontSize = 14.sp,
          color = AberGrayText,
          textAlign = TextAlign.Center,
          lineHeight = 20.sp,
          modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        AberPrimaryButton(
          text = "Suivre la course",
          onClick = onDone,
          testTag = "booking_success_done_button"
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = "Annuler",
          color = AberGrayText,
          fontSize = 15.sp,
          fontWeight = FontWeight.SemiBold,
          modifier = Modifier
            .clickable { onCancel() }
            .padding(8.dp)
            .testTag("booking_success_cancel_button")
        )
      }
    }
  }
}
