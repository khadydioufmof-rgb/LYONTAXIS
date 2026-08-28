package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserProfile
import com.example.ui.components.AberHeader
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
  userProfile: UserProfile,
  onOpenProfile: () -> Unit,
  onLogout: () -> Unit,
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  var notificationsEnabled by remember { mutableStateOf(true) }
  var securityEnabled by remember { mutableStateOf(true) }
  var cacheCleared by remember { mutableStateOf(false) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(AberBackground)
      .verticalScroll(rememberScrollState())
  ) {
    AberHeader(
      title = "Paramètres",
      onBackClick = onBackClick
    )

    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
      // Profile Quick Card
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 3.dp,
        border = BorderStroke(1.dp, AberBorder),
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onOpenProfile() }
          .testTag("settings_profile_card")
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

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = userProfile.name,
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = AberDark
            )
            Text(
              text = userProfile.memberLevel,
              fontSize = 13.sp,
              color = LyonBluePrimary,
              fontWeight = FontWeight.SemiBold
            )
          }

          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = AberGrayText,
            modifier = Modifier.size(14.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Preferences Settings Group
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, AberBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column {
          // Notifications switch
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Notifications, contentDescription = null, tint = AberDark, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(14.dp))
              Text(text = "Notifications", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = AberDark)
            }
            Switch(
              checked = notificationsEnabled,
              onCheckedChange = { notificationsEnabled = it },
              colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = LyonBluePrimary)
            )
          }

          HorizontalDivider(color = AberBorder, modifier = Modifier.padding(horizontal = 16.dp))

          // Security switch
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Lock, contentDescription = null, tint = AberDark, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(14.dp))
              Text(text = "Sécurité & Code PIN", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = AberDark)
            }
            Switch(
              checked = securityEnabled,
              onCheckedChange = { securityEnabled = it },
              colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = LyonBluePrimary)
            )
          }

          HorizontalDivider(color = AberBorder, modifier = Modifier.padding(horizontal = 16.dp))

          SettingsActionRow(
            icon = Icons.Default.Language,
            title = "Langue",
            trailingText = "Français",
            onClick = { }
          )

          HorizontalDivider(color = AberBorder, modifier = Modifier.padding(horizontal = 16.dp))

          SettingsActionRow(
            icon = Icons.Default.CleaningServices,
            title = "Vider le cache",
            trailingText = if (cacheCleared) "0 Ko" else "42,5 Mo",
            onClick = { cacheCleared = true }
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Legal & Support Group
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, AberBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column {
          SettingsActionRow(
            icon = Icons.Default.Description,
            title = "Conditions générales & Confidentialité",
            trailingText = null,
            onClick = { }
          )

          HorizontalDivider(color = AberBorder, modifier = Modifier.padding(horizontal = 16.dp))

          SettingsActionRow(
            icon = Icons.Default.Headphones,
            title = "Nous contacter",
            trailingText = null,
            onClick = { }
          )
        }
      }

      Spacer(modifier = Modifier.height(28.dp))

      // Log out Text Button
      Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "Se déconnecter",
          color = AberRed,
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          modifier = Modifier
            .clickable { onLogout() }
            .padding(12.dp)
            .testTag("settings_logout_button")
        )
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
private fun SettingsActionRow(
  icon: ImageVector,
  title: String,
  trailingText: String?,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .padding(horizontal = 16.dp, vertical = 16.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = AberDark,
        modifier = Modifier.size(20.dp)
      )
      Spacer(modifier = Modifier.width(14.dp))
      Text(
        text = title,
        fontSize = 15.sp,
        fontWeight = FontWeight.Medium,
        color = AberDark
      )
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
      if (trailingText != null) {
        Text(
          text = trailingText,
          fontSize = 13.sp,
          color = AberGrayText
        )
        Spacer(modifier = Modifier.width(6.dp))
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
