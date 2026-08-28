package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PaymentMethodItem
import com.example.model.PaymentType
import com.example.ui.components.AberHeader
import com.example.ui.components.AberPrimaryButton
import com.example.ui.theme.*

@Composable
fun PaymentMethodsScreen(
  paymentMethods: List<PaymentMethodItem>,
  onSelectPayment: (String) -> Unit,
  onAddPaymentMethod: (PaymentType, String, String) -> Unit,
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  var showAddDialog by remember { mutableStateOf(false) }
  var newCardNumber by remember { mutableStateOf("") }
  var newCardType by remember { mutableStateOf(PaymentType.VISA) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(AberBackground)
  ) {
    AberHeader(
      title = "Moyens de paiement",
      actionText = "Terminé",
      onActionClick = onBackClick,
      onBackClick = onBackClick
    )

    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
      Text(
        text = "MODE PAR DÉFAUT",
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = AberGrayText,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(vertical = 8.dp)
      )

      // Cash Default
      val cashMethod = paymentMethods.firstOrNull { it.type == PaymentType.CASH }
      if (cashMethod != null) {
        PaymentMethodCard(
          item = cashMethod,
          onClick = { onSelectPayment(cashMethod.id) }
        )
      }

      Spacer(modifier = Modifier.height(20.dp))

      Text(
        text = "CARTES BANCAIRES & DIGITAL",
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = AberGrayText,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(vertical = 8.dp)
      )

      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.weight(1f)
      ) {
        val nonCash = paymentMethods.filter { it.type != PaymentType.CASH }
        items(nonCash, key = { it.id }) { item ->
          PaymentMethodCard(
            item = item,
            onClick = { onSelectPayment(item.id) }
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      AberPrimaryButton(
        text = "Ajouter un moyen de paiement",
        icon = Icons.Default.Add,
        onClick = { showAddDialog = true },
        testTag = "add_payment_method_button"
      )

      Spacer(modifier = Modifier.height(12.dp))
    }
  }

  if (showAddDialog) {
    AlertDialog(
      onDismissRequest = { showAddDialog = false },
      title = { Text("Ajouter un moyen de paiement", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text("Saisissez les coordonnées de votre carte :")
          OutlinedTextField(
            value = newCardNumber,
            onValueChange = { newCardNumber = it },
            label = { Text("Numéro de carte") },
            placeholder = { Text("4970 1234 5678 9012") },
            singleLine = true
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (newCardNumber.isNotBlank()) {
              onAddPaymentMethod(
                PaymentType.VISA,
                "Carte Bancaire",
                "**** **** **** " + newCardNumber.takeLast(4)
              )
              newCardNumber = ""
              showAddDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = LyonBluePrimary)
        ) {
          Text("Ajouter", color = Color.White)
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddDialog = false }) {
          Text("Annuler", color = AberGrayText)
        }
      }
    )
  }
}

@Composable
private fun PaymentMethodCard(
  item: PaymentMethodItem,
  onClick: () -> Unit
) {
  val icon: ImageVector = when (item.type) {
    PaymentType.CASH -> Icons.Default.Euro
    PaymentType.VISA -> Icons.Default.CreditCard
    PaymentType.PAYPAL -> Icons.Default.AccountBalance
    PaymentType.MASTERCARD -> Icons.Default.CreditCard
  }

  val iconColor: Color = when (item.type) {
    PaymentType.CASH -> LyonBluePrimary
    PaymentType.VISA -> LyonBlueDark
    PaymentType.PAYPAL -> Color(0xFF003087)
    PaymentType.MASTERCARD -> Color(0xFFEB001B)
  }

  Surface(
    shape = RoundedCornerShape(16.dp),
    color = Color.White,
    shadowElevation = if (item.isSelected) 4.dp else 1.dp,
    border = BorderStroke(
      width = if (item.isSelected) 2.dp else 1.dp,
      color = if (item.isSelected) LyonBluePrimary else AberBorder
    ),
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("payment_method_${item.id}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = iconColor.copy(alpha = 0.12f),
        modifier = Modifier.size(42.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = icon,
            contentDescription = item.title,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
          )
        }
      }

      Spacer(modifier = Modifier.width(14.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = item.title,
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = AberDark
        )
        Text(
          text = item.subtitle,
          fontSize = 12.sp,
          color = AberGrayText
        )
      }

      if (item.isSelected) {
        Icon(
          imageVector = Icons.Default.CheckCircle,
          contentDescription = "Sélectionné",
          tint = LyonBluePrimary,
          modifier = Modifier.size(22.dp)
        )
      }
    }
  }
}
