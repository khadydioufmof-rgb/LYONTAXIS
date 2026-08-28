package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AberPrimaryButton
import com.example.ui.theme.*

data class OnboardingStep(
  val title: String,
  val description: String,
  val icon: ImageVector,
  val accentColor: Color
)

@Composable
fun OnboardingScreen(
  onComplete: () -> Unit,
  modifier: Modifier = Modifier
) {
  var currentStep by remember { mutableIntStateOf(0) }

  val steps = listOf(
    OnboardingStep(
      title = "Commander une course",
      description = "Commandez un taxi en quelques secondes et soyez pris en charge par un chauffeur lyonnais agréé",
      icon = Icons.Default.DirectionsCar,
      accentColor = LyonBluePrimary
    ),
    OnboardingStep(
      title = "Chauffeurs certifiés",
      description = "Notre réseau de chauffeurs professionnels à Lyon vous garantit confort, sécurité et tarifs transparents",
      icon = Icons.Default.VerifiedUser,
      accentColor = LyonBlueDark
    ),
    OnboardingStep(
      title = "Suivi en temps réel",
      description = "Visualisez votre itinéraire et l'arrivée de votre chauffeur en direct sur la carte avec arrêts multiples",
      icon = Icons.Default.LocationSearching,
      accentColor = LyonBluePrimary
    )
  )

  val current = steps[currentStep]

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color.White)
      .statusBarsPadding()
      .navigationBarsPadding()
      .padding(horizontal = 24.dp, vertical = 16.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Top Skip Action
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.End
    ) {
      if (currentStep < steps.size - 1) {
        TextButton(
          onClick = onComplete,
          modifier = Modifier.testTag("onboarding_skip_button")
        ) {
          Text(
            text = "Passer",
            color = AberGrayText,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
          )
        }
      } else {
        Spacer(modifier = Modifier.height(48.dp))
      }
    }

    Spacer(modifier = Modifier.weight(0.4f))

    // Illustration Graphics Card
    Surface(
      shape = RoundedCornerShape(32.dp),
      color = LyonBlueLight,
      modifier = Modifier
        .size(240.dp)
        .shadow(8.dp, RoundedCornerShape(32.dp))
    ) {
      Box(contentAlignment = Alignment.Center) {
        // Decorative background circles
        Canvas(modifier = Modifier.fillMaxSize()) {
          drawCircle(
            color = LyonBluePrimary.copy(alpha = 0.15f),
            radius = size.width * 0.45f,
            center = Offset(size.width * 0.5f, size.height * 0.5f)
          )
          drawCircle(
            color = LyonBluePrimary.copy(alpha = 0.25f),
            radius = size.width * 0.32f,
            center = Offset(size.width * 0.5f, size.height * 0.5f)
          )
        }

        // Feature Icon
        Surface(
          shape = CircleShape,
          color = Color.White,
          shadowElevation = 6.dp,
          modifier = Modifier.size(90.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = current.icon,
              contentDescription = current.title,
              tint = current.accentColor,
              modifier = Modifier.size(48.dp)
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.weight(0.4f))

    // Title & Description
    Text(
      text = current.title,
      fontSize = 24.sp,
      fontWeight = FontWeight.Bold,
      color = AberDark,
      textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(14.dp))

    Text(
      text = current.description,
      fontSize = 15.sp,
      color = AberGrayText,
      textAlign = TextAlign.Center,
      lineHeight = 22.sp,
      modifier = Modifier.padding(horizontal = 16.dp)
    )

    Spacer(modifier = Modifier.weight(0.5f))

    // Step Indicator Dots
    Row(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      steps.indices.forEach { index ->
        val isActive = index == currentStep
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (isActive) LyonBluePrimary else AberBorder)
            .size(width = if (isActive) 24.dp else 8.dp, height = 8.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(32.dp))

    // Bottom Action Button
    if (currentStep < steps.size - 1) {
      AberPrimaryButton(
        text = "SUIVANT",
        onClick = { currentStep++ },
        testTag = "onboarding_next_button"
      )
    } else {
      AberPrimaryButton(
        text = "COMMENCER",
        onClick = onComplete,
        testTag = "onboarding_get_started_button"
      )
    }

    Spacer(modifier = Modifier.height(16.dp))
  }
}
