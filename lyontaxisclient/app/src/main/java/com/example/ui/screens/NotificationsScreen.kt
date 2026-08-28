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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NotificationItem
import com.example.model.NotificationType
import com.example.ui.components.AberHeader
import com.example.ui.theme.*

@Composable
fun NotificationsScreen(
  notifications: List<NotificationItem>,
  onClearAll: () -> Unit,
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .background(AberBackground)
  ) {
    AberHeader(
      title = "Notifications",
      actionIcon = Icons.Default.DeleteOutline,
      onActionClick = onClearAll,
      onBackClick = onBackClick
    )

    if (notifications.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(24.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = Icons.Default.NotificationsNone,
            contentDescription = null,
            tint = AberGrayText,
            modifier = Modifier.size(64.dp)
          )
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "Aucune notification",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = AberDark
          )
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(notifications, key = { it.id }) { item ->
          NotificationCard(item = item)
        }
      }
    }
  }
}

@Composable
private fun NotificationCard(item: NotificationItem) {
  val (icon, iconColor, iconBg) = when (item.type) {
    NotificationType.SYSTEM_CONFIRM -> Triple(Icons.Default.Check, LyonBluePrimary, LyonBlueLight)
    NotificationType.SYSTEM_CANCEL -> Triple(Icons.Default.Close, AberRed, AberRedLight)
    NotificationType.SYSTEM_WALLET -> Triple(Icons.Default.CreditCard, AberDark, AberGrayLight)
    NotificationType.PROMOTION -> Triple(Icons.Default.ConfirmationNumber, LyonBluePrimary, LyonBlueLight)
  }

  Surface(
    shape = RoundedCornerShape(16.dp),
    color = Color.White,
    shadowElevation = 2.dp,
    border = BorderStroke(1.dp, AberBorder),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("notification_item_${item.id}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.Top
    ) {
      Surface(
        shape = CircleShape,
        color = iconBg,
        modifier = Modifier.size(40.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
          )
        }
      }

      Spacer(modifier = Modifier.width(14.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = item.title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = AberDark
          )
          Text(
            text = item.timeAgo,
            fontSize = 11.sp,
            color = AberGrayText
          )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = item.description,
          fontSize = 13.sp,
          color = AberDark.copy(alpha = 0.85f),
          lineHeight = 18.sp
        )
      }
    }
  }
}
