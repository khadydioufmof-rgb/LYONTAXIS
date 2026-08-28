package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ContactFriend
import com.example.ui.components.AberPrimaryButton
import com.example.ui.theme.*

@Composable
fun InviteFriendsScreen(
  inviteCode: String,
  friends: List<ContactFriend>,
  onToggleInvite: (String) -> Unit,
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  var showContactsDialog by remember { mutableStateOf(false) }
  var copiedToast by remember { mutableStateOf(false) }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(LyonBlueLight, LyonBluePrimary, LyonBlueDark)
        )
      )
  ) {
    // Confetti particles canvas
    Canvas(modifier = Modifier.fillMaxSize()) {
      val w = size.width
      val h = size.height

      val particles = listOf(
        Pair(w * 0.15f, h * 0.18f), Pair(w * 0.85f, h * 0.15f),
        Pair(w * 0.25f, h * 0.28f), Pair(w * 0.75f, h * 0.32f),
        Pair(w * 0.1f, h * 0.45f), Pair(w * 0.9f, h * 0.48f)
      )

      particles.forEachIndexed { i, p ->
        val color = if (i % 2 == 0) LyonYellow else Color.White.copy(alpha = 0.8f)
        drawCircle(color = color, radius = 5.dp.toPx(), center = Offset(p.first, p.second))
      }
    }

    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .padding(horizontal = 24.dp, vertical = 12.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Top Bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onBackClick,
          modifier = Modifier.testTag("invite_back_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Retour",
            tint = Color.White
          )
        }

        Text(
          text = "Parrainer des amis",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )

        Spacer(modifier = Modifier.size(48.dp))
      }

      Spacer(modifier = Modifier.weight(0.2f))

      // Gift Box Celebration Graphic
      Surface(
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.2f),
        modifier = Modifier.size(160.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Surface(
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 10.dp,
            modifier = Modifier.size(110.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Default.CardGiftcard,
                contentDescription = null,
                tint = LyonBluePrimary,
                modifier = Modifier.size(60.dp)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.weight(0.3f))

      Text(
        text = "Invitez vos amis\nGagnez 3 bons de réduction !",
        fontSize = 24.sp,
        fontWeight = FontWeight.ExtraBold,
        color = Color.White,
        textAlign = TextAlign.Center,
        lineHeight = 32.sp
      )

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = "Lorsque votre ami s'inscrit avec votre code de parrainage LyonTaxis, vous recevez tous les deux 3 réductions.",
        fontSize = 14.sp,
        color = Color.White.copy(alpha = 0.9f),
        textAlign = TextAlign.Center,
        lineHeight = 20.sp,
        modifier = Modifier.padding(horizontal = 16.dp)
      )

      Spacer(modifier = Modifier.weight(0.4f))

      // Referral Code Share Box
      Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { copiedToast = true }
            .padding(horizontal = 20.dp, vertical = 14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column {
            Text(
              text = "VOTRE CODE DE PARRAINAGE",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = AberGrayText,
              letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = inviteCode,
              fontSize = 20.sp,
              fontWeight = FontWeight.ExtraBold,
              color = AberDark
            )
          }

          Surface(
            shape = CircleShape,
            color = LyonBlueLight,
            modifier = Modifier.size(42.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Partager",
                tint = LyonBluePrimary,
                modifier = Modifier.size(20.dp)
              )
            }
          }
        }
      }

      if (copiedToast) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "Code copié dans le presse-papiers !",
          color = LyonYellow,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold
        )
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Action Button
      Button(
        onClick = { showContactsDialog = true },
        modifier = Modifier
          .fillMaxWidth()
          .height(54.dp)
          .testTag("open_invite_contacts_button"),
        shape = RoundedCornerShape(27.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = Color.White,
          contentColor = LyonBluePrimary
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
      ) {
        Text(
          text = "INVITER DES AMIS",
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.5.sp
        )
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }

  if (showContactsDialog) {
    AlertDialog(
      onDismissRequest = { showContactsDialog = false },
      title = { Text("Sélectionnez des contacts à inviter", fontWeight = FontWeight.Bold) },
      text = {
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(friends) { friend ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleInvite(friend.id) }
                .padding(vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Checkbox(
                checked = friend.isInvited,
                onCheckedChange = { onToggleInvite(friend.id) },
                colors = CheckboxDefaults.colors(checkedColor = LyonBluePrimary)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(text = friend.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(text = friend.phone, fontSize = 12.sp, color = AberGrayText)
              }
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = { showContactsDialog = false },
          colors = ButtonDefaults.buttonColors(containerColor = LyonBluePrimary)
        ) {
          Text("Envoyer les invitations", color = Color.White)
        }
      },
      dismissButton = {
        TextButton(onClick = { showContactsDialog = false }) {
          Text("Fermer", color = AberGrayText)
        }
      }
    )
  }
}
