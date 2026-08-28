package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserProfile
import com.example.ui.components.AberHeader
import com.example.ui.components.AberPrimaryButton
import com.example.ui.theme.*

@Composable
fun MyWalletScreen(
  userProfile: UserProfile,
  onOpenPaymentMethods: () -> Unit,
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  var showTopUpDialog by remember { mutableStateOf(false) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(AberBackground)
  ) {
    AberHeader(
      title = "Mon Portefeuille",
      onBackClick = onBackClick
    )

    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
      // Stacked Modern Wallet Card
      Surface(
        shape = RoundedCornerShape(22.dp),
        shadowElevation = 8.dp,
        modifier = Modifier
          .fillMaxWidth()
          .height(180.dp)
      ) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(
              Brush.linearGradient(
                colors = listOf(AberDark, AberDarkSurface)
              )
            )
            .padding(20.dp)
        ) {
          Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Espèces - Moyen par défaut",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
              )
              Surface(
                shape = CircleShape,
                color = LyonBluePrimary,
                modifier = Modifier.size(10.dp)
              ) {}
            }

            Column {
              Text(
                text = "SOLDE DISPONIBLE",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "${"%.2f".format(userProfile.cashBalance)} €",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold
              )
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "EXPIRE : 12/28",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 11.sp
              )

              // 3 Pagination dots
              Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(LyonBluePrimary))
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.4f)))
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.4f)))
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Wallet Navigation Menu Card
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 3.dp,
        border = BorderStroke(1.dp, AberBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column {
          WalletMenuRow(
            icon = Icons.Default.Payment,
            title = "Moyens de paiement",
            badge = null,
            onClick = onOpenPaymentMethods,
            tag = "wallet_payment_methods_row"
          )
          HorizontalDivider(color = AberBorder, modifier = Modifier.padding(horizontal = 16.dp))

          WalletMenuRow(
            icon = Icons.Default.ConfirmationNumber,
            title = "Bons de réduction",
            badge = "${userProfile.couponsCount}",
            onClick = { },
            tag = "wallet_coupon_row"
          )
          HorizontalDivider(color = AberBorder, modifier = Modifier.padding(horizontal = 16.dp))

          WalletMenuRow(
            icon = Icons.Default.Storefront,
            title = "Boutique de fidélité",
            badge = "${userProfile.integralPoints} pts",
            onClick = { },
            tag = "wallet_integral_row"
          )
        }
      }

      Spacer(modifier = Modifier.weight(1f))

      AberPrimaryButton(
        text = "Recharger le portefeuille",
        onClick = { showTopUpDialog = true },
        icon = Icons.Default.AddCard,
        testTag = "wallet_top_up_button"
      )

      Spacer(modifier = Modifier.height(16.dp))
    }
  }

  if (showTopUpDialog) {
    AlertDialog(
      onDismissRequest = { showTopUpDialog = false },
      title = { Text("Recharger le portefeuille", fontWeight = FontWeight.Bold) },
      text = {
        Text("Sélectionnez le montant à créditer sur votre compte LyonTaxis : 20 €, 50 € ou 100 €.")
      },
      confirmButton = {
        Button(
          onClick = { showTopUpDialog = false },
          colors = ButtonDefaults.buttonColors(containerColor = LyonBluePrimary)
        ) {
          Text("Ajouter 50,00 €", color = Color.White)
        }
      },
      dismissButton = {
        TextButton(onClick = { showTopUpDialog = false }) {
          Text("Annuler", color = AberGrayText)
        }
      }
    )
  }
}

@Composable
private fun WalletMenuRow(
  icon: ImageVector,
  title: String,
  badge: String?,
  onClick: () -> Unit,
  tag: String
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .padding(horizontal = 16.dp, vertical = 16.dp)
      .testTag(tag),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = LyonBlueLight,
        modifier = Modifier.size(36.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = icon,
            contentDescription = title,
            tint = LyonBluePrimary,
            modifier = Modifier.size(20.dp)
          )
        }
      }

      Spacer(modifier = Modifier.width(14.dp))

      Text(
        text = title,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        color = AberDark
      )
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
      if (badge != null) {
        Text(
          text = badge,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = LyonBluePrimary
        )
        Spacer(modifier = Modifier.width(8.dp))
      }
      Icon(
        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
        contentDescription = null,
        tint = AberGrayText,
        modifier = Modifier.size(13.dp)
      )
    }
  }
}
