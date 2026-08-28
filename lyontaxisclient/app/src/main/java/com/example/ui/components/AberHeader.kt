package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AberTealGradientEnd
import com.example.ui.theme.AberTealGradientStart

@Composable
fun AberHeader(
  title: String,
  modifier: Modifier = Modifier,
  subtitle: String? = null,
  onBackClick: (() -> Unit)? = null,
  onMenuClick: (() -> Unit)? = null,
  actionIcon: ImageVector? = null,
  actionText: String? = null,
  onActionClick: (() -> Unit)? = null,
  cornerRadius: Dp = 28.dp,
  content: (@Composable () -> Unit)? = null
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(bottomStart = cornerRadius, bottomEnd = cornerRadius))
      .background(
        Brush.horizontalGradient(
          colors = listOf(AberTealGradientStart, AberTealGradientEnd)
        )
      )
      .statusBarsPadding()
      .padding(bottom = 16.dp)
  ) {
    Column(
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (onBackClick != null) {
          IconButton(
            onClick = onBackClick,
            modifier = Modifier.testTag("header_back_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = Color.White
            )
          }
        } else if (onMenuClick != null) {
          IconButton(
            onClick = onMenuClick,
            modifier = Modifier.testTag("header_menu_button")
          ) {
            Icon(
              imageVector = Icons.Default.Menu,
              contentDescription = "Menu",
              tint = Color.White
            )
          }
        } else {
          Spacer(modifier = Modifier.width(48.dp))
        }

        Column(
          modifier = Modifier.weight(1f),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = title,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
          )
          if (subtitle != null) {
            Text(
              text = subtitle,
              color = Color.White.copy(alpha = 0.85f),
              fontSize = 12.sp,
              textAlign = TextAlign.Center
            )
          }
        }

        if (onActionClick != null && actionIcon != null) {
          IconButton(
            onClick = onActionClick,
            modifier = Modifier.testTag("header_action_button")
          ) {
            Icon(
              imageVector = actionIcon,
              contentDescription = "Action",
              tint = Color.White
            )
          }
        } else if (onActionClick != null && actionText != null) {
          TextButton(
            onClick = onActionClick,
            modifier = Modifier.testTag("header_action_text_button")
          ) {
            Text(
              text = actionText,
              color = Color.White,
              fontWeight = FontWeight.SemiBold,
              fontSize = 15.sp
            )
          }
        } else {
          Spacer(modifier = Modifier.width(48.dp))
        }
      }

      content?.invoke()
    }
  }
}
