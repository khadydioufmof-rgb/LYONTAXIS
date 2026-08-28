package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.components.AberPrimaryButton
import com.example.ui.theme.*

@Composable
fun PromoCodeDialog(
  onDismiss: () -> Unit,
  onApplyPromo: (String) -> Unit,
  appliedPromo: String?
) {
  var codeText by remember { mutableStateOf(appliedPromo ?: "") }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = Color.White,
      shadowElevation = 16.dp,
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
      ) {
        // Header with Title & Close 'X'
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Code Promo",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = AberDark
          )

          IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(28.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Fermer",
              tint = AberGrayText
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Input Field
        OutlinedTextField(
          value = codeText,
          onValueChange = {
            codeText = it
            errorMessage = null
          },
          placeholder = { Text("Entrez votre code promo", color = AberGrayText) },
          leadingIcon = {
            Icon(
              imageVector = Icons.Default.ConfirmationNumber,
              contentDescription = null,
              tint = LyonBluePrimary
            )
          },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("promo_code_input"),
          shape = RoundedCornerShape(16.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = LyonBluePrimary,
            unfocusedBorderColor = AberBorder
          )
        )

        if (errorMessage != null) {
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = errorMessage!!,
            color = AberRed,
            fontSize = 12.sp
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Quick Suggestion Chips
        Text(
          text = "Codes suggérés :",
          fontSize = 12.sp,
          color = AberGrayText,
          fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = LyonBlueLight,
            border = BorderStroke(1.dp, LyonBluePrimary),
            modifier = Modifier.clickable { codeText = "LYON30" }
          ) {
            Text(
              text = "LYON30 (-30%)",
              color = LyonBluePrimary,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
          }

          Surface(
            shape = RoundedCornerShape(12.dp),
            color = LyonBlueLight,
            border = BorderStroke(1.dp, LyonBlueDark),
            modifier = Modifier.clickable { codeText = "BIENVENUE" }
          ) {
            Text(
              text = "BIENVENUE (-5 €)",
              color = LyonBlueDark,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        AberPrimaryButton(
          text = "Appliquer",
          onClick = {
            if (codeText.isNotBlank()) {
              onApplyPromo(codeText)
            } else {
              errorMessage = "Veuillez saisir un code valide"
            }
          },
          testTag = "promo_apply_button"
        )
      }
    }
  }
}
