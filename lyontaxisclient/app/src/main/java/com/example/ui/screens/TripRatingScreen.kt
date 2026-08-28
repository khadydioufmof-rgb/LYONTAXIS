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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Driver
import com.example.ui.components.AberHeader
import com.example.ui.components.AberSecondaryButton
import com.example.ui.theme.*

@Composable
fun TripRatingScreen(
  driver: Driver,
  onSubmitRating: (Float, String) -> Unit,
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  var rating by remember { mutableFloatStateOf(5.0f) }
  var comment by remember { mutableStateOf("") }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(AberBackground)
      .verticalScroll(rememberScrollState())
  ) {
    AberHeader(
      title = "Noter votre course",
      onBackClick = onBackClick
    )

    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp, vertical = 20.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Driver Profile Card
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, AberBorder),
        modifier = Modifier.fillMaxWidth()
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

          Column {
            Text(
              text = "${driver.name} - ${driver.licensePlate}",
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = AberDark
            )
            Text(
              text = driver.carModel,
              fontSize = 13.sp,
              color = AberGrayText
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(28.dp))

      Text(
        text = "Comment s'est passée votre course ?",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = AberDark,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "Votre avis permet d'améliorer la qualité des trajets LyonTaxis",
        fontSize = 14.sp,
        color = AberGrayText,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(24.dp))

      // 5-Star Rating Row
      Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        for (i in 1..5) {
          val isFilled = i <= rating
          IconButton(
            onClick = { rating = i.toFloat() },
            modifier = Modifier.size(48.dp).testTag("star_rate_$i")
          ) {
            Icon(
              imageVector = if (isFilled) Icons.Default.Star else Icons.Outlined.StarBorder,
              contentDescription = "$i étoile",
              tint = if (isFilled) AberYellow else AberGrayText,
              modifier = Modifier.size(42.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(28.dp))

      // Comments Text Area
      OutlinedTextField(
        value = comment,
        onValueChange = { comment = it },
        placeholder = { Text("Commentaires supplémentaires...", color = AberGrayText) },
        modifier = Modifier
          .fillMaxWidth()
          .height(130.dp)
          .testTag("rating_comment_input"),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = LyonBluePrimary,
          unfocusedBorderColor = AberBorder,
          focusedContainerColor = Color.White,
          unfocusedContainerColor = Color.White
        )
      )

      Spacer(modifier = Modifier.height(32.dp))

      // Submit Review
      AberSecondaryButton(
        text = "Envoyer l'avis",
        onClick = { onSubmitRating(rating, comment) },
        backgroundColor = LyonBluePrimary,
        testTag = "submit_review_button"
      )
    }
  }
}
