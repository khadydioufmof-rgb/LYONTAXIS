package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun AberPrimaryButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  icon: ImageVector? = null,
  cornerRadius: Dp = 26.dp,
  height: Dp = 52.dp,
  testTag: String = "primary_button"
) {
  Button(
    onClick = onClick,
    enabled = enabled,
    modifier = modifier
      .fillMaxWidth()
      .height(height)
      .shadow(elevation = if (enabled) 6.dp else 0.dp, shape = RoundedCornerShape(cornerRadius))
      .testTag(testTag),
    shape = RoundedCornerShape(cornerRadius),
    colors = ButtonDefaults.buttonColors(
      containerColor = AberTealPrimary,
      contentColor = Color.White,
      disabledContainerColor = AberTealPrimary.copy(alpha = 0.5f),
      disabledContentColor = Color.White.copy(alpha = 0.7f)
    )
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      if (icon != null) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
      }
      Text(
        text = text,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp
      )
    }
  }
}

@Composable
fun AberSecondaryButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  icon: ImageVector? = null,
  backgroundColor: Color = LyonBluePrimary,
  testTag: String = "secondary_button"
) {
  Button(
    onClick = onClick,
    modifier = modifier
      .fillMaxWidth()
      .height(52.dp)
      .shadow(4.dp, RoundedCornerShape(26.dp))
      .testTag(testTag),
    shape = RoundedCornerShape(26.dp),
    colors = ButtonDefaults.buttonColors(
      containerColor = backgroundColor,
      contentColor = Color.White
    )
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      if (icon != null) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
      }
      Text(
        text = text,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold
      )
    }
  }
}

@Composable
fun AberDarkButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  testTag: String = "dark_button"
) {
  Button(
    onClick = onClick,
    modifier = modifier
      .fillMaxWidth()
      .height(52.dp)
      .shadow(4.dp, RoundedCornerShape(26.dp))
      .testTag(testTag),
    shape = RoundedCornerShape(26.dp),
    colors = ButtonDefaults.buttonColors(
      containerColor = AberDark,
      contentColor = Color.White
    )
  ) {
    Text(
      text = text,
      fontSize = 16.sp,
      fontWeight = FontWeight.Bold
    )
  }
}

@Composable
fun AberOutlinedButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  borderColor: Color = AberTealPrimary,
  textColor: Color = AberTealPrimary,
  testTag: String = "outlined_button"
) {
  OutlinedButton(
    onClick = onClick,
    modifier = modifier
      .fillMaxWidth()
      .height(52.dp)
      .testTag(testTag),
    shape = RoundedCornerShape(26.dp),
    border = BorderStroke(1.5.dp, borderColor),
    colors = ButtonDefaults.outlinedButtonColors(
      contentColor = textColor
    )
  ) {
    Text(
      text = text,
      fontSize = 16.sp,
      fontWeight = FontWeight.Bold
    )
  }
}

@Composable
fun AberLocationChip(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  icon: ImageVector? = null
) {
  Surface(
    modifier = modifier
      .clip(RoundedCornerShape(20.dp))
      .clickable { onClick() },
    shape = RoundedCornerShape(20.dp),
    color = AberGrayLight,
    border = BorderStroke(1.dp, AberBorder)
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      if (icon != null) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = AberTealPrimary,
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
      }
      Text(
        text = text,
        color = AberDark,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium
      )
    }
  }
}
