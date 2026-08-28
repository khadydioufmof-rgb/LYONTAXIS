package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*

@Composable
fun SplashScreen(
  onFinishSplash: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(LyonBlueDark, LyonBluePrimary, AberDark)
        )
      )
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 32.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      // Official LyonTaxis Logo Badge
      Surface(
        shape = RoundedCornerShape(28.dp),
        color = AberDarkSurface,
        shadowElevation = 16.dp,
        modifier = Modifier.size(130.dp)
      ) {
        Image(
          painter = painterResource(id = R.drawable.lyontaxis_logo_1787822271774),
          contentDescription = "Logo LyonTaxis",
          modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(28.dp))
        )
      }

      Spacer(modifier = Modifier.height(28.dp))

      Text(
        text = "LyonTaxis",
        color = Color.White,
        fontSize = 36.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 1.sp
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "Votre taxi lyonnais en quelques secondes",
        color = Color.White.copy(alpha = 0.9f),
        fontSize = 15.sp,
        fontWeight = FontWeight.Medium
      )

      Spacer(modifier = Modifier.height(48.dp))

      Button(
        onClick = onFinishSplash,
        modifier = Modifier
          .fillMaxWidth()
          .height(54.dp)
          .testTag("splash_start_button"),
        shape = RoundedCornerShape(27.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = Color.White,
          contentColor = LyonBluePrimary
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
      ) {
        Text(
          text = "COMMANDER UN TAXI",
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.5.sp
        )
      }
    }
  }
}

