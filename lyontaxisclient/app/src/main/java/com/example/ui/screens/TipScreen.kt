package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Driver
import com.example.ui.components.AberHeader
import com.example.ui.components.AberSecondaryButton
import com.example.ui.theme.*

@Composable
fun TipScreen(
  driver: Driver,
  onCompleteTip: (Double) -> Unit,
  onSkip: () -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedTip by remember { mutableDoubleStateOf(2.0) }
  var customTipText by remember { mutableStateOf("") }
  var showCustomInput by remember { mutableStateOf(false) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(AberBackground)
  ) {
    AberHeader(
      title = "Pourboire",
      onBackClick = onSkip
    )

    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp, vertical = 20.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Driver Avatar Card
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, AberBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            shape = CircleShape,
            color = LyonBlueLight,
            border = BorderStroke(2.dp, LyonBluePrimary),
            modifier = Modifier.size(54.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = LyonBluePrimary,
                modifier = Modifier.size(32.dp)
              )
            }
          }

          Spacer(modifier = Modifier.width(14.dp))

          Column {
            Text(
              text = driver.name,
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = AberDark
            )
            Text(
              text = "${driver.carModel} • ${driver.licensePlate}",
              fontSize = 13.sp,
              color = AberGrayText
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(32.dp))

      Text(
        text = "Super ! 5 étoiles !",
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = AberDark
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "Souhaitez-vous ajouter un pourboire pour ${driver.name.split(" ").first()} ?",
        fontSize = 15.sp,
        color = AberGrayText,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(28.dp))

      // Tip Amount Chips (1€, 2€, 5€)
      Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        val amounts = listOf(1.0, 2.0, 5.0)
        amounts.forEach { amt ->
          val isSelected = selectedTip == amt && !showCustomInput
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (isSelected) LyonBluePrimary else Color.White,
            border = BorderStroke(1.dp, if (isSelected) LyonBluePrimary else AberBorder),
            shadowElevation = if (isSelected) 4.dp else 1.dp,
            modifier = Modifier
              .size(width = 80.dp, height = 54.dp)
              .clickable {
                selectedTip = amt
                showCustomInput = false
              }
              .testTag("tip_amount_${amt.toInt()}")
          ) {
            Box(contentAlignment = Alignment.Center) {
              Text(
                text = "${amt.toInt()} €",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else AberDark
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      if (showCustomInput) {
        OutlinedTextField(
          value = customTipText,
          onValueChange = {
            customTipText = it
            selectedTip = it.toDoubleOrNull() ?: 0.0
          },
          placeholder = { Text("Montant personnalisé (€)") },
          singleLine = true,
          shape = RoundedCornerShape(16.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = LyonBluePrimary,
            unfocusedBorderColor = AberBorder
          ),
          modifier = Modifier.width(220.dp)
        )
      } else {
        Text(
          text = "Choisir un autre montant",
          color = LyonBluePrimary,
          fontSize = 14.sp,
          fontWeight = FontWeight.SemiBold,
          modifier = Modifier
            .clickable { showCustomInput = true }
            .padding(6.dp)
        )
      }

      Spacer(modifier = Modifier.weight(1f))

      AberSecondaryButton(
        text = "Confirmer",
        onClick = { onCompleteTip(selectedTip) },
        backgroundColor = LyonBluePrimary,
        testTag = "tip_done_button"
      )

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = "Peut-être la prochaine fois",
        color = AberGrayText,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
          .clickable { onSkip() }
          .padding(8.dp)
      )

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}
