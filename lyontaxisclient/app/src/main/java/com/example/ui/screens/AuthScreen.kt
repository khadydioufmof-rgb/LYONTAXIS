package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AberPrimaryButton
import com.example.ui.theme.*

@Composable
fun AuthScreen(
  onNavigateToOtp: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedTab by remember { mutableIntStateOf(0) } // 0: Inscription, 1: Connexion

  var identifier by remember { mutableStateOf("") }
  val trimmedIdentifier = identifier.trim()
  val isEmail = trimmedIdentifier.contains("@")
  val normalizedPhone = trimmedIdentifier.filter(Char::isDigit)
  val canContinue = if (isEmail) {
    trimmedIdentifier.matches(Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))
  } else {
    normalizedPhone.length in 8..15
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color.White)
      .verticalScroll(rememberScrollState())
  ) {
    // Curved Blue Header with LyonTaxis Brand & City Skyline
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(260.dp)
        .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
        .background(
          Brush.verticalGradient(
            colors = listOf(LyonBlueLight, LyonBluePrimary)
          )
        )
        .statusBarsPadding()
    ) {
      // Skyline Canvas at bottom of header
      Canvas(
        modifier = Modifier
          .fillMaxWidth()
          .height(100.dp)
          .align(Alignment.BottomCenter)
      ) {
        val w = size.width
        val h = size.height
        val col = Color(0x25000000)
        val buildingWidth = w / 10f
        val heights = listOf(40f, 70f, 30f, 90f, 60f, 85f, 45f, 75f, 35f, 65f)
        heights.forEachIndexed { i, bh ->
          drawRect(
            color = col,
            topLeft = Offset(i * buildingWidth, h - (bh / 100f) * h),
            size = Size(buildingWidth - 3f, (bh / 100f) * h)
          )
        }
      }

      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        // Logo
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = Color.White,
          shadowElevation = 8.dp,
          modifier = Modifier.size(68.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Default.LocalTaxi,
              contentDescription = "LyonTaxis Logo",
              tint = LyonBluePrimary,
              modifier = Modifier.size(40.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = "LyonTaxis",
          color = Color.White,
          fontSize = 28.sp,
          fontWeight = FontWeight.ExtraBold
        )
      }
    }

    // Segmented Tab Switcher [Inscription] | [Connexion]
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 32.dp, vertical = 20.dp)
        .clip(RoundedCornerShape(24.dp))
        .background(AberGrayLight)
        .padding(4.dp)
    ) {
      TabButton(
        text = "Inscription",
        isSelected = selectedTab == 0,
        onClick = { selectedTab = 0 },
        modifier = Modifier.weight(1f),
        tag = "tab_sign_up"
      )
      TabButton(
        text = "Connexion",
        isSelected = selectedTab == 1,
        onClick = { selectedTab = 1 },
        modifier = Modifier.weight(1f),
        tag = "tab_sign_in"
      )
    }

    // Form Content
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp)
    ) {
      Text(
        text = "E-mail ou numéro de téléphone",
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = AberDark
      )
      Spacer(modifier = Modifier.height(6.dp))
      OutlinedTextField(
        value = identifier,
        onValueChange = { identifier = it },
        placeholder = { Text("nom@exemple.fr ou +33 6 12 34 56 78", color = AberGrayText) },
        leadingIcon = {
          Icon(
            imageVector = if (isEmail) Icons.Default.Email else Icons.Default.Phone,
            contentDescription = null,
            tint = LyonBluePrimary
          )
        },
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .testTag(if (selectedTab == 0) "signup_identifier_input" else "signin_identifier_input"),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = LyonBluePrimary,
          unfocusedBorderColor = AberBorder
        ),
        keyboardOptions = KeyboardOptions(
          keyboardType = if (isEmail) KeyboardType.Email else KeyboardType.Phone
        ),
        supportingText = {
          if (identifier.isNotEmpty() && !canContinue) {
            Text("Saisissez un e-mail valide ou un numéro de téléphone valide", color = AberRed)
          }
        }
      )

      Spacer(modifier = Modifier.height(24.dp))

      AberPrimaryButton(
        text = if (selectedTab == 0) "Créer mon compte" else "Recevoir le code",
        onClick = {
          val destination = if (isEmail) trimmedIdentifier else {
            val digits = normalizedPhone
            if (digits.startsWith("0")) "+33${digits.drop(1)}" else "+$digits"
          }
          onNavigateToOtp(destination)
        },
        enabled = canContinue,
        testTag = if (selectedTab == 0) "signup_submit_button" else "signin_next_button"
      )

      if (selectedTab == 0) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
          text = "En vous inscrivant, vous acceptez nos Conditions d'utilisation & Politique de confidentialité",
          fontSize = 13.sp,
          color = AberGrayText,
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth()
        )
      }

      Spacer(modifier = Modifier.height(36.dp))
    }
  }
}

@Composable
private fun TabButton(
  text: String,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  tag: String
) {
  Surface(
    modifier = modifier
      .clip(RoundedCornerShape(20.dp))
      .clickable { onClick() }
      .testTag(tag),
    shape = RoundedCornerShape(20.dp),
    color = if (isSelected) LyonBluePrimary else Color.Transparent
  ) {
    Text(
      text = text,
      color = if (isSelected) Color.White else AberGrayText,
      fontSize = 15.sp,
      fontWeight = FontWeight.Bold,
      textAlign = TextAlign.Center,
      modifier = Modifier.padding(vertical = 12.dp)
    )
  }
}
